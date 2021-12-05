package application.desktop.ui.components.editor;

import application.desktop.DesktopApplication;
import application.desktop.ui.Colour;
import application.desktop.ui.FontAwesomeIcon;
import application.desktop.ui.components.RectBorderType;
import application.desktop.ui.components.common.Component;
import imgui.*;
import imgui.flag.ImGuiButtonFlags;
import imgui.flag.ImGuiMouseButton;
import utils.Pair;
import warehouse.*;
import warehouse.tiles.*;

/**
 * A canvas that visualizes the Warehouse.
 */
public class WarehouseCanvas extends Component {
    private Warehouse warehouse;
    private WarehouseCanvasColourScheme colourScheme;

    private float gridStep;
    private float minSizeX;
    private float minSizeY;
    private boolean showGrid;

    private int frameCounter;

    private ImVec2 size;
    private ImVec2 contentTopLeft;

    private TileType tileTypeToInsert;
    private TileFactory tileFactory;

    /**
     * Offset applied to canvas elements to enable scrolling.
     */
    private final ImVec2 panOffset;
    /**
     * Current input mode of this WarehouseCanvas.
     */
    private WarehouseCanvasInputMode inputMode;
    /**
     * The currently selected tile, or null if no tile is selected.
     */
    private Tile selectedTile;

    /**
     * Construct a new WarehouseCanvas with a default colour scheme.
     *
     * @param warehouse The Warehouse to visualise.
     */
    public WarehouseCanvas(Warehouse warehouse) {
        this(warehouse, WarehouseCanvasColourScheme.DEFAULT,
                32.0f,
                100.0f, 100.0f,
                true);
    }

    /**
     * Construct a new WarehouseCanvas with a custom colour scheme.
     *
     * @param warehouse    The Warehouse to visualise.
     * @param colourScheme The colour scheme of this WarehouseCanvas.
     * @param gridStep     The size of a grid cell in screen coordinates.
     * @param minSizeX     The minimum horizontal size of the canvas, in pixels.
     * @param minSizeY     The minimum vertical size of the canvas, in pixels.
     * @param showGrid     Whether to show the grid.
     */
    public WarehouseCanvas(Warehouse warehouse,
                           WarehouseCanvasColourScheme colourScheme,
                           float gridStep,
                           float minSizeX, float minSizeY,
                           boolean showGrid) {
        this.warehouse = warehouse;
        this.colourScheme = colourScheme;
        this.gridStep = gridStep;
        this.minSizeX = minSizeX;
        this.minSizeY = minSizeY;
        this.showGrid = showGrid;

        tileTypeToInsert = TileType.RACK;
        tileFactory = new TileFactory();

        size = new ImVec2(minSizeX, minSizeY);
        contentTopLeft = new ImVec2(0, 0);
        panOffset = new ImVec2(0, 0);
        inputMode = WarehouseCanvasInputMode.SELECT_TILE;

        frameCounter = 0;
    }

    @Override
    public void drawContent(DesktopApplication application) {
        updateTransform();
        handleInteraction();

        // NOTE: The frame counter is a hacky way of centering the contents on load. It works by waiting for the second
        // frame, so that all the contents have been rendered and updated at least once.
        if (frameCounter <= 1) {
            panToCentre();
            frameCounter += 1;
        }

        ImDrawList drawList = ImGui.getWindowDrawList();
        drawBackground(drawList);
        drawCanvas(drawList);
    }


    /**
     * Return the origin of the canvas in screen space.
     */
    private ImVec2 getOrigin() {
        return new ImVec2(contentTopLeft.x + panOffset.x, contentTopLeft.y + panOffset.y);
    }

    /**
     * Return the position of the mouse relative to the canvas origin.
     */
    private ImVec2 getRelativeMousePosition() {
        ImGuiIO io = ImGui.getIO();
        ImVec2 origin = getOrigin();
        return new ImVec2(io.getMousePos().x - origin.x, io.getMousePos().y - origin.y);
    }

    /**
     * Return the coordinates of the tile at the given point in warehouse space.
     * @remark Note that it is NOT guaranteed that the warehouse space coordinates correspond to an actual tile
     * in the warehouse, e.g. the returned coordinates may be out of bounds!
     * @param p The point in screen space to convert to warehouse space.
     */
    private Pair<Integer, Integer> screenToWarehousePoint(ImVec2 p) {
        int tileX = (int) Math.floor(p.x / gridStep);
        int tileY = (int) Math.floor(p.y / gridStep);
        return new Pair<>(tileX, tileY);
    }

    /**
     * Return the Tile at the given screen point, or null if the point (in warehouse space) is out of bounds.
     * @param p The position of the tile in screen space.
     */
    private Tile getTileFromScreenPoint(ImVec2 p) {
        Pair<Integer, Integer> tileCoords = screenToWarehousePoint(p);
        return warehouse.getTileAt(tileCoords.getFirst(), tileCoords.getSecond());
    }

    /**
     * Update the panOffset so that the contents are centered in the canvas. Mutates panOffset.
     */
    private void panToCentre() {
        panOffset.x = gridStep * (float) Math.floor(warehouse.getWidth() / 2.0f)
                + size.x / 2.0f - gridStep * warehouse.getWidth();
        panOffset.y = gridStep * (float) Math.floor(warehouse.getHeight() / 2.0f)
                + size.y / 2.0f - gridStep * warehouse.getHeight();
    }

    /**
     * Update transform information such as size and position.
     */
    private void updateTransform() {
        ImVec2 contentAvailable = ImGui.getContentRegionAvail();
        size = new ImVec2(Math.max(contentAvailable.x, minSizeX),
                Math.max(contentAvailable.y, minSizeY));
        contentTopLeft = ImGui.getCursorScreenPos();
    }

    /**
     * Handle user interaction.
     */
    private void handleInteraction() {
        ImGuiIO io = ImGui.getIO();
        ImGui.invisibleButton("canvas", size.x, size.y, ImGuiButtonFlags.MouseButtonLeft |
                ImGuiButtonFlags.MouseButtonRight);
        boolean isHovered = ImGui.isItemHovered();
        if (isHovered && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            ImGui.setWindowFocus();
        }

//        // Add first and second point
//        if (isHovered && !adding_line && ImGui.isMouseClicked(ImGuiMouseButton.Left))
//        {
//            points.add(mousePosition);
//            points.add(mousePosition);
//            adding_line = true;
//        }
//        if (adding_line)
//        {
//            points.set(points.size() - 1, mousePosition);
////            points.back() = mouse_pos_in_canvas;
//            if (!ImGui.isMouseDown(ImGuiMouseButton.Left))
//                adding_line = false;
//        }

        // Pan (we use a zero mouse threshold when there's no context menu)
        // You may decide to make that threshold dynamic based on whether the mouse is hovering something etc.
        if (isHovered && ImGui.isMouseDragging(ImGuiMouseButton.Right, -1)) {
            panOffset.x += io.getMouseDelta().x;
            panOffset.y += io.getMouseDelta().y;
        }

        // Context menu (under default mouse threshold)
        ImVec2 dragDelta = ImGui.getMouseDragDelta(ImGuiMouseButton.Right);
        if (ImGui.isMouseReleased(ImGuiMouseButton.Right) && dragDelta.x == 0.0f && dragDelta.y == 0.0f) {
            ImGui.openPopupOnItemClick("context");
        }

        if (ImGui.beginPopup("context")) {
            ImGui.text("context menu");
            ImGui.endPopup();
        }

        if (isHovered && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            handleLeftClick();
        }
    }

    /**
     * Handle left click interaction.
     */
    private void handleLeftClick() {
        selectedTile = getTileFromScreenPoint(getRelativeMousePosition());
        if (inputMode == WarehouseCanvasInputMode.INSERT_TILE) {
            Pair<Integer, Integer> tileCoords = screenToWarehousePoint(getRelativeMousePosition());
            Tile tileToInsert = tileFactory.createTile(tileTypeToInsert, tileCoords.getFirst(), tileCoords.getSecond());
            if (tileToInsert != null) {
                warehouse.setTile(tileToInsert);
                selectedTile = tileToInsert;
            }
        }
    }

    /**
     * Draw the canvas background. Mutates the given ImDrawList.
     */
    private void drawBackground(ImDrawList drawList) {
        ImVec2 contentBottomRight = getContentBottomRight();
        drawRect(drawList, contentTopLeft, contentBottomRight, colourScheme.getBackgroundColour(),
                colourScheme.getBorderColour(), 1, 0, RectBorderType.Middle);
    }

    /**
     * Draw the canvas grid. Mutates the given ImDrawList.
     */
    private void drawCanvas(ImDrawList drawList) {
        if (!showGrid) {
            return;
        }

        ImVec2 bottomRight = getContentBottomRight();
        drawList.pushClipRect(contentTopLeft.x, contentTopLeft.y, bottomRight.x, bottomRight.y, true);
        final int gridLineColour = colourScheme.getGridLineColour().toU32Colour();

        // Draw horizontal lines
        for (float x = panOffset.x % gridStep; x < size.x; x += gridStep) {
            float x1 = contentTopLeft.x + x;
            float x2 = contentTopLeft.x + x;
            drawList.addLine(x1, contentTopLeft.y, x2, bottomRight.y, gridLineColour);
        }
        // Draw vertical lines
        for (float y = panOffset.y % gridStep; y < size.y; y += gridStep) {
            float y1 = contentTopLeft.y + y;
            float y2 = contentTopLeft.y + y;
            drawList.addLine(contentTopLeft.x, y1, bottomRight.x, y2, gridLineColour);
        }

        drawWarehouse(drawList);
        drawList.popClipRect();
    }

    /**
     * Draw the warehouse centered at the origin. Mutates the given ImDrawList.
     */
    private void drawWarehouse(ImDrawList drawList) {
        ImVec2 origin = getOrigin();

        // Colours
        Colour WORLD_BORDER_COLOUR = new Colour(113, 129, 109, 1.0f);
        Colour FLOOR_TILE_COLOUR = new Colour(101, 101, 101, 0.5f);

        // Draw border
        float worldBorderThickness = 2.0f;
        float worldBorderRadius = 10.0f;
        ImVec2 borderBottomRight = new ImVec2(origin.x + gridStep * warehouse.getWidth(),
                origin.y + gridStep * warehouse.getHeight());
        drawRect(drawList, origin, borderBottomRight, null, WORLD_BORDER_COLOUR,
                worldBorderThickness, worldBorderRadius, RectBorderType.Outer);

        // Draw tiles
        for (int y = 0; y < warehouse.getHeight(); y++) {
            for (int x = 0; x < warehouse.getWidth(); x++) {
                ImVec2 topLeft = getTileTopLeft(x, y);
                ImVec2 bottomRight = getTileBottomRight(x, y);
                drawRect(drawList, topLeft, bottomRight, FLOOR_TILE_COLOUR);

                Tile tile = warehouse.getTileAt(x, y);
                if (tile instanceof Rack) {
                    drawRack(drawList, x, y);
                } else if (tile instanceof ReceiveDepot) {
                    drawReceiveDepot(drawList, x, y);
                } else if (tile instanceof ShipDepot) {
                    drawShipDepot(drawList, x, y);
                }
            }
        }

        drawSelectedTile(drawList);
    }

    private void drawSelectedTile(ImDrawList drawList) {
        if (selectedTile == null || selectedTile instanceof EmptyTile) return;

        ImVec2 topLeft = getTileTopLeft(selectedTile.getX(), selectedTile.getY());
        ImVec2 bottomRight = getTileBottomRight(selectedTile.getX(), selectedTile.getY());

        Colour outlineColour = getCurrentTileHandleColour();
        drawRect(drawList, topLeft, bottomRight, null, outlineColour,
                1, 0, RectBorderType.Outer);

        // Draw handles
        if (inputMode.equals(WarehouseCanvasInputMode.MOVE_TILE)) {
            ImVec2 handleSize = new ImVec2(6, 6);
            float handleBorderThickness = 1.5f;
            // Top left
            drawRectFromCentre(drawList, topLeft, handleSize,
                    Colour.WHITE, outlineColour,
                    handleBorderThickness, 0, RectBorderType.Outer);
            // Top right
            drawRectFromCentre(drawList, new ImVec2(bottomRight.x, topLeft.y), handleSize,
                    Colour.WHITE, outlineColour,
                    handleBorderThickness, 0, RectBorderType.Outer);

            // Bottom left
            drawRectFromCentre(drawList, new ImVec2(topLeft.x, bottomRight.y), handleSize,
                    Colour.WHITE, outlineColour,
                    handleBorderThickness, 0, RectBorderType.Outer);
            // Bottom right
            drawRectFromCentre(drawList, bottomRight, handleSize,
                    Colour.WHITE, outlineColour,
                    handleBorderThickness, 0, RectBorderType.Outer);
        }
    }

    /**
     * Draw a rectangle from the given top-left coordinate to the given bottom-right coordinate.
     * @param drawList The draw list to add the rectangle to.
     * @param topLeft The top-left coordinate of the rectangle.
     * @param bottomRight The bottom-right coordinate of the rectangle.
     * @param fill The fill colour of the rectangle. If null, then no fill is drawn.
     * @param border The border colour of the rectangle. If null, the no border is drawn.
     * @param borderThickness The thickness of the border.
     * @param borderRadius The radius of the border.
     * @param borderType The type of border to draw.
     */
    private void drawRect(ImDrawList drawList, ImVec2 topLeft, ImVec2 bottomRight, Colour fill,
                          Colour border, float borderThickness, float borderRadius, RectBorderType borderType) {
        // Fill coordinates
        float x1 = topLeft.x;
        float y1 = topLeft.y;
        float x2 = bottomRight.x;
        float y2 = bottomRight.y;
        // Border coordinates
        float bx1 = x1;
        float by1 = y1;
        float bx2 = x2;
        float by2 = y2;

        if (border != null) {
            borderThickness = Math.max(0, borderThickness);
            borderRadius = Math.max(0, borderRadius);
            // Transform coordinates based on border type
            float halfBorderThickness = borderThickness * 0.5f;
            if (borderType == RectBorderType.Outer) {
                bx1 -= halfBorderThickness;
                by1 -= halfBorderThickness;
                bx2 += halfBorderThickness;
                by2 += halfBorderThickness;
                x1 -= halfBorderThickness;
                y1 -= halfBorderThickness;
                x2 += halfBorderThickness;
                y2 += halfBorderThickness;
            } else if (borderType == RectBorderType.Inner) {
                bx1 += halfBorderThickness;
                by1 += halfBorderThickness;
                bx2 -= halfBorderThickness;
                by2 -= halfBorderThickness;
                x1 += halfBorderThickness;
                y1 += halfBorderThickness;
                x2 -= halfBorderThickness;
                y2 -= halfBorderThickness;
            }
            // NOTE: Do nothing if borderType == RectBorderType.Middle since the ImDrawList
            // draws a middle border by default.
        }

        if (fill != null) {
            drawList.addRectFilled(x1, y1, x2, y2,
                    fill.toU32Colour(), borderRadius);
        }

        if (border != null) {
            // Only draw the border if there is a border to draw!
            drawList.addRect((float)Math.floor(bx1), (float)Math.floor(by1),
                    (float)Math.ceil(bx2), (float)Math.ceil(by2),
                    border.toU32Colour(), borderRadius, 0, borderThickness);
        }
    }

    /**
     * Draw a rectangle from the given top-left coordinate to the given bottom-right coordinate with an inner border.
     * @param drawList The draw list to add the rectangle to.
     * @param topLeft The top-left coordinate of the rectangle.
     * @param bottomRight The bottom-right coordinate of the rectangle.
     * @param fill The fill colour of the rectangle. If null, then no fill is drawn.
     * @param border The border colour of the rectangle. If null, the no border is drawn.
     * @param borderThickness The thickness of the border.
     * @param borderRadius The radius of the border.
     */
    private void drawRect(ImDrawList drawList, ImVec2 topLeft, ImVec2 bottomRight, Colour fill,
                          Colour border, float borderThickness, float borderRadius) {
        drawRect(drawList, topLeft, bottomRight, fill, border, borderThickness, borderRadius, RectBorderType.Inner);
    }

    /**
     * Draw a rectangle from the given top-left coordinate to the given bottom-right coordinate with no border.
     * @param drawList The draw list to add the rectangle to.
     * @param topLeft The top-left coordinate of the rectangle.
     * @param bottomRight The bottom-right coordinate of the rectangle.
     * @param fill The fill colour of the rectangle. If null, then no fill is drawn.
     */
    private void drawRect(ImDrawList drawList, ImVec2 topLeft, ImVec2 bottomRight, Colour fill) {
        drawRect(drawList, topLeft, bottomRight, fill, null, 0, 0);
    }

    /**
     * Draw a rectangle of the given size and whose centre is the given point.
     * @param drawList The draw list to add the rectangle to.
     * @param centre The centre of the rectangle.
     * @param size The size of the rectangle.
     * @param fill The fill colour of the rectangle. If null, then no fill is drawn.
     * @param border The border colour of the rectangle. If null, the no border is drawn.
     * @param borderThickness The thickness of the border. If non-positive, then no border is drawn.
     * @param borderRadius The radius of the border. If non-positive, then no border radius is used.
     * @param borderType The type of border to draw.
     */
    private void drawRectFromCentre(ImDrawList drawList, ImVec2 centre, ImVec2 size, Colour fill,
                                    Colour border, float borderThickness, float borderRadius, RectBorderType borderType) {
        ImVec2 topLeft = new ImVec2(centre.x - size.x * 0.5f, centre.y - size.y * 0.5f);
        ImVec2 bottomRight = new ImVec2(centre.x + size.x * 0.5f, centre.y + size.y * 0.5f);
        drawRect(drawList, topLeft, bottomRight, fill, border, borderThickness, borderRadius, borderType);
    }

    /**
     * Draw a rectangle of the given size and whose centre is the given point with no border.
     * @param drawList The draw list to add the rectangle to.
     * @param centre The centre of the rectangle.
     * @param size The size of the rectangle.
     * @param fill The fill colour of the rectangle. If null, then no fill is drawn.
     */
    private void drawRectFromCentre(ImDrawList drawList, ImVec2 centre, ImVec2 size, Colour fill) {
        drawRectFromCentre(drawList, centre, size, fill, null, 0, 0, RectBorderType.Inner);
    }

    /**
     * Get the colour of the tile handle outline.
     */
    private Colour getCurrentTileHandleColour() {
        if (inputMode == WarehouseCanvasInputMode.MOVE_TILE) {
            return colourScheme.getMoveOutlineColour();
        } else {
            return colourScheme.getSelectionOutlineColour();
        }
    }

    /**
     * Get the top-left coordinate of the tile at the given warehouse space coordinates.
     * @param tileX The horizontal coordinate of the tile, in warehouse space.
     * @param tileY The vertical coordinate of the tile, in warehouse space.
     * @return An ImVec2 representing the top-left coordinate of the tile in screen space.
     */
    private ImVec2 getTileTopLeft(int tileX, int tileY) {
        ImVec2 origin = getOrigin();
        float gridStep = getGridStep();
        return new ImVec2(origin.x + gridStep * tileX, origin.y + gridStep * tileY);
    }

    /**
     * Get the bottom-right coordinate of the tile at the given warehouse space coordinates.
     * @param tileX The horizontal coordinate of the tile, in warehouse space.
     * @param tileY The vertical coordinate of the tile, in warehouse space.
     * @return An ImVec2 representing the bottom-right coordinate of the tile in screen space.
     */
    private ImVec2 getTileBottomRight(int tileX, int tileY) {
        ImVec2 topLeft = getTileTopLeft(tileX, tileY);
        float gridStep = getGridStep();
        return new ImVec2(topLeft.x + gridStep, topLeft.y + gridStep);
    }

    private void drawRack(ImDrawList drawList, int tileX, int tileY) {
        float thickness = 2.0f;
        Colour BACKGROUND_COLOUR = new Colour(68, 118, 160, 0.2f);
        Colour BORDER_COLOUR = new Colour(BACKGROUND_COLOUR, 1.0f);

        ImVec2 topLeft = getTileTopLeft(tileX, tileY);
        ImVec2 bottomRight = getTileBottomRight(tileX, tileY);
        drawRect(drawList, topLeft, bottomRight, BACKGROUND_COLOUR, BORDER_COLOUR,
                thickness, 5.0f, RectBorderType.Inner);
    }

    private void drawReceiveDepot(ImDrawList drawList, int tileX, int tileY) {
        float thickness = 2.0f;
        Colour BACKGROUND_COLOUR = new Colour(207, 192, 121, 0.2f);
        Colour BORDER_COLOUR = new Colour(BACKGROUND_COLOUR, 1.0f);
        Colour ICON_COLOUR = new Colour(223, 224, 223, 0.6f);

        ImVec2 topLeft = getTileTopLeft(tileX, tileY);
        ImVec2 bottomRight = getTileBottomRight(tileX, tileY);
        drawRect(drawList, topLeft, bottomRight, BACKGROUND_COLOUR, BORDER_COLOUR,
                thickness,5.0f, RectBorderType.Inner);

        float iconSize = 17.5f;
        drawList.addText(ImGui.getFont(), iconSize,
                (topLeft.x + bottomRight.x - iconSize) / 2,
                (topLeft.y + bottomRight.y - iconSize) / 2,
                ICON_COLOUR.toU32Colour(), FontAwesomeIcon.SignInAlt.getIconCode());
    }

    private void drawShipDepot(ImDrawList drawList, int tileX, int tileY) {
        float thickness = 2.0f;
        Colour BACKGROUND_COLOUR = new Colour(166, 109, 113, 0.2f);
        Colour BORDER_COLOUR = new Colour(BACKGROUND_COLOUR, 1.0f);
        Colour ICON_COLOUR = new Colour(223, 224, 223, 0.6f);

        ImVec2 topLeft = getTileTopLeft(tileX, tileY);
        ImVec2 bottomRight = getTileBottomRight(tileX, tileY);
        drawRect(drawList, topLeft, bottomRight, BACKGROUND_COLOUR, BORDER_COLOUR,
                thickness,5.0f, RectBorderType.Inner);

        float iconSize = 17.5f;
        ImFont font = ImGui.getFont();
        drawList.addText(font, iconSize,
                (topLeft.x + bottomRight.x - iconSize) / 2 + 3,
                (topLeft.y + bottomRight.y - iconSize) / 2,
                ICON_COLOUR.toU32Colour(), FontAwesomeIcon.SignOutAlt.getIconCode());
    }

    /**
     * Get the bottom right coordinate of the canvas.
     */
    private ImVec2 getContentBottomRight() {
        return new ImVec2(contentTopLeft.x + size.x, contentTopLeft.y + size.y);
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public WarehouseCanvasColourScheme getColourScheme() {
        return colourScheme;
    }

    public void setColourScheme(WarehouseCanvasColourScheme colourScheme) {
        this.colourScheme = colourScheme;
    }

    public float getGridStep() {
        return gridStep;
    }

    public void setGridStep(float gridStep) {
        this.gridStep = gridStep;
    }

    public float getMinSizeX() {
        return minSizeX;
    }

    public void setMinSizeX(float minSizeX) {
        this.minSizeX = minSizeX;
    }

    public float getMinSizeY() {
        return minSizeY;
    }

    public void setMinSizeY(float minSizeY) {
        this.minSizeY = minSizeY;
    }

    public WarehouseCanvasInputMode getInputMode() {
        return inputMode;
    }

    public void setInputMode(WarehouseCanvasInputMode inputMode) {
        this.inputMode = inputMode;
    }

    public TileType getTileTypeToInsert() {
        return tileTypeToInsert;
    }

    public void setTileTypeToInsert(TileType tileTypeToInsert) {
        this.tileTypeToInsert = tileTypeToInsert;
    }

    public Tile getSelectedTile() {
        return selectedTile;
    }

    public void setSelectedTile(Tile selectedTile) {
        this.selectedTile = selectedTile;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }
}

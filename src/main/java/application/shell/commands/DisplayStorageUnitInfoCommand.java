package application.shell.commands;


import application.shell.ShellApplication;
import application.shell.commands.framework.ShellCommand;
import application.shell.commands.framework.ShellCommandArg;
import application.shell.commands.framework.ShellCommandArgContainer;
import application.shell.commands.framework.ShellCommandSpec;
import warehouse.storage.StorageUnit;
import warehouse.tiles.StorageTile;
import warehouse.tiles.Tile;
import warehouse.TileOutOfBoundsException;
import warehouse.Warehouse;

/**
 * Argument container for the DisplayStorageUnitInfoCommand.
 */
class DisplayStorageUnitInfoCommandArgContainer extends ShellCommandArgContainer {
    @ShellCommandArg
    private int x;
    @ShellCommandArg
    private int y;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

/**
 * A command to display info about a storage unit at a location.
 */
@ShellCommandSpec(name = "display-storage-unit-info", description = "Display info about a storage unit at a location.")
public class DisplayStorageUnitInfoCommand extends ShellCommand {
    @Override
    public String execute(ShellApplication application, ShellCommandArgContainer argContainer) {
        DisplayStorageUnitInfoCommandArgContainer args = (DisplayStorageUnitInfoCommandArgContainer) argContainer;
        Warehouse warehouse = application.getWarehouseController().getWarehouse();
        try {
            Tile tile = warehouse.getTileAt(args.getX(), args.getY());
            if (tile instanceof StorageTile) {
                return ((StorageTile)tile).getStorageUnit().toString();
            } else {
                return String.format("the tile at (%d, %d) is not a StorageTile", args.getX(), args.getY());
            }
        } catch (TileOutOfBoundsException e) {
            return e.getMessage();
        }
    }

    @Override
    public ShellCommandArgContainer createArgContainer() {
        return new DisplayStorageUnitInfoCommandArgContainer();
    }
}

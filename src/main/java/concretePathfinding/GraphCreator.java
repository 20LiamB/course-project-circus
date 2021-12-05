package concretePathfinding;
import complexPathfinder.Graph;
import warehouse.Tile;
import warehouse.TileOutOfBoundsException;

import java.util.*;

/**
 * This class will be an assistant class to the PathfinderController which will help it with Graph construction.
 *
 */
public class GraphCreator<T> {

    private final Tile[][] map;

    /**
     * Creates an instance of GraphCreator
     * @param tiles the set of all tiles in the warehouse
     */
    public GraphCreator(Tile[][] tiles) {
        this.map = tiles;
    }

    /**
     *
     * @return the set of empty TileNodes in the warehouse
     */
    public Set<TileNode> getNodes()
    {
        Set<TileNode> nodes = new HashSet<>();

        for (Tile[] tile : this.map) {
            for (Tile value : tile) {
                if (value.isEmpty())
                    nodes.add(new TileNode(value));
            }
        }

        return nodes;
    }
    /**
     * @param node TileNode in the warehouse layout.
     * @return Set of Id's of all connected nodes.
     * @throws TileOutOfBoundsException if the tile location is out of Bounds
     */
    public Set<String> getConnections(TileNode node) throws TileOutOfBoundsException {
        Set<String> connections = new HashSet<String>();

        if(node.getT().getY()< map[0].length-1) {
            if (this.map[node.getT().getX()][node.getT().getY() + 1].isEmpty())
                connections.add((new TileNode(this.map[node.getT().getX()][node.getT().getY() + 1])).getId());
        }
        if(node.getT().getY()-1 >= 0) {
            if(this.map[node.getT().getX()][node.getT().getY()-1].isEmpty())
                connections.add((new TileNode(this.map[node.getT().getX()][node.getT().getY()-1])).getId());
        }
        if(node.getT().getX()< map.length-1) {
            if (this.map[node.getT().getX() + 1][node.getT().getY()].isEmpty())
                connections.add((new TileNode(this.map[node.getT().getX() + 1][node.getT().getY()])).getId());
        }
        if(node.getT().getX()-1 >=0) {
            if  (this.map[node.getT().getX() - 1][node.getT().getY()].isEmpty())
                connections.add((new TileNode(this.map[node.getT().getX() - 1][node.getT().getY()]).getId()));
        }
        return connections;
    }

    /**
     *
     * @return returns the graph of the warehouse
     * @throws TileOutOfBoundsException
     */
    public Graph getGraph() throws TileOutOfBoundsException {
        Set<TileNode> tileSet = this.getNodes();
        Map<String, Set<String>> connections = new HashMap<String, Set<String>>();
        for (TileNode t: tileSet){
            connections.put(t.getId(), this.getConnections(t));
        }
        return(new Graph(tileSet, connections));
    }




}

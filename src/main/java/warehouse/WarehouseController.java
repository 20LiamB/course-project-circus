package warehouse;


import warehouse.inventory.PartCatalogue;
import warehouse.inventory.Item;
import warehouse.logistics.assignment.BasicRackAssignmentPolicy;
import warehouse.logistics.assignment.BasicReceiveDepotAssignmentPolicy;
import warehouse.logistics.assignment.BasicShipDepotAssignmentPolicy;
import warehouse.logistics.assignment.StorageTileAssignmentPolicy;
import warehouse.logistics.orders.OrderCreatedAtComparator;
import warehouse.logistics.orders.PlaceOrder;
import warehouse.tiles.Rack;
import warehouse.tiles.ReceiveDepot;
import warehouse.tiles.ShipDepot;
import warehouse.logistics.orders.Order;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Controls the Warehouse.
 */
public class WarehouseController {
    private final Warehouse warehouse;
    private final PartCatalogue partCatalogue;

    private final StorageTileAssignmentPolicy<ReceiveDepot> receiveDepotAssignmentPolicy;
    private final StorageTileAssignmentPolicy<ShipDepot> shipDepotAssignmentPolicy;
    private final StorageTileAssignmentPolicy<Rack> rackAssignmentPolicy;

    private final Queue<Order> orderQueue;

    /**
     * Construct a WarehouseController.
     * @param warehouse The Warehouse to manage.
     * @param partCatalogue Available parts serviced by the Warehouse.
     * @param receiveDepotAssignmentPolicy The policy for assigning incoming items to a ReceiveDepot.
     * @param shipDepotAssignmentPolicy The policy for assigning outgoing items to a ShipDepot.
     * @param rackAssignmentPolicy The policy for assigning items to a Rack.
     */
    public WarehouseController(Warehouse warehouse, PartCatalogue partCatalogue,
                               StorageTileAssignmentPolicy<ReceiveDepot> receiveDepotAssignmentPolicy,
                               StorageTileAssignmentPolicy<ShipDepot> shipDepotAssignmentPolicy,
                               StorageTileAssignmentPolicy<Rack> rackAssignmentPolicy)
    {
        this.warehouse = warehouse;
        this.partCatalogue = partCatalogue;
        // Assignment policies
        this.receiveDepotAssignmentPolicy = receiveDepotAssignmentPolicy;
        this.shipDepotAssignmentPolicy = shipDepotAssignmentPolicy;
        this.rackAssignmentPolicy = rackAssignmentPolicy;
        // Initialise order queue
        orderQueue = new PriorityQueue<>(new OrderCreatedAtComparator());
    }

    /**
     * Construct a WarehouseController with basic assignment policies.
     * @param warehouse The Warehouse to manage.
     * @param partCatalogue Available parts serviced by the Warehouse.
     */
    public WarehouseController(Warehouse warehouse, PartCatalogue partCatalogue) {
        this(warehouse, partCatalogue,
                new BasicReceiveDepotAssignmentPolicy(),
                new BasicShipDepotAssignmentPolicy(),
                new BasicRackAssignmentPolicy());
    }

    /**
     * Receive an Item from the outside world. This will place the Item into an available ReceiveDepot, and then issue
     * an Order for the Item to be moved from that ReceiveDepot to an available StorageUnit in the Warehouse.
     * @param item The Item to insert.
     * @return a PlaceOrder representing a request to move the Item to an available StorageUnit in the Warehouse,
     * or null if the Item cannot be inserted into the Warehouse.
     */
    public PlaceOrder receiveItem(Item item) {
        ReceiveDepot receiveDepot = receiveDepotAssignmentPolicy.assign(item, warehouse);
        if (receiveDepot == null) {
            // Item could not be assigned to a ReceiveDepot (source)!
            return null;
        } else {
            PlaceOrder order = new PlaceOrder(receiveDepot, item);
            orderQueue.add(order);
            return order;
        }
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public PartCatalogue getPartCatalogue() {
        return partCatalogue;
    }
}
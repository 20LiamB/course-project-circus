
import org.junit.jupiter.api.Test;
import warehouse.inventory.Item;
import warehouse.inventory.Part;
import warehouse.orders.Order;
import warehouse.orders.OrderQueue;

import static org.junit.jupiter.api.Assertions.*;

public class OrderQueueTest {
    @Test
    public void testCompleteOrder() {
        OrderQueue queue = new OrderQueue();
        Item item = new Item(new Part("Cucumber", "A Vegetable"));
        Order order = new Order(item);
        queue.addOrder(order);
        queue.completeOrder();
        assertTrue(queue.emptyQueue());
        assertEquals(order.getStatus(), "Order complete");
    }

    @Test
    public void testAddOrder() {
        OrderQueue queue = new OrderQueue();
        Item item = new Item(new Part("Cucumber", "A Vegetable"));
        Item item2 = new Item(new Part("Banana", "A Fruit"));
        Order order = new Order(item);
        Order order2 = new Order(item2);
        queue.addOrder(order);
        queue.addOrder(order2);
        assertEquals(queue.completeOrder(), item);
        assertEquals(queue.completeOrder(), item2);
    }

}

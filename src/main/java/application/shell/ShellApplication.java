package application.shell;

import application.shell.commands.*;
import application.shell.commands.framework.ShellCommand;
import application.shell.commands.framework.ShellCommandExecutor;
import inventory.InventoryCatalogue;
import inventory.Item;
import serialization.FileObjectSaver;
import serialization.JsonFileObjectSaver;
import warehouse.Warehouse;
import warehouse.WarehouseController;

import java.util.Scanner;

/**
 * Driver class for the shell application.
 */
public class ShellApplication {
    private final FileObjectSaver<Item> itemFileSaver;
    private final FileObjectSaver<InventoryCatalogue> inventoryCatalogueFileSaver;
    private final WarehouseController warehouseController;

    private boolean isRunning;
    private final ShellCommandExecutor commandExecutor;

    /**
     * Construct a ShellApplication.
     */
    public ShellApplication() {
        // Serialization adapters
        itemFileSaver = new JsonFileObjectSaver<>();
        inventoryCatalogueFileSaver = new JsonFileObjectSaver<>();
        // TODO: Should the warehouse controller be made here?!
        // TODO: Replace empty warehouse with file loading or something
        // TODO: Don't hardcode warehouse dimensions
        warehouseController = new WarehouseController(
                new Warehouse(10, 10),
                new InventoryCatalogue()
        );

        isRunning = false;
        commandExecutor = new ShellCommandExecutor(this, new ShellCommand[]{
                new CreateItemCommand(),
                new CreateStorageUnitCommand(),
                new InsertItemCommand(),
                new DisplayWarehouseCommand(),
                new DisplayStorageUnitInfoCommand(),
                new DisplayInventoryCommand(),
                new SaveItemCommand(),
                new HelpCommand(),
                new ExitCommand(),
        });
    }

    /**
     * Run the shell application.
     */
    private void run() {
        isRunning = true;
        Scanner in = new Scanner(System.in);
        while (isRunning) {
            // Read input
            System.out.print("> ");
            String input = in.nextLine();
            // Execute commands
            String output = commandExecutor.execute(input);
            // Display output
            if (output != null && !output.isEmpty()) {
                System.out.println(output);
            }
        }
        in.close();
    }

    /**
     * Stop running the application.
     */
    public void stop() {
        isRunning = false;
    }

    /**
     * Get the WarehouseController for this application.
     * @return the WarehouseController instance.
     */
    public WarehouseController getWarehouseController() {
        return warehouseController;
    }

    public FileObjectSaver<Item> getItemFileSaver() {
        return itemFileSaver;
    }

    public FileObjectSaver<InventoryCatalogue> getInventoryCatalogueFileSaver() {
        return inventoryCatalogueFileSaver;
    }

    /**
     * Get the ShellCommandExecutor for this application.
     * @return the ShellCommandExecutor instance.
     */
    public ShellCommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    /**
     * Entrypoint for the shell application.
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        ShellApplication application = new ShellApplication();
        application.run();
    }
}

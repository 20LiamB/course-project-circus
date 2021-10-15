package circus.application.commands;

import circus.application.ShellApplication;

/**
 * A shell command.
 */
public abstract class ShellCommand {
    /**
     * Create the arg container for this command.
     * @return a ShellCommandArgContainer instance.
     */
    public ShellCommandArgContainer createArgContainer() {
        return new ShellCommandArgContainer();
    }

    /**
     * Execute the command. Optionally mutate the application.
     * @param application The application state.
     * @param args the arguments to this ShellCommand.
     * @return the output of the command. This can be null.
     */
    public abstract String execute(ShellApplication application, ShellCommandArgContainer args);

    /**
     * Check whether this ShellCommand has a ShellCommandSpec.
     * @return True if it has a spec, and False otherwise.
     */
    public boolean hasSpec() {
        return this.getClass().isAnnotationPresent(ShellCommandSpec.class);
    }

    /**
     * Get the ShellCommandSpec for this ShellCommand.
     * @return the command spec.
     * @throws ShellCommandSpecNotFoundException thrown when the command does not have a ShellCommandSpec annotation.
     */
    public ShellCommandSpec getSpec() throws ShellCommandSpecNotFoundException {
        if (!hasSpec()) {
            throw new ShellCommandSpecNotFoundException(this.getClass().getSimpleName());
        } else {
            return this.getClass().getAnnotation(ShellCommandSpec.class);
        }
    }
}

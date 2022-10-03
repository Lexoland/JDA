package dev.lexoland.jda.api.interaction;

public class CommandException extends RuntimeException {

    private final Object[] values;

    public CommandException(String tl, Object... values) {
        super(tl);
        this.values = values;
    }

    public Object[] getValues() {
        return values;
    }
}

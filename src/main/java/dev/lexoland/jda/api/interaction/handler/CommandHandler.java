package dev.lexoland.jda.api.interaction.handler;

import dev.lexoland.jda.api.interaction.executor.CommandExecutor;
import dev.lexoland.jda.api.interaction.executor.ContextCommandExecutor;
import dev.lexoland.jda.api.interaction.executor.SlashCommandExecutor;
import dev.lexoland.jda.api.interaction.response.CommandResponseHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandHandler extends ListenerAdapter {

    private final HashSet<CommandExecutor> commands = new HashSet<>();
    private final Function<GenericCommandInteractionEvent, CommandResponseHandler> responseHandlerFactory;

    public CommandHandler() {
        this(event -> new CommandResponseHandler(event, null));
    }

    public CommandHandler(Function<GenericCommandInteractionEvent, CommandResponseHandler> responseHandlerFactory) {
        this.responseHandlerFactory = responseHandlerFactory;
    }

    /**
     * Registers a command to the handler.
     *
     * @param command The command to register.
     */
    public void register(CommandExecutor command) {
        commands.add(command);
    }

    /**
     * Updates all registered commands to the guild.
     * @param guild The guild to update the commands to.
     */
    public void updateCommands(Guild guild) {
        guild.updateCommands().addCommands(
                commands.stream()
                        .map(CommandExecutor::init)
                        .collect(Collectors.toList())
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        CommandResponseHandler responseHandler = createResponseHandler(e);
        responseHandler.catchExceptions(() -> {
            for (CommandExecutor command : commands)
                if(command instanceof SlashCommandExecutor slashCommand)
                    slashCommand.onSlashCommandInteraction(e, responseHandler);
        });
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent e) {
        for (CommandExecutor command : commands)
            if (command instanceof SlashCommandExecutor slashCommand)
                slashCommand.onCommandAutoCompleteInteraction(e).suggest(e);
    }

    @Override
    public void onGenericContextInteraction(@NotNull GenericContextInteractionEvent<?> e) {
        CommandResponseHandler responseHandler = createResponseHandler(e);
        responseHandler.catchExceptions(() -> {
            for (CommandExecutor command : commands)
                if (command instanceof ContextCommandExecutor contextCommand)
                    contextCommand.onContextInteraction(e, responseHandler);
        });
    }

    /**
     * Returns all registered commands.
     * @return The registered commands.
     */
    public HashSet<CommandExecutor> getCommands() {
        return commands;
    }

    /**
     * Creates a new {@link CommandResponseHandler} for the given {@link GenericCommandInteractionEvent}.
     * @param event The event to create the response handler for.
     * @return The created response handler.
     */
    public CommandResponseHandler createResponseHandler(GenericCommandInteractionEvent event) {
        return responseHandlerFactory.apply(event);
    }
}

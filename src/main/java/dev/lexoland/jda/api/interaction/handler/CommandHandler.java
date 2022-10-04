package dev.lexoland.jda.api.interaction.handler;

import dev.lexoland.jda.api.LocalizationManager;
import dev.lexoland.jda.api.interaction.executor.CommandExecutor;
import dev.lexoland.jda.api.interaction.executor.ContextCommandExecutor;
import dev.lexoland.jda.api.interaction.executor.SlashCommandExecutor;
import dev.lexoland.jda.api.interaction.response.CommandResponseHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandHandler extends ListenerAdapter {

    private final HashSet<CommandExecutor> commands = new HashSet<>();
    private final HashSet<CommandExecutor> globalCommands = new HashSet<>();
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
     * Registers a global command to the handler.
     *
     * @param command The command to register.
     */
    public void registerGlobal(CommandExecutor command) {
        globalCommands.add(command);
    }

    /**
     * Updates all registered commands to the guild.
     * @param guild The guild to update the commands to
     */
    public void updateCommands(Guild guild) {
        guild.updateCommands().addCommands(
                commands.stream()
                        .map(CommandExecutor::init)
                        .collect(Collectors.toList())
        ).queue();
    }

    /**
     * Updates all registered global commands to the JDA instance.
     * @param jda The JDA instance to update the commands to
     */
    public void updateGlobalCommands(JDA jda) {
        jda.updateCommands().addCommands(
                globalCommands.stream()
                        .map(CommandExecutor::init)
                        .collect(Collectors.toList())
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        CommandResponseHandler responseHandler = createResponseHandler(e);
        responseHandler.catchExceptions(() -> {
            if(e.isGlobalCommand()) {
                for (CommandExecutor command : globalCommands)
                    if (command instanceof SlashCommandExecutor slashCommand)
                        slashCommand.onSlashCommandInteraction(e, responseHandler);
            } else {
                for (CommandExecutor command : commands)
                    if (command instanceof SlashCommandExecutor slashCommand)
                        slashCommand.onSlashCommandInteraction(e, responseHandler);
            }
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

    /**
     * Create a slash command builder.
     *
     * @param name        The command name, 1-32 lowercase alphanumeric characters
     * @param description The command description, 1-100 characters
     * @return {@link SlashCommandData} builder for slash commands
     * @throws IllegalArgumentException If any of the following requirements are not met
     *                                  <ul>
     *                                      <li>The name must be lowercase alphanumeric (with dash), 1-32 characters long</li>
     *                                      <li>The description must be 1-100 characters long</li>
     *                                  </ul>
     */
    @Nonnull
    public static SlashCommandData slash(@Nonnull String name, @Nonnull String description) {
        SlashCommandData commandData = Commands.slash(name, description);
        if (LocalizationManager.INSTANCE != null)
            commandData.setLocalizationFunction(LocalizationManager.INSTANCE);
        return commandData;
    }

    /**
     * Create a message context menu command builder.
     *
     * @param name The command name, 1-32 characters
     * @return {@link CommandData}
     * @throws IllegalArgumentException If the name is not between 1-32 characters long
     */
    @Nonnull
    public static CommandData message(@Nonnull String name) {
        CommandData commandData = Commands.message(name);
        if (LocalizationManager.INSTANCE != null)
            commandData.setLocalizationFunction(LocalizationManager.INSTANCE);
        return commandData;
    }

    /**
     * Create a user context menu command builder.
     *
     * @param name The command name, 1-32 characters
     * @return {@link CommandData}
     * @throws IllegalArgumentException If the name is not between 1-32 characters long
     */
    @Nonnull
    public static CommandData user(@Nonnull String name) {
        CommandData commandData = Commands.user(name);
        if (LocalizationManager.INSTANCE != null)
            commandData.setLocalizationFunction(LocalizationManager.INSTANCE);
        return commandData;
    }
}

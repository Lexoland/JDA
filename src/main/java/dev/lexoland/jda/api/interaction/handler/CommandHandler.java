package dev.lexoland.jda.api.interaction.handler;

import dev.lexoland.jda.api.LocalizationManager;
import dev.lexoland.jda.api.interaction.CommandException;
import dev.lexoland.jda.api.interaction.executor.CommandExecutor;
import dev.lexoland.jda.api.interaction.executor.SlashCommandExecutor;
import dev.lexoland.jda.api.interaction.response.CommandResponseHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles slash commands and context menus.
 */
public class CommandHandler {

    private final HashSet<CommandExecutor> commands = new HashSet<>();
    private final HashSet<CommandExecutor> globalCommands = new HashSet<>();
    private final Function<GenericCommandInteractionEvent, CommandResponseHandler> responseHandlerFactory;
    private final HashMap<String, Pair<Method, CommandExecutor>> commandInvokers = new HashMap<>();
    private final HashMap<String, Pair<Method, CommandExecutor>> globalCommandInvokers = new HashMap<>();

    /**
     * Creates a new command handler with a default response handler factory. These response handlers doesn't use logging channels. If you want to use logging channels, use {@link #CommandHandler(Function)}.
     */
    public CommandHandler() {
        this(event -> new CommandResponseHandler(event, null));
    }

    /**
     * Creates a new command handler with a response handler factory. This can be used to set a custom response handler e.g. for logging channels.
     *
     * @param responseHandlerFactory The factory to create a response handler.
     */
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
        initInvokers(command, commandInvokers);
    }

    /**
     * Registers a global command to the handler.
     *
     * @param command The command to register.
     */
    public void registerGlobal(CommandExecutor command) {
        globalCommands.add(command);
        initInvokers(command, globalCommandInvokers);
    }

    private void initInvokers(CommandExecutor command, HashMap<String, Pair<Method, CommandExecutor>> map) {
        Method[] declaredMethods = command.getClass().getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (!method.isAnnotationPresent(CommandExecutor.CommandEvent.class))
                continue;
            method.setAccessible(true);
            SlashCommandExecutor.CommandEvent annotation = method.getAnnotation(CommandExecutor.CommandEvent.class);
            String path = annotation.value();
            if(map.containsKey(path))
                throw new IllegalArgumentException("Command path '" + path + "' is already registered!");
            map.put(path, Pair.of(method, command));
        }
    }

    /**
     * Updates all registered commands to the guild.
     * @param guild The guild to update the commands to
     * @return The update action
     */
    public CommandListUpdateAction updateCommands(Guild guild) {
        return guild.updateCommands().addCommands(
                commands.stream()
                        .map(CommandExecutor::init)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Updates all registered global commands to the JDA instance.
     * @param jda The JDA instance to update the commands to
     * @return The update action
     */
    public CommandListUpdateAction updateGlobalCommands(JDA jda) {
        return jda.updateCommands().addCommands(
                globalCommands.stream()
                        .map(CommandExecutor::init)
                        .collect(Collectors.toList())
        );
    }

    @SubscribeEvent
    private void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        onGenericCommandInteraction(e);
    }

    @SubscribeEvent
    private void onGenericContextInteraction(@NotNull GenericContextInteractionEvent<?> e) {
        onGenericCommandInteraction(e);
    }

    @SubscribeEvent
    private void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent e) {
        for (CommandExecutor command : e.isGlobalCommand() ? globalCommands : commands)
            if (command instanceof SlashCommandExecutor slashCommand)
                slashCommand.onCommandAutoCompleteInteraction(e).suggest(e);
    }

    private void onGenericCommandInteraction(GenericCommandInteractionEvent e) {
        Pair<Method, CommandExecutor> pair = (e.isGlobalCommand() ? globalCommandInvokers : commandInvokers).get(e.getFullCommandName());
        if (pair == null)
            return;
        CommandResponseHandler responseHandler = createResponseHandler(e);
        responseHandler.catchExceptions(() -> {
            Method method = pair.getLeft();
            try {
                method.invoke(pair.getRight(), e, responseHandler);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                Throwable target = ex.getTargetException();
                if (target instanceof CommandException ce)
                    throw ce;
                throw new RuntimeException(target);
            }
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

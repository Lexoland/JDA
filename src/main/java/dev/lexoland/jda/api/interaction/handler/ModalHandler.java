package dev.lexoland.jda.api.interaction.handler;

import dev.lexoland.jda.api.LocalizationString;
import dev.lexoland.jda.api.interaction.executor.ModalExecutor;
import dev.lexoland.jda.api.interaction.response.ModalResponseHandler;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.BiFunction;

/**
 * Handles modal interactions. To use this api, you need to register it as a listener to your JDA instance.
 */
public class ModalHandler {
    @ApiStatus.Internal
    public static HashMap<String, Pair<ModalExecutor, LocalizationString>> modals = new HashMap<>();

    private final BiFunction<ModalInteractionEvent, LocalizationString, ModalResponseHandler> responseHandlerFactory;

    /**
     * Creates a new modal handler with a default response handler factory. These response handlers doesn't use logging channels. If you want to use logging channels, use {@link #ModalHandler(BiFunction)}.
     */
    public ModalHandler() {
        this((event, title) -> new ModalResponseHandler(event, title, null));
    }

    /**
     * Creates a new modal handler with a response handler factory. This can be used to set a custom response handler e.g. for logging channels.
     * @param responseHandlerFactory The factory to create a response handler.
     */
    public ModalHandler(BiFunction<ModalInteractionEvent, LocalizationString, ModalResponseHandler> responseHandlerFactory) {
        this.responseHandlerFactory = responseHandlerFactory;
    }

    @SubscribeEvent
    private void onModalInteraction(@NotNull ModalInteractionEvent event) {
        Pair<ModalExecutor, LocalizationString> executor = modals.get(event.getModalId());
        if(executor != null) {
            ModalResponseHandler responseHandler = createResponseHandler(event, executor.getRight());
            responseHandler.catchExceptions(() -> executor.getLeft().run(event, responseHandler));
        }

    }

    /**
     * Creates a new {@link ModalResponseHandler} for the given {@link ModalInteractionEvent}.
     *
     * @param event The event to create the response handler for.
     * @return The created response handler.
     */
    public ModalResponseHandler createResponseHandler(ModalInteractionEvent event, LocalizationString title) {
        return responseHandlerFactory.apply(event, title);
    }

    public static Modal.Builder modal(GenericInteractionCreateEvent event, ModalExecutor executor, LocalizationString title) {
        User user = event.getUser();
        modals.put(user.getId(), Pair.of(executor, title));
        return Modal.create(user.getId(), title.translate(event.getUserLocale()));
    }
}

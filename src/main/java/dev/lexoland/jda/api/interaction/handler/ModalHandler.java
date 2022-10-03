package dev.lexoland.jda.api.interaction.handler;

import dev.lexoland.jda.api.LocalizationString;
import dev.lexoland.jda.api.interaction.executor.ModalExecutor;
import dev.lexoland.jda.api.interaction.response.CommandResponseHandler;
import dev.lexoland.jda.api.interaction.response.ModalResponseHandler;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.BiFunction;

public class ModalHandler extends ListenerAdapter {
    @ApiStatus.Internal
    public static HashMap<String, Pair<ModalExecutor, LocalizationString>> modals = new HashMap<>();

    private final BiFunction<ModalInteractionEvent, LocalizationString, ModalResponseHandler> responseHandlerFactory;

    public ModalHandler() {
        this((event, title) -> new ModalResponseHandler(event, title, null));
    }

    public ModalHandler(BiFunction<ModalInteractionEvent, LocalizationString, ModalResponseHandler> responseHandlerFactory) {
        this.responseHandlerFactory = responseHandlerFactory;
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
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

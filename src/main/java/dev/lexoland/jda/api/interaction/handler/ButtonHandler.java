package dev.lexoland.jda.api.interaction.handler;

import dev.lexoland.jda.api.interaction.CommandException;
import dev.lexoland.jda.api.interaction.response.ButtonResponseHandler;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Handles a button interactions. To track button interactions, you have to annotate a method with {@link ButtonEvent} and register the class to the {@link net.dv8tion.jda.api.JDA} instance.
 */
public class ButtonHandler {

    private final Function<ButtonInteractionEvent, ButtonResponseHandler> responseHandlerFactory;
    private final HashMap<String, Pair<Env, Method>> buttonInvokers = new HashMap<>();

    /**
     * Creates a new button handler with a default response handler factory. These response handlers doesn't use logging channels. If you want to use logging channels, use {@link #ButtonHandler(Function)}.
     */
    public ButtonHandler() {
        this(event -> new ButtonResponseHandler(event, null));
    }

    /**
     * Creates a new button handler with a response handler factory. This can be used to set a custom response handler e.g. for logging channels.
     *
     * @param responseHandlerFactory The factory to create a response handler.
     */
    public ButtonHandler(Function<ButtonInteractionEvent, ButtonResponseHandler> responseHandlerFactory) {
        this.responseHandlerFactory = responseHandlerFactory;
        initInvokers();
    }

    private void initInvokers() {
        Method[] declaredMethods = getClass().getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (!method.isAnnotationPresent(ButtonEvent.class))
                continue;
            method.setAccessible(true);
            ButtonEvent annotation = method.getAnnotation(ButtonEvent.class);
            buttonInvokers.put(annotation.value(), Pair.of(annotation.env(), method));
        }
    }

    @SubscribeEvent
    public void onButtonInteraction(@NotNull ButtonInteractionEvent e) {
        Pair<Env, Method> pair = buttonInvokers.get(e.getComponentId());
        if(pair == null)
            return;
        Env env = pair.getLeft();
        if (env != Env.BOTH && ((e.isFromGuild() && env == Env.DM) || (!e.isFromGuild() && env == Env.GUILD)))
            return;
        Method method = pair.getRight();
        ButtonResponseHandler re = responseHandlerFactory.apply(e);
        re.catchExceptions(() -> {
            try {
                method.invoke(this, e, re);
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (InvocationTargetException ex) {
                Throwable target = ex.getTargetException();
                if (target instanceof CommandException ce)
                    throw ce;
                throw new RuntimeException(target);
            }
        });
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ButtonEvent {
        /**
         * The id of the button.
         *
         * @return The id
         */
        String value();

        Env env() default Env.BOTH;
    }

    public enum Env {
        GUILD, DM, BOTH
    }
}

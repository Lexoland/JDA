package dev.lexoland.jda.api.interaction.handler;

import dev.lexoland.jda.api.interaction.response.ButtonResponseHandler;
import dev.lexoland.jda.api.interaction.response.CommandResponseHandler;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Handles a button interactions. To track button interactions, you have to annotate a method with {@link ButtonEvent} and register the class to the {@link net.dv8tion.jda.api.JDA} instance.
 */
public class ButtonHandler {

    private final Function<ButtonInteractionEvent, ButtonResponseHandler> responseHandlerFactory;

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
    }

    @SubscribeEvent
    public void onButtonInteraction(@NotNull ButtonInteractionEvent e) {
        Method[] declaredMethods = getClass().getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (!method.isAnnotationPresent(ButtonEvent.class))
                continue;
            ButtonEvent annotation = method.getAnnotation(ButtonEvent.class);
            String id = annotation.value();
            String regex = annotation.regex();
            Env env = annotation.env();
            if(id.isEmpty() && regex.isEmpty())
                throw new IllegalArgumentException("ButtonEvent annotation must have a value or a regex.");
            if(env != Env.BOTH && ((e.isFromGuild() && env == Env.DM) || (!e.isFromGuild() && env == Env.GUILD)))
                continue;
            if((!id.isEmpty() && !id.equals(e.getComponentId())) || (!regex.isEmpty() && !e.getComponentId().matches(regex)))
                continue;
            ButtonResponseHandler re = responseHandlerFactory.apply(e);
            re.catchExceptions(() -> {
                try {
                    method.setAccessible(true);
                    method.invoke(this, e, re);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ButtonEvent {
        /**
         * The id of the button.
         * @return The id
         */
        String value() default "";

        String regex() default "";

        Env env() default Env.BOTH;
    }

    public enum Env {
        GUILD, DM, BOTH
    }
}

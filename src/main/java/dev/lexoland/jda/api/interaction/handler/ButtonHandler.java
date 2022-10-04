package dev.lexoland.jda.api.interaction.handler;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

/**
 * Handles a button interactions. To track button interactions, you have to annotate a method with {@link ButtonEvent} and register the class to the {@link net.dv8tion.jda.api.JDA} instance.
 */
public class ButtonHandler {

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
            if(env != Env.BOTH && (e.isFromGuild() && env == Env.DM || !e.isFromGuild() && env == Env.GUILD))
                continue;
            if((!id.isEmpty() && !id.equals(e.getComponentId())) || (!regex.isEmpty() && !e.getComponentId().matches(regex)))
                continue;
            try {
                method.invoke(this, e);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
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

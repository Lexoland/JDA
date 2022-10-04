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
            if (!annotation.id().equals(e.getComponentId()))
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
        String id();
    }
}

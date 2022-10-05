package dev.lexoland.jda.api.interaction.executor;

import dev.lexoland.jda.api.interaction.AutoCompleteHandler;
import dev.lexoland.jda.api.interaction.response.CommandResponseHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface SlashCommandExecutor extends CommandExecutor {

    SlashCommandData init();

    default void onSlashCommandInteraction(SlashCommandInteractionEvent e, CommandResponseHandler re) {
        Method[] declaredMethods = getClass().getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (!method.isAnnotationPresent(CommandEvent.class))
                continue;
            CommandEvent annotation = method.getAnnotation(CommandEvent.class);
            String path = annotation.value();
            if(!path.equals(e.getCommandPath()))
                continue;
            try {
                method.setAccessible(true);
                method.invoke(this, e, re);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }

    default AutoCompleteHandler onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent e) {
        return AutoCompleteHandler.NONE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface CommandEvent {
        String value();
    }

}

package dev.lexoland.jda.api.interaction.executor;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface CommandExecutor {

    CommandData init();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface CommandEvent {
        String value();
    }
}

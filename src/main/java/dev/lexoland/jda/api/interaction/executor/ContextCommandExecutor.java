package dev.lexoland.jda.api.interaction.executor;

import dev.lexoland.jda.api.interaction.response.CommandResponseHandler;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface ContextCommandExecutor extends CommandExecutor{

    CommandData init();

    default void onUserContextInteraction(UserContextInteractionEvent e, CommandResponseHandler re) {}

    default void onMessageContextInteraction(MessageContextInteractionEvent e, CommandResponseHandler re) {}

    default void onContextInteraction(GenericContextInteractionEvent<?> e, CommandResponseHandler re) {
        if(e instanceof MessageContextInteractionEvent e2)
            onMessageContextInteraction(e2, re);
        else if(e instanceof UserContextInteractionEvent e2)
            onUserContextInteraction(e2, re);
    }
}

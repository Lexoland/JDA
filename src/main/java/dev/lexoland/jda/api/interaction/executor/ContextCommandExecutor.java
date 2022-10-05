package dev.lexoland.jda.api.interaction.executor;

import dev.lexoland.jda.api.interaction.response.CommandResponseHandler;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface ContextCommandExecutor extends CommandExecutor {

    CommandData init();

}

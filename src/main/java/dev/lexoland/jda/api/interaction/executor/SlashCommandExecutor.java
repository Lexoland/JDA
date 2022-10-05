package dev.lexoland.jda.api.interaction.executor;

import dev.lexoland.jda.api.interaction.AutoCompleteHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface SlashCommandExecutor extends CommandExecutor {

    SlashCommandData init();

    default AutoCompleteHandler onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent e) {
        return AutoCompleteHandler.NONE;
    }

}

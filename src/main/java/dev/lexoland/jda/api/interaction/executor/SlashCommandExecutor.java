package dev.lexoland.jda.api.interaction.executor;

import dev.lexoland.jda.api.interaction.AutoCompleteHandler;
import dev.lexoland.jda.api.interaction.response.CommandResponseHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface SlashCommandExecutor extends CommandExecutor {

    SlashCommandData init();

    void onSlashCommandInteraction(SlashCommandInteractionEvent e, CommandResponseHandler re);

    default AutoCompleteHandler onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent e) {
        return AutoCompleteHandler.NONE;
    }

}

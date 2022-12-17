package dev.lexoland.jda.api.interaction.response;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dev.lexoland.jda.api.LocalizationManager.tl;

public class ButtonResponseHandler extends ResponseHandler {

    private final ButtonInteractionEvent event;

    public ButtonResponseHandler(@NotNull ButtonInteractionEvent event, @Nullable TextChannel loggingChannel) {
        super(loggingChannel);
        this.event = event;
    }

    public @NotNull ModalCallbackAction replyModal(@NotNull Modal modal) {
        log(0x3b9dff, "event.log.result.modal", new Object[0], new MessageEmbed[0]);
        return event.replyModal(modal);
    }

    @Override
    protected void log(int embedColor, String resTl, Object[] resValues, MessageEmbed[] embeds) {
        if (loggingChannel == null)
            return;
        DiscordLocale locale = event.getGuildLocale();
        User executor = event.getUser();

        List<MessageEmbed> embedsMapped = Arrays.stream(embeds)
                .map(embed -> new EmbedBuilder(embed).setColor(0x2F3136).build())
                .collect(Collectors.toList());
        embedsMapped.add(0, new EmbedBuilder()
                .setAuthor(executor.getAsTag(), null, executor.getEffectiveAvatarUrl())
                .setTitle(tl(locale, "event.log.button"))
                .setDescription("`" + getButtonName(event.getButton()) + "`")
                .addField(tl(locale, "event.log.result"), tl(locale, resTl, resValues), false)
                .setColor(embedColor)
                .build());
        loggingChannel.sendMessageEmbeds(embedsMapped).queue();
    }

    private String getButtonName(Button button) {
        EmojiUnion emoji = button.getEmoji();
        String name = (emoji != null ? emoji.getFormatted() + " " : "") + button.getLabel();
        return name + " [" + button.getId() + "]";
    }

    @Override
    public @NotNull ButtonInteractionEvent getEvent() {
        return event;
    }
}

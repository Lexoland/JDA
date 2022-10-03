package dev.lexoland.jda.api.interaction.response;

import dev.lexoland.jda.api.LocalizationString;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dev.lexoland.jda.api.LocalizationManager.tl;

public class ModalResponseHandler extends ResponseHandler {

    private final ModalInteractionEvent event;
    private final LocalizationString title;

    public ModalResponseHandler(@NotNull ModalInteractionEvent event, @NotNull LocalizationString title, @Nullable TextChannel loggingChannel) {
        super(loggingChannel);
        this.event = event;
        this.title = title;
    }

    @Override
    protected void log(int embedColor, String resTl, Object[] resValues, MessageEmbed[] embeds) {
        if(loggingChannel == null)
            return;
        DiscordLocale locale = event.getGuildLocale();
        User executor = event.getUser();

        List<MessageEmbed> embedsMapped = Arrays.stream(embeds)
                .map(embed -> new EmbedBuilder(embed).setColor(0x2F3136).build())
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("__**").append(title.translate(locale)).append("**__\n\n");
        for (ModalMapping value : event.getValues()) {
            sb.append("**- ").append(value.getId()).append("**").append(": ");
            sb.append("```").append(value.getAsString()).append("```\n");
        }

        embedsMapped.add(0, new EmbedBuilder()
                .setAuthor(executor.getAsTag(), null, executor.getEffectiveAvatarUrl())
                .setTitle(tl(locale, "event.log.modal"))
                .setDescription(sb.toString())
                .addField(tl(locale, "event.log.result"), tl(locale, resTl, resValues), false)
                .setColor(embedColor)
                .build());

        loggingChannel.sendMessageEmbeds(embedsMapped).queue();
    }

    @Override
    public @NotNull ModalInteractionEvent getEvent() {
        return event;
    }

    /**
     * @return The title
     */
    public @Nullable LocalizationString getTitle() {
        return title;
    }
}

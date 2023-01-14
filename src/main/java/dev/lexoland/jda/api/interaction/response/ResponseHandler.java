package dev.lexoland.jda.api.interaction.response;

import dev.lexoland.jda.api.API;
import dev.lexoland.jda.api.interaction.CommandException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;

import static dev.lexoland.jda.api.LocalizationManager.tl;

public abstract class ResponseHandler {

    protected final TextChannel loggingChannel;

    public ResponseHandler(TextChannel loggingChannel) {
        this.loggingChannel = loggingChannel;
    }

    /**
     * @return The event of this response handler
     */
    public abstract IReplyCallback getEvent();

    /**
     * Logs the response.
     *
     * @param embedColor The color of the embed message
     * @param resTl      The translation key of the response message
     * @param resValues  The values of the response message
     * @param embeds     The embeds of the response message
     */
    protected abstract void log(int embedColor, String resTl, Object[] resValues, MessageEmbed[] embeds);

    /**
     * Replies the interaction. The logging message will be marked as successful.
     *
     * @param tl     The translation key of the response message
     * @param values The values of the response message
     * @return The reply callback action
     */
    public @NotNull ReplyCallbackAction replySuccess(String tl, Object... values) {
        return reply(0x3b9dff, tl, values);
    }

    /**
     * Replies the interaction. The logging message will be marked as failed.
     *
     * @param tl     The translation key of the response message
     * @param values The values of the response message
     * @return The reply callback action
     */
    public @NotNull ReplyCallbackAction replyFail(String tl, Object... values) {
        return reply(0xff3b3b, tl, values);
    }

    /**
     * Sends a reply of a deferred interaction. The logging message will be marked as successful.
     *
     * @param hook   The interaction hook
     * @param tl     The translation key of the response message
     * @param values The values of the response message
     * @return The message edit action
     */
    public @NotNull WebhookMessageEditAction<Message> deferredReplySuccess(InteractionHook hook, String tl, Object... values) {
        return deferredReply(0x3b9dff, hook, tl, values);
    }

    /**
     * Sends a reply of a deferred interaction. The logging message will be marked as failed.
     *
     * @param hook   The interaction hook
     * @param tl     The translation key of the response message
     * @param values The values of the response message
     * @return The message edit action
     */
    public @NotNull WebhookMessageEditAction<Message> deferredReplyFail(InteractionHook hook, String tl, Object... values) {
        return deferredReply(0xff3b3b, hook, tl, values);
    }

    /**
     * Replies the interaction with embed messages. The logging message will be marked as successful.
     *
     * @param embeds The embed messages of the response
     * @return The reply callback action
     */
    public @NotNull ReplyCallbackAction replyEmbedsSuccess(MessageEmbed... embeds) {
        return replyEmbeds(0x3b9dff, embeds);
    }

    /**
     * Replies the interaction with embed messages. The logging message will be marked as failed.
     *
     * @param embeds The embed messages of the response
     * @return The reply callback action
     */
    public @NotNull ReplyCallbackAction replyEmbedsFail(MessageEmbed... embeds) {
        return replyEmbeds(0xff3b3b, embeds);
    }

    private @NotNull ReplyCallbackAction reply(int embedColor, String tl, Object[] values) {
        log(embedColor, tl, values, new MessageEmbed[0]);
        return getEvent().reply(tl(getEvent().getUserLocale(), tl, values));
    }

    private @NotNull WebhookMessageEditAction<Message> deferredReply(int embedColor, InteractionHook hook, String tl, Object[] values) {
        log(embedColor, tl, values, new MessageEmbed[0]);
        return hook.editOriginal(tl(getEvent().getUserLocale(), tl, values));
    }

    private @NotNull ReplyCallbackAction replyEmbeds(int embedColor, MessageEmbed[] embeds) {
        log(embedColor, "event.log.result.embed", new Object[0], embeds);
        return getEvent().replyEmbeds(Arrays.asList(embeds));
    }

    @ApiStatus.Internal
    public void catchExceptions(Runnable exec) {
        try {
            exec.run();
        } catch (CommandException ex) {
            replyFail(ex.getMessage(), ex.getValues())
                    .setAllowedMentions(Collections.emptyList())
                    .setEphemeral(true)
                    .queue();
        } catch (Exception ex) {
            API.LOGGER.error("An error occurred while executing an interaction", ex);
            String exception = ExceptionUtils.getStackTrace(ex);
            if (exception.length() > 4000)
                exception = exception.substring(0, 4000) + "...";
            getEvent().replyEmbeds(new EmbedBuilder()
                    .setTitle("Error")
                    .setColor(0xff0000)
                    .setDescription(exception)
                    .build()
            ).setEphemeral(true).setAllowedMentions(Collections.emptyList()).queue();
        }
    }

    /**
     * @return The logging channel or null if not set
     */
    public TextChannel getLoggingChannel() {
        return loggingChannel;
    }
}

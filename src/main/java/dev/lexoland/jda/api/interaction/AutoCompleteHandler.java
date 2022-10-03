package dev.lexoland.jda.api.interaction;

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

@FunctionalInterface
public interface AutoCompleteHandler {

    AutoCompleteHandler NONE = e -> { };

    void suggest(CommandAutoCompleteInteractionEvent e);

    static AutoCompleteHandler listString(Collection<? extends SuggestibleString> list) {
        return e -> {
            String value = e.getFocusedOption().getValue();
            e.replyChoices(list.stream()
                            .filter(o -> o.getChoiceName().toLowerCase(Locale.ROOT).startsWith(value))
                            .map(SuggestibleString::getAsChoice)
                            .collect(Collectors.toList()))
                    .queue();
        };
    }

    static AutoCompleteHandler listLong(Collection<? extends SuggestibleLong> list) {
        return e -> {
            String value = e.getFocusedOption().getValue();
            e.replyChoices(list.stream()
                            .filter(o -> o.getChoiceName().toLowerCase(Locale.ROOT).startsWith(value))
                            .map(SuggestibleLong::getAsChoice)
                            .collect(Collectors.toList()))
                    .queue();
        };
    }

    static AutoCompleteHandler listDouble(Collection<? extends SuggestibleDouble> list) {
        return e -> {
            String value = e.getFocusedOption().getValue().toLowerCase(Locale.ROOT);
            e.replyChoices(list.stream()
                            .filter(o -> o.getChoiceName().toLowerCase(Locale.ROOT).startsWith(value))
                            .map(SuggestibleDouble::getAsChoice)
                            .collect(Collectors.toList()))
                    .queue();
        };
    }

    interface Suggestible<V> {

        String getChoiceName();

        V getChoiceValue();

    }
    interface SuggestibleString extends Suggestible<String> {
        default Command.Choice getAsChoice() {
            return new Command.Choice(getChoiceName(), getChoiceValue());
        }
    }
    interface SuggestibleLong extends Suggestible<Long> {
        default Command.Choice getAsChoice() {
            return new Command.Choice(getChoiceName(), getChoiceValue());
        }
    }
    interface SuggestibleDouble extends Suggestible<Double> {
        default Command.Choice getAsChoice() {
            return new Command.Choice(getChoiceName(), getChoiceValue());
        }
    }

    record PresetColor(String name, String hex) implements SuggestibleString {

        @Override
        public String getChoiceName() {
            return getChoiceValue() + " | " + name;
        }

        @Override
        public String getChoiceValue() {
            return hex;
        }
    }
    PresetColor[] PRESET_COLORS = new PresetColor[] {
            new PresetColor("Red", "#ff5555"),
            new PresetColor("Green", "#55ff55"),
            new PresetColor("Blue", "#5555ff"),
            new PresetColor("Yellow", "#ffff55"),
            new PresetColor("Magenta", "#ff55ff"),
            new PresetColor("Purple", "#9c55ff"),
            new PresetColor("Aqua", "#55ffff")
    };

    static AutoCompleteHandler listPresetColors() {
        return e -> {
            String value = e.getFocusedOption().getValue().toLowerCase(Locale.ROOT);
            e.replyChoices(Arrays.stream(PRESET_COLORS)
                    .filter(c -> c.name().toLowerCase(Locale.ROOT).startsWith(value))
                    .map(SuggestibleString::getAsChoice)
                    .collect(Collectors.toList())
            ).queue();
        };
    }

    static AutoCompleteHandler listEmojis() {
        return e -> {
            String value = e.getFocusedOption().getValue().toLowerCase(Locale.ROOT);
            e.replyChoices(e.getGuild().getEmojiCache().stream()
                    .filter(emote -> emote.getName().toLowerCase(Locale.ROOT).startsWith(value))
                    .limit(25)
                    .sorted(Comparator.comparing(RichCustomEmoji::getName))
                    .map(emote -> new Command.Choice(emote.getName(), emote.getAsMention()))
                    .collect(Collectors.toList())
            ).queue();
        };
    }
}

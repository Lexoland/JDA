package dev.lexoland.jda.api;


import net.dv8tion.jda.api.interactions.DiscordLocale;

import static dev.lexoland.jda.api.LocalizationManager.tl;

public record LocalizationString(String key, Object... values) {

    public String translate(DiscordLocale locale) {
        return tl(locale, key, values);
    }

}

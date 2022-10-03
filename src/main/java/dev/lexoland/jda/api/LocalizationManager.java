package dev.lexoland.jda.api;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LocalizationManager implements LocalizationFunction {

    /**
     * Used for commands. To localize responses or other messages use {@link LocalizationManager#tl(DiscordLocale, String, Object...)}.
     */
    @ApiStatus.Internal
    public static LocalizationManager INSTANCE;

    /**
     * Initializes the {@link LocalizationManager} with the given {@link DiscordLocale}s.
     * The first {@link DiscordLocale} will be the fallback language.<br>
     * This have to be called before registering any commands.
     * @param commandBundle The language bundle for commands
     * @param generalBundle The language bundle for general messages
     * @param locales The {@link DiscordLocale}s to register
     */
    public static void init(String commandBundle, String generalBundle, DiscordLocale... locales) {
        INSTANCE = new LocalizationManager(commandBundle, generalBundle, locales);
    }

    private final List<Pair<DiscordLocale, ResourceBundle>> commandBundles;
    private final HashMap<DiscordLocale, ResourceBundle> generalBundles;
    private final DiscordLocale fallbackLocale;

    public LocalizationManager(String commandBundle, String generalBundle, DiscordLocale... locales) {
        Checks.notEmpty(locales, "locales");
        Checks.noneNull(locales, "locales");
        commandBundles = new ArrayList<>(locales.length);
        generalBundles = new HashMap<>(locales.length);
        fallbackLocale = locales[0];
        for (DiscordLocale locale : locales) {
            Locale javaLocale = Locale.forLanguageTag(locale.getLocale());
            ResourceBundle commandBundleResources = ResourceBundle.getBundle(commandBundle, javaLocale);
            commandBundles.add(Pair.of(locale, commandBundleResources));

            ResourceBundle generalBundleResources = ResourceBundle.getBundle(generalBundle, javaLocale);
            generalBundles.put(locale, generalBundleResources);
        }
    }

    // command localization
    @ApiStatus.Internal
    @NotNull
    @Override
    public Map<DiscordLocale, String> apply(@NotNull String localizationKey) {
        Map<DiscordLocale, String> map = new HashMap<>();
        for (Pair<DiscordLocale, ResourceBundle> pair : commandBundles) {
            ResourceBundle resourceBundle = pair.getRight();
            if (resourceBundle.containsKey(localizationKey))
                map.put(pair.getLeft(), resourceBundle.getString(localizationKey));
        }
        return map;
    }

    /**
     * Localizes the given key to the given {@link DiscordLocale}.
     * @param locale The {@link DiscordLocale} to localize to
     * @param key The key to localize
     * @param values The values to insert into the localized string
     * @throws IllegalStateException If the {@link LocalizationManager} is not initialized
     * @return The localized string
     */
    @NotNull
    public static String tl(@NotNull DiscordLocale locale, @NotNull String key, Object... values) {
        if(INSTANCE == null)
            throw new IllegalStateException("LanguageManager not initialized");
        ResourceBundle bundle = INSTANCE.generalBundles.get(locale);
        if(bundle == null)
            bundle = INSTANCE.generalBundles.get(INSTANCE.fallbackLocale);
        try {
            return bundle.getString(key).formatted(values);
        } catch (MissingResourceException e) {
            return key;
        }
    }
}

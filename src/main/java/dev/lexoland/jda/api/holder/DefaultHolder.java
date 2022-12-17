package dev.lexoland.jda.api.holder;

import dev.lexoland.jda.api.DataSerializable;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public abstract class DefaultHolder implements Holder, DataSerializable {

    private static final Logger LOGGER = JDALogger.getLog(JDA.class);
    private static final File GUILD_DIR = new File("guilds");

    protected final Guild guild;
    protected boolean dirty = false;

    public DefaultHolder(Guild guild) {
        this.guild = guild;
    }

    @Override
    public void onInitialized() {
        fromJson(load());
    }

    @Override
    public void onDestruct() {
        saveIfDirty();
    }

    protected File getSaveFile() {
        return new File(GUILD_DIR, guild.getId() + ".json");
    }

    protected DataObject load() {
        File file = getSaveFile();
        if (!file.exists())
            return DataObject.empty();

        try (FileInputStream in = new FileInputStream(file)) {
            return DataObject.fromJson(in);
        } catch (Exception e) {
            LOGGER.error("Failed to load guild '" + guild.getId() + "'", e);
            return DataObject.empty();
        }
    }

    public void save() {
        GUILD_DIR.mkdirs();

        try (FileOutputStream out = new FileOutputStream(getSaveFile())) {
            out.write(toJson().toJson());
            dirty = false;
        } catch (Exception e) {
            LOGGER.error("Failed to save guild '" + guild.getId() + "'", e);
        }
    }

    public void saveIfDirty() {
        if (dirty)
            save();
    }

    public Guild getGuild() {
        return guild;
    }

    public void dirty() {
        dirty = true;
    }
}
package dev.lexoland.jda.api;

public interface Holder {

    /**
     * Indicates that you joined a {@link net.dv8tion.jda.api.entities.Guild Guild} or the guild has been successfully initialized on startup of the JDA.
     */
    void onInitialized();

    /**
     * Indicates that you left a {@link net.dv8tion.jda.api.entities.Guild Guild} or the JDA is shutting down.
     */
    void onDestruct();

}

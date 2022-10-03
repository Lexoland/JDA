package dev.lexoland.jda.api.interaction.executor;

import dev.lexoland.jda.api.interaction.response.ModalResponseHandler;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

@FunctionalInterface
public interface ModalExecutor {

    void run(ModalInteractionEvent e, ModalResponseHandler re);

}

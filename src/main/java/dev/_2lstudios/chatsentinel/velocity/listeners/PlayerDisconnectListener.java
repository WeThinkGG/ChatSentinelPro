package dev._2lstudios.chatsentinel.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import dev._2lstudios.chatsentinel.shared.modules.GeneralModule;

public class PlayerDisconnectListener {
    private final GeneralModule generalModule;

    public PlayerDisconnectListener(GeneralModule generalModule) {
        this.generalModule = generalModule;
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        generalModule.removeNickname(event.getPlayer().getUsername());
    }
}

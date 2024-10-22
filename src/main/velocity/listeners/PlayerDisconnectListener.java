package dev._2lstudios.chatsentinel.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.modules.GeneralModule;

public class PlayerDisconnectListener {
    private final GeneralModule generalModule;
    private final ChatPlayerManager chatPlayerManager;
    private final ChatNotificationManager chatNotificationManager;

    public PlayerDisconnectListener(GeneralModule generalModule, ChatPlayerManager chatPlayerManager, ChatNotificationManager chatNotificationManager) {
        this.generalModule = generalModule;
        this.chatPlayerManager = chatPlayerManager;
        this.chatNotificationManager = chatNotificationManager;
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        generalModule.removeNickname(event.getPlayer().getUsername());

        Player player = event.getPlayer();
        ChatPlayer chatPlayer = chatPlayerManager.getPlayer(player);
        if (chatPlayer != null && chatNotificationManager.containsPlayer(chatPlayer)) {
            chatNotificationManager.removePlayer(chatPlayer);
        }
    }
}

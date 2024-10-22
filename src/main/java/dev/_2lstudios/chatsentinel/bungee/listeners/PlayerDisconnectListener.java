package dev._2lstudios.chatsentinel.bungee.listeners;

import dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.modules.GeneralModule;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerDisconnectListener implements Listener {
    private GeneralModule generalModule;
    private ChatPlayerManager chatPlayerManager;
    private ChatNotificationManager chatNotificationManager;

    public PlayerDisconnectListener(GeneralModule generalModule, ChatPlayerManager chatPlayerManager, ChatNotificationManager chatNotificationManager) {
        this.generalModule = generalModule;
        this.chatPlayerManager = chatPlayerManager;
        this.chatNotificationManager = chatNotificationManager;
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        generalModule.removeNickname(event.getPlayer().getName());

        ProxiedPlayer player = event.getPlayer();
        ChatPlayer chatPlayer = chatPlayerManager.getPlayer(player);
        if (chatPlayer != null && chatNotificationManager.containsPlayer(chatPlayer)) {
            chatNotificationManager.removePlayer(chatPlayer);
        }
    }
}

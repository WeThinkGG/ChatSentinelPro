package dev._2lstudios.chatsentinel.bukkit.listeners;

import dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import dev._2lstudios.chatsentinel.shared.modules.GeneralModule;

public class PlayerQuitListener implements Listener {
    private GeneralModule generalModule;
    private ChatPlayerManager chatPlayerManager;
    private ChatNotificationManager chatNotificationManager;

    public PlayerQuitListener(GeneralModule generalModule, ChatPlayerManager chatPlayerManager, ChatNotificationManager chatNotificationManager) {
        this.generalModule = generalModule;
        this.chatPlayerManager = chatPlayerManager;
        this.chatNotificationManager = chatNotificationManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        generalModule.removeNickname(event.getPlayer().getName());
        Player player = event.getPlayer();
        ChatPlayer chatPlayer = chatPlayerManager.getPlayer(player);

        if (chatPlayer != null && chatNotificationManager.containsPlayer(chatPlayer)) {
            chatNotificationManager.removePlayer(chatPlayer);
        }
    }
}

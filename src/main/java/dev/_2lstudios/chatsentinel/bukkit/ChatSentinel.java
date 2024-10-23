package dev._2lstudios.chatsentinel.bukkit;

import dev._2lstudios.chatsentinel.bukkit.utils.BlockerUtil;
import dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import dev._2lstudios.chatsentinel.bukkit.commands.ChatSentinelCommand;
import dev._2lstudios.chatsentinel.bukkit.listeners.AsyncPlayerChatListener;
import dev._2lstudios.chatsentinel.bukkit.listeners.PlayerJoinListener;
import dev._2lstudios.chatsentinel.bukkit.listeners.PlayerQuitListener;
import dev._2lstudios.chatsentinel.bukkit.listeners.ServerCommandListener;
import dev._2lstudios.chatsentinel.bukkit.modules.BukkitModuleManager;
import dev._2lstudios.chatsentinel.bukkit.utils.ConfigUtil;
import dev._2lstudios.chatsentinel.shared.chat.ChatEventResult;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.modules.CooldownModerationModule;
import dev._2lstudios.chatsentinel.shared.modules.GeneralModule;
import dev._2lstudios.chatsentinel.shared.modules.MessagesModule;
import dev._2lstudios.chatsentinel.shared.modules.ModerationModule;
import dev._2lstudios.chatsentinel.shared.modules.SyntaxModerationModule;

public class ChatSentinel extends JavaPlugin {
    private static ChatSentinel instance;
    private BukkitModuleManager moduleManager;
    private BlockerUtil blockerUtil;

    public static ChatSentinel getInstance() {
        return instance;
    }

    public static void setInstance(ChatSentinel instance) {
        ChatSentinel.instance = instance;
    }

    public BukkitModuleManager getModuleManager() {
        return moduleManager;
    }

    @Override
    public void onEnable() {
        setInstance(this);
        ConfigUtil configUtil = new ConfigUtil(this);
        Server server = getServer();

        moduleManager = new BukkitModuleManager(configUtil);
        blockerUtil = new BlockerUtil(); // Initialize BlockerUtil

        GeneralModule generalModule = moduleManager.getGeneralModule();
        ChatPlayerManager chatPlayerManager = new ChatPlayerManager();
        ChatNotificationManager chatNotificationManager = new ChatNotificationManager();
        PluginManager pluginManager = server.getPluginManager();

        pluginManager.registerEvents(new AsyncPlayerChatListener(chatPlayerManager, chatNotificationManager), this);
        pluginManager.registerEvents(new PlayerJoinListener(generalModule, chatPlayerManager, chatNotificationManager), this);
        pluginManager.registerEvents(new PlayerQuitListener(moduleManager.getGeneralModule(), chatPlayerManager, chatNotificationManager), this);
        pluginManager.registerEvents(new ServerCommandListener(chatPlayerManager, chatNotificationManager), this);

        getCommand("chatsentinel").setExecutor(new ChatSentinelCommand(chatPlayerManager, chatNotificationManager, moduleManager, server));

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (generalModule.needsNicknameCompile()) {
                generalModule.compileNicknamesPattern();
            }
        }, 20L, 20L);
    }

    public void dispatchCommmands(ModerationModule moderationModule, ChatPlayer chatPlayer, String[][] placeholders) {
        Server server = getServer();

        server.getScheduler().runTask(this, () -> {
            ConsoleCommandSender console = server.getConsoleSender();
            for (String command : moderationModule.getCommands(placeholders)) {
                server.dispatchCommand(console, command);
            }
        });

        chatPlayer.clearWarns();
    }

    public void dispatchNotification(ModerationModule moderationModule, String[][] placeholders, ChatNotificationManager chatNotificationManager) {
        Server server = getServer();
        String notificationMessage = moderationModule.getWarnNotification(placeholders);

        if (notificationMessage != null && !notificationMessage.isEmpty()) {
            for (ChatPlayer chatPlayer : chatNotificationManager.getAllPlayers()) {
                Player player = Bukkit.getPlayer(chatPlayer.getUniqueId());
                if (player != null) {
                    player.sendMessage(notificationMessage);
                }
            }
            server.getConsoleSender().sendMessage(notificationMessage);
        }
    }

    public String[][] getPlaceholders(Player player, ChatPlayer chatPlayer, ModerationModule moderationModule, String message) {
        String playerName = player.getName();
        int warns = chatPlayer.getWarns(moderationModule);
        int maxWarns = moderationModule.getMaxWarns();
        float remainingTime = moduleManager.getCooldownModule().getRemainingTime(chatPlayer, message);

        return new String[][]{
                {"%player%", "%message%", "%warns%", "%maxwarns%", "%cooldown%"},
                {playerName, message, String.valueOf(warns), String.valueOf(maxWarns), String.valueOf(remainingTime)}
        };
    }

    public void sendWarning(String[][] placeholders, ModerationModule moderationModule, Player player, String lang) {
        String warnMessage = moduleManager.getMessagesModule().getWarnMessage(placeholders, lang, moderationModule.getName());

        if (warnMessage != null && !warnMessage.isEmpty()) {
            player.sendMessage(warnMessage);
        }
    }

    public ChatEventResult processEvent(ChatPlayer chatPlayer, Player player, String originalMessage, ChatNotificationManager chatNotificationManager) {
        ChatEventResult finalResult = new ChatEventResult(originalMessage, false, false);
        MessagesModule messagesModule = moduleManager.getMessagesModule();
        String playerName = player.getName();
        String lang = chatPlayer.getLocale();
        ModerationModule[] moderationModulesToProcess = {
                moduleManager.getSyntaxModule(),
                moduleManager.getCapsModule(),
                moduleManager.getCooldownModule(),
                moduleManager.getFloodModule(),
                moduleManager.getBlacklistModule()
        };

        // Check for blocked domains and fonts before processing
        if (blockerUtil.isBlockedMessage(originalMessage)) {
            chatPlayer.addWarn(moduleManager.getBlacklistModule());
            String[][] placeholders = ChatSentinel.getInstance().getPlaceholders(player, chatPlayer, moduleManager.getBlacklistModule(), originalMessage);
            ChatSentinel.getInstance().sendWarning(placeholders, moduleManager.getBlacklistModule(), player, lang);
            return new ChatEventResult(originalMessage, true, true); // Cancel event
        }

        for (ModerationModule moderationModule : moderationModulesToProcess) {
            boolean isCommmand = originalMessage.startsWith("/");
            boolean isNormalCommmand = ChatSentinel.getInstance().getModuleManager().getGeneralModule()
                    .isCommand(originalMessage);
            if (!(moderationModule instanceof SyntaxModerationModule) &&
                    !(moderationModule instanceof CooldownModerationModule) &&
                    isCommmand &&
                    !isNormalCommmand) {
                continue;
            }

            String message = finalResult.getMessage();
            if (player.hasPermission(moderationModule.getBypassPermission())) {
                continue;
            }

            ChatEventResult result = moderationModule.processEvent(chatPlayer, messagesModule, playerName, message, lang);

            if (result != null) {
                chatPlayer.addWarn(moderationModule);
                String[][] placeholders = ChatSentinel.getInstance().getPlaceholders(player, chatPlayer, moderationModule, message);
                ChatSentinel.getInstance().sendWarning(placeholders, moderationModule, player, lang);

                if (moderationModule.hasExceededWarns(chatPlayer)) {
                    ChatSentinel.getInstance().dispatchCommmands(moderationModule, chatPlayer, placeholders);
                }

                ChatSentinel.getInstance().dispatchNotification(moderationModule, placeholders, chatNotificationManager);
                finalResult.setMessage(result.getMessage());

                if (result.isHide())
                    finalResult.setHide(true);
                if (result.isCancelled()) {
                    finalResult.setCancelled(true);
                    break;
                }
            }
        }

        return finalResult;
    }
}

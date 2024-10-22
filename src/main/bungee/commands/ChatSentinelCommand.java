package dev._2lstudios.chatsentinel.bungee.commands;

import dev._2lstudios.chatsentinel.bungee.modules.BungeeModuleManager;
import dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.modules.MessagesModule;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ChatSentinelCommand extends Command {
	private ChatPlayerManager chatPlayerManager;
	private ChatNotificationManager chatNotificationManager;
	private BungeeModuleManager moduleManager;
	private ProxyServer server;

	public ChatSentinelCommand(ChatPlayerManager chatPlayerManager, ChatNotificationManager chatNotificationManager, BungeeModuleManager moduleManager, ProxyServer server) {
		super("chatsentinel");
		this.chatPlayerManager = chatPlayerManager;
		this.chatNotificationManager = chatNotificationManager;
		this.moduleManager = moduleManager;
		this.server = server;
	}

	private void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(TextComponent.fromLegacyText(message));
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		MessagesModule messagesModule = moduleManager.getMessagesModule();
		String lang;
		ChatPlayer chatPlayer = null;

		if (sender instanceof ProxiedPlayer) {
			chatPlayer = chatPlayerManager.getPlayer(((ProxiedPlayer) sender));
			lang = chatPlayer.getLocale();
		} else {
			lang = "en";
		}

		if (sender.hasPermission("chatsentinel.admin")) {
			if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
				sendMessage(sender, messagesModule.getHelp(lang));
			} else if (args[0].equalsIgnoreCase("reload")) {
				moduleManager.reloadData();

				sendMessage(sender, messagesModule.getReload(lang));
			} else if (args[0].equalsIgnoreCase("clear")) {
				StringBuilder emptyLines = new StringBuilder();
				String newLine = "\n ";
				String[][] placeholders = { { "%player%" }, { sender.getName() } };

				for (int i = 0; i < 128; i++) {
					emptyLines.append(newLine);
				}

				emptyLines.append(messagesModule.getCleared(placeholders, lang));

				for (ProxiedPlayer player : server.getPlayers()) {
					sendMessage(player, emptyLines.toString());
				}
			} else if (args[0].equalsIgnoreCase("notify")) {
				if (sender instanceof ProxiedPlayer) {
					boolean notify = chatNotificationManager.containsPlayer(chatPlayer);

					if (notify) {
						chatNotificationManager.removePlayer(chatPlayer);
						sender.sendMessage(messagesModule.getNotifyDisabled(lang));
					} else {
						chatNotificationManager.addPlayer(chatPlayer);
						sender.sendMessage(messagesModule.getNotifyEnabled(lang));
					}
				} else {
					sender.sendMessage(messagesModule.getUnknownCommand(lang));
				}
			} else {
				sendMessage(sender, messagesModule.getUnknownCommand(lang));
			}
		} else {
			sendMessage(sender, messagesModule.getNoPermission(lang));
		}
	}
}

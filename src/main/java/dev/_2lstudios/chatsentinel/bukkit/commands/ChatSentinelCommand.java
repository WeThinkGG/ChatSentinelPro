package dev._2lstudios.chatsentinel.bukkit.commands;

import dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev._2lstudios.chatsentinel.bukkit.modules.BukkitModuleManager;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.modules.MessagesModule;

public class ChatSentinelCommand implements CommandExecutor {
	private ChatPlayerManager chatPlayerManager;
	private ChatNotificationManager chatNotificationManager;
	private BukkitModuleManager moduleManager;
	private Server server;

	public ChatSentinelCommand(ChatPlayerManager chatPlayerManager, ChatNotificationManager chatNotificationManager, BukkitModuleManager moduleManager, Server server) {
		this.chatPlayerManager = chatPlayerManager;
		this.chatNotificationManager = chatNotificationManager;
		this.moduleManager = moduleManager;
		this.server = server;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		MessagesModule messagesModule = moduleManager.getMessagesModule();
		String lang;
		ChatPlayer chatPlayer = null;

		if (sender instanceof Player) {
			chatPlayer = chatPlayerManager.getPlayer(((Player) sender));
			lang = chatPlayer.getLocale();
		} else {
			lang = "en";
		}

		if (sender.hasPermission("chatsentinel.admin")) {
			if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
				sender.sendMessage(messagesModule.getHelp(lang));
			} else if (args[0].equalsIgnoreCase("reload")) {
				moduleManager.reloadData();

				sender.sendMessage(messagesModule.getReload(lang));
			} else if (args[0].equalsIgnoreCase("notify")) {
				if (sender instanceof Player) {
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
			} else if (args[0].equalsIgnoreCase("clear")) {
				StringBuilder emptyLines = new StringBuilder();
				String newLine = "\n ";
				String[][] placeholders = { { "%player%" }, { sender.getName() } };

				for (int i = 0; i < 128; i++) {
					emptyLines.append(newLine);
				}

				emptyLines.append(messagesModule.getCleared(placeholders, lang));

				for (Player player : server.getOnlinePlayers()) {
					player.sendMessage(emptyLines.toString());
				}
			} else {
				sender.sendMessage(messagesModule.getUnknownCommand(lang));
			}
		} else {
			sender.sendMessage(messagesModule.getNoPermission(lang));
		}

		return true;
	}
}

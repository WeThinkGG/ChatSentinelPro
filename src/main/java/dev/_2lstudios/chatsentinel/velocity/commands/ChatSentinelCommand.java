package dev._2lstudios.chatsentinel.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.modules.MessagesModule;
import dev._2lstudios.chatsentinel.velocity.modules.VelocityModuleManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChatSentinelCommand implements SimpleCommand {
	private final ChatPlayerManager chatPlayerManager;
	private final ChatNotificationManager chatNotificationManager;
	private final VelocityModuleManager moduleManager;
	private final ProxyServer server;

	public ChatSentinelCommand(ChatPlayerManager chatPlayerManager, ChatNotificationManager chatNotificationManager, VelocityModuleManager moduleManager, ProxyServer server) {
		this.chatPlayerManager = chatPlayerManager;
		this.chatNotificationManager = chatNotificationManager;
		this.moduleManager = moduleManager;
		this.server = server;
	}

	private void sendMessage(CommandSource sender, String message) {
		sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
	}

	@Override
	public boolean hasPermission(final Invocation invocation) {
		return invocation.source().hasPermission("chatsentinel.admin");
	}

	@Override
	public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
		return CompletableFuture.completedFuture(Stream.of("help", "reload", "clear", "notify")
				.collect(Collectors.toCollection(ArrayList::new)));
	}

	@Override
	public void execute(final Invocation invocation) {
		MessagesModule messagesModule = moduleManager.getMessagesModule();
		String lang;
		ChatPlayer chatPlayer = null;
		CommandSource sender = invocation.source();
		String[] args = invocation.arguments();

		if (sender instanceof Player) {
			chatPlayer = chatPlayerManager.getPlayer((Player) sender);
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
                if (sender instanceof Player) {
					StringBuilder emptyLines = new StringBuilder();
					String newLine = "\n ";
					String[][] placeholders;
                    placeholders = new String[][]{ { "%player%" }, { ((Player) sender).getUsername() } };

					for (int i = 0; i < 128; i++) {
						emptyLines.append(newLine);
					}

					emptyLines.append(messagesModule.getCleared(placeholders, lang));

					for (Player player : server.getAllPlayers()) {
						sendMessage(player, emptyLines.toString());
					}
				}
			} else if (args[0].equalsIgnoreCase("notify")) {
				if (sender instanceof Player) {
					boolean notify = chatNotificationManager.containsPlayer(chatPlayer);

					if (notify) {
						chatNotificationManager.removePlayer(chatPlayer);
						sendMessage(sender, messagesModule.getNotifyDisabled(lang));
					} else {
						chatNotificationManager.addPlayer(chatPlayer);
						sendMessage(sender, messagesModule.getNotifyEnabled(lang));
					}
				} else {
					sendMessage(sender, messagesModule.getUnknownCommand(lang));
				}
			} else {
				sendMessage(sender, messagesModule.getUnknownCommand(lang));
			}
		} else {
			sendMessage(sender, messagesModule.getNoPermission(lang));
		}
	}
}

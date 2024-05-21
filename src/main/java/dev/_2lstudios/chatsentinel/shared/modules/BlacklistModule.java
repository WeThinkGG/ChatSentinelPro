package dev._2lstudios.chatsentinel.shared.modules;

import java.util.regex.Pattern;

import dev._2lstudios.chatsentinel.shared.chat.ChatEventResult;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.utils.PatternUtil;

public class BlacklistModule extends Module {
	private ModuleManager moduleManager;

	private boolean fakeMessage;
	private boolean hideWords;
  private boolean blockRawMessage;
	private Pattern pattern;

	public BlacklistModule(ModuleManager moduleManager) {
		this.moduleManager = moduleManager;
	}

	public void loadData(boolean enabled, boolean fakeMessage, boolean hideWords, int maxWarns,
        String warnNotification, String[] commands, String[] patterns, boolean blockRawMessage) {
		setEnabled(enabled);
		setMaxWarns(maxWarns);
		setWarnNotification(warnNotification);
		setCommands(commands);
		this.fakeMessage = fakeMessage;
		this.hideWords = hideWords;
		this.pattern = PatternUtil.compile(patterns);
		this.blockRawMessage = blockRawMessage;
	}

	public boolean isFakeMessage() {
		return this.fakeMessage;
	}

	public boolean isHideWords() {
		return this.hideWords;
	}

	public boolean isBlockRawMessage() {
		return this.blockRawMessage;
	}

	public Pattern getPattern() {
		return pattern;
	}

	@Override
	public ChatEventResult processEvent(ChatPlayer chatPlayer, MessagesModule messagesModule, String playerName,
			String message, String lang) {
		if (!isEnabled()) {
			return null;
		}

		boolean cancelled = false;
		boolean hide = false;

		GeneralModule generalModule = moduleManager.getGeneralModule();
		WhitelistModule whitelistModule = moduleManager.getWhitelistModule();

		String sanitizedMessage = message;

		// Filter the arguments of the commands
		if (sanitizedMessage.startsWith("/") && message.contains(" ")) {
			sanitizedMessage = sanitizedMessage.substring(message.indexOf(" "));
		}

		// Remove weird stuff
		if (generalModule.isSanitizeEnabled()) {
			sanitizedMessage = generalModule.sanitize(message);
		}

		// Remove names
		if (generalModule.isSanitizeNames()) {
			sanitizedMessage = generalModule.sanitizeNames(message);
		}

		// Remove whitelisted stuff
		if (whitelistModule.isEnabled()) {
			sanitizedMessage = whitelistModule.getPattern().matcher(message).replaceAll("");
		}

		if (pattern.matcher(sanitizedMessage).find()) {
			if (isFakeMessage()) {
				hide = true;
			} else if (isHideWords()) {
				message = pattern.matcher(message).replaceAll("***");
			} else if (isBlockRawMessage()) {
				cancelled = true;
			}

			return new ChatEventResult(message, cancelled, hide);
		}

		return null;
	}

	@Override
	public String getName() {
		return "Blacklist";
	}
}

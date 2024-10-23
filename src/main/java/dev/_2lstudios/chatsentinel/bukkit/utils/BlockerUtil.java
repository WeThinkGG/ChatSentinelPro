package dev._2lstudios.chatsentinel.bukkit.utils;

import java.util.Set;
import java.util.regex.Pattern;

public class BlockerUtil {
    private static final Set<String> blockedDomains = Set.of(
        ".com", ".net", ".org", ".fun", ".gg", ".io", ".xyz", ".biz", ".info", ".co", ".us", ".ca",
        ".uk", ".ru", ".de", ".fr", ".jp", ".cn", ".nl", ".br", ".eu", ".tv", ".live", ".me", ".dev",
        ".app", ".club", ".site", ".shop", ".tech", ".online", ".space", ".store", ".agency", ".email"
    );

    private static final Pattern urlPattern = Pattern.compile(
        "([a-zA-Z0-9]+\\.[a-zA-Z]{2,6})"
    );

    public boolean containsBlockedDomain(String message) {
        String lowerMessage = message.toLowerCase();
        var matcher = urlPattern.matcher(lowerMessage);
        while (matcher.find()) {
            String domain = matcher.group();
            for (String blockedDomain : blockedDomains) {
                if (domain.endsWith(blockedDomain)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsCustomFont(String message) {
        for (char c : message.toCharArray()) {
            if ((c < 32 || c > 126) && c != '§') {
                return true;
            }
        }
        return false;
    }

    public boolean isBlockedMessage(String message) {
        return containsBlockedDomain(message) || containsCustomFont(message);
    }
}

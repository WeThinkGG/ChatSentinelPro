package dev._2lstudios.chatsentinel.bukkit.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import dev._2lstudios.chatsentinel.bukkit.ChatSentinel;
import org.bukkit.configuration.file.FileConfiguration;

public class BlacklistFetcher {
    private Set<String> blacklistWords;
    private long lastFetchTime;
    private String blacklistURL;
    private long cacheDuration;
    
    public BlacklistFetcher() {
        this.blacklistWords = new HashSet<>();
        this.lastFetchTime = 0;
        
        // Load values from config
        FileConfiguration config = ChatSentinel.getInstance().getConfig();
        this.blacklistURL = config.getString("blacklist.url", "https://raw.githubusercontent.com/WeThinkGG/curesword/refs/heads/main/gg.txt");
        this.cacheDuration = config.getLong("blacklist.cache_duration", 60 * 60 * 1000);  // Default: 1 hour
    }

    public Set<String> getBlacklistWords() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFetchTime > cacheDuration) {
            fetchBlacklistWords();
        }
        return blacklistWords;
    }

    private void fetchBlacklistWords() {
        try {
            URL url = new URL(blacklistURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                blacklistWords.clear();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    blacklistWords.add(line.trim().toLowerCase());
                }

                reader.close();
                lastFetchTime = System.currentTimeMillis();
                ChatSentinel.getInstance().getLogger().log(Level.INFO, "Blacklist fetched and updated.");
            } else {
                ChatSentinel.getInstance().getLogger().log(Level.WARNING, "Failed to fetch blacklist. Response code: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            ChatSentinel.getInstance().getLogger().log(Level.SEVERE, "Error fetching blacklist: ", e);
        }
    }
}

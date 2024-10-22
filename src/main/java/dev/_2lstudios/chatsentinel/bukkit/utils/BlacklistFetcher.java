package dev._2lstudios.chatsentinel.bukkit.utils;

import dev._2lstudios.chatsentinel.bukkit.ChatSentinel;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlacklistFetcher {
    private Set<String> blacklistWords;
    private long lastFetchTime;
    private String blacklistURL;
    private long cacheDuration;
    private File blacklistFile;
    private FileConfiguration blacklistConfig;

    public BlacklistFetcher() {
        this.blacklistWords = new HashSet<>();
        this.lastFetchTime = 0;

        // Load config values
        FileConfiguration config = ChatSentinel.getInstance().getConfig();
        this.blacklistURL = config.getString("blacklist.url", "https://raw.githubusercontent.com/WeThinkGG/curesword/refs/heads/main/gg.txt");
        this.cacheDuration = config.getLong("blacklist.cache_duration", 60 * 60 * 1000); // 1 hour default

        // Initialize blacklist.yml
        blacklistFile = new File(ChatSentinel.getInstance().getDataFolder(), "blacklist.yml");
        if (!blacklistFile.exists()) {
            try {
                blacklistFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        blacklistConfig = YamlConfiguration.loadConfiguration(blacklistFile);
    }

    public Set<String> getBlacklistWords() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFetchTime > cacheDuration) {
            fetchBlacklistWords();
        } else {
            loadBlacklistFromFile();
        }
        return blacklistWords;
    }

    private void fetchBlacklistWords() {
        Logger logger = ChatSentinel.getInstance().getLogger();
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
                saveBlacklistToFile();
                logger.log(Level.INFO, "Blacklist fetched and saved to blacklist.yml.");
            } else {
                logger.log(Level.WARNING, "Failed to fetch blacklist. Response code: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching blacklist: ", e);
        }
    }

    private void saveBlacklistToFile() {
        try {
            blacklistConfig.set("blacklist.words", new HashSet<>(blacklistWords));
            blacklistConfig.save(blacklistFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBlacklistFromFile() {
        blacklistWords.clear();
        blacklistWords.addAll(blacklistConfig.getStringList("blacklist.words"));
    }
}

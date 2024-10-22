package dev._2lstudios.chatsentinel.velocity.utils;

import dev._2lstudios.chatsentinel.velocity.ChatSentinel;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigUtil {
	private final ChatSentinel plugin;

	public ConfigUtil(ChatSentinel plugin) {
		this.plugin = plugin;
	}

	public CommentedConfigurationNode get(String file) {
		try {
			Path dataDirectory =  plugin.getDataDirectory();
			final ConfigurationLoader<CommentedConfigurationNode> loader = YamlConfigurationLoader.builder().path(dataDirectory.resolve(file)).build();
			return loader.load();
		} catch (ConfigurateException e) {
            plugin.getLogger().error("An error occurred while trying to load {} config file.", file, e);
			return null;
		}
	}

	public void create(String file) {
		try {
			Path dataDirectory =  plugin.getDataDirectory();

			if (Files.notExists(dataDirectory)) {
				Files.createDirectory(dataDirectory);
			}

			Path configFile = dataDirectory.resolve(file);

			if (Files.notExists(configFile)) {
				InputStream inputStream = plugin.getClass().getClassLoader()
						.getResourceAsStream(file);

				if (inputStream != null) {
					Files.copy(inputStream, configFile);
					plugin.getLogger().info("File {} has been created!", configFile);
				} else {
					Files.createFile(configFile);
				}
			}
		} catch (IOException e) {
			plugin.getLogger().info("Unable to create configuration file!");
		}
	}
}
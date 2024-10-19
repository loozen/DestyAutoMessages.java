package ru.loozen.destyautomessages;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class DestyAutoMessages extends JavaPlugin implements Listener {

    private Map<String, AutoMessage> autoMessages = new HashMap<>();
    private FileConfiguration config;

    @Override
    public void onEnable() {
        createConfigFile();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Add any necessary cleanup code here
    }

    private void createConfigFile() {
        File configFolder = new File("plugins/DestyAutoMessages");
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        File configFile = new File("plugins/DestyAutoMessages/config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8))) {
                    writer.write("auto-messages:\n");
                    writer.write("  message1:\n");
                    writer.write("    message: \"§cПривет, это авто-сообщение!\"\n");
                    writer.write("    delay: 10\n");
                    writer.write("  message2:\n");
                    writer.write("    message: \"§aЭто второе авто-сообщение!\"\n");
                    writer.write("    delay: 20\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadConfig() {
        config = getConfig();
        ConfigurationSection configSection = config.getConfigurationSection("auto-messages");
        if (configSection != null) {
            for (String message : configSection.getKeys(false)) {
                String text = configSection.getString(message + ".message");
                int delay = configSection.getInt(message + ".delay");
                autoMessages.put(message, new AutoMessage(text, delay));
            }
        } else {
            getLogger().severe("Раздел 'auto-messages' не найден в файле конфигурации!");
            // Вы можете добавить здесь код для создания раздела "auto-messages" по умолчанию
            config.createSection("auto-messages");
            saveConfig();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        for (AutoMessage message : autoMessages.values()) {
            if (message.isTimeToSend()) {
                String text = message.getText().replace("&", "§"); // Заменить & на §
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('§', text));
                message.resetTimer();
            }
        }
    }

    private class AutoMessage {
        private String text;
        private int delay;
        private long lastSendTime;

        public AutoMessage(String text, int delay) {
            this.text = text;
            this.delay = delay;
            this.lastSendTime = System.currentTimeMillis();
        }

        public boolean isTimeToSend() {
            return System.currentTimeMillis() - lastSendTime >= delay * 1000;
        }

        public void resetTimer() {
            lastSendTime = System.currentTimeMillis();
        }

        public String getText() {
            return text;
        }
    }
}

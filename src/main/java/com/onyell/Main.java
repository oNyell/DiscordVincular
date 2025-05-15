package com.onyell;

/**
 * @author oNyell
 * GitHub: https://github.com/oNyell
 * Discord: onyell
 */

import com.onyell.commands.Commands;
import com.onyell.database.Database;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class Main extends Plugin {
    public static Main instance;

    private Configuration config;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Commands.setupCommands();
        Database.setupDatabase();
        this.getLogger().log(Level.FINE, "Plugin ativo com sucesso.");
    }

    @Override
    public void onDisable() {
        this.getLogger().log(Level.SEVERE, "Plugin desativo com sucesso.");
    }

    public static Main getInstance() {
        return instance;
    }
    public Configuration getConfig() {
        return config;
    }
    public void saveDefaultConfig() {
        for (String fileName : new String[]{"config"}) {
            File file = new File("plugins/DiscordVincular/" + fileName + ".yml");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                copyFile(Main.getInstance().getResourceAsStream(fileName + ".yml"), file);
            }

            try {
                if (fileName.equals("config")) {
                    this.config = YamlConfiguration.getProvider(YamlConfiguration.class).load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                }
            } catch (IOException ex) {
                this.getLogger().log(Level.WARNING, "Cannot load " + fileName + ".yml: ", ex);
            }
        }
    }
    /**
     * Copia um arquivo a partir de um InputStream.
     *
     * @param input O input para ser copiado.
     * @param out   O arquivo destinario.
     */
    public static void copyFile(InputStream input, File out) {
        FileOutputStream ou = null;
        try {
            ou = new FileOutputStream(out);
            byte[] buff = new byte[1024];
            int len;
            while ((len = input.read(buff)) > 0) {
                ou.write(buff, 0, len);
            }
        } catch (IOException ex) {
            getInstance().getLogger().log(Level.WARNING, "Failed at copy file " + out.getName() + "!", ex);
        } finally {
            try {
                if (ou != null) {
                    ou.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignore) {
            }
        }
    }
}
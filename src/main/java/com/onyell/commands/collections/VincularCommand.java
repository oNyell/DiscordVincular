package com.onyell.commands.collections;

/**
 * @author oNyell
 * GitHub: https://github.com/oNyell
 * Discord: onyell
 */

import com.onyell.commands.Commands;
import com.onyell.database.Database;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import javax.sql.rowset.CachedRowSet;

public class VincularCommand extends Commands {
    public VincularCommand() {
        super("discord");
    }

    private static final String CLIENT_ID = "1269331271977406514";
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final String AUTH_URL = "https://discord.com/api/oauth2/authorize";

    @Override
    public void execute(CommandSender sender, String[] args) {
        String playerName = sender.getName();

        if (args.length == 0) {
            TextComponent message = new TextComponent("\n§eEntre em nosso discord clicando ");
            TextComponent link = new TextComponent("§b§naqui\n");
            link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/5gMuJeHw6t"));
            message.addExtra(link);

            sender.sendMessage(message);
            return;
        }

        switch (args[0]) {
            case "vincular": {
                try {
                    String queryCheck = "SELECT `discord` FROM `profiles` WHERE `name` = ?";
                    CachedRowSet resultSet = Database.getInstance().query(queryCheck, playerName);

                    if (resultSet == null) {
                        String insertQuery = "INSERT INTO `profiles` (`name`, `discord`) VALUES (?, NULL)";
                        Database.getInstance().execute(insertQuery, playerName);
                        enviarLinkVinculacao(sender, playerName);
                    } else {
                        String discordId = resultSet.getString("discord");
                        
                        if (discordId == null) {
                            enviarLinkVinculacao(sender, playerName);
                        } else {
                            sender.sendMessage(TextComponent.fromLegacyText("§cSua conta já está vinculada, use /discord desvincular para desvinculá-la."));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            case "desvincular": {
                try {
                    String query = "SELECT `discord` FROM `profiles` WHERE `name` = ?";
                    CachedRowSet resultSet = Database.getInstance().query(query, playerName);

                    if (resultSet != null) {
                        String discordId = resultSet.getString("discord");

                        if (discordId != null) {
                            String updateQuery = "UPDATE `profiles` SET `discord` = NULL WHERE `name` = ?";
                            Database.getInstance().execute(updateQuery, playerName);

                            sender.sendMessage(TextComponent.fromLegacyText("§cO discord de ID: §b" + discordId + "§c foi desvinculado com sucesso."));
                        } else {
                            sender.sendMessage(TextComponent.fromLegacyText("§cVocê não possui uma conta vinculada, use /discord vincular para vincular."));
                        }
                    } else {
                        sender.sendMessage(TextComponent.fromLegacyText("§cVocê não possui uma conta vinculada, use /discord vincular para vincular."));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            default: {
                TextComponent message = new TextComponent("\n§eEntre em nosso discord clicando ");
                TextComponent link = new TextComponent("§b§naqui\n");
                link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/5gMuJeHw6t"));
                message.addExtra(link);

                sender.sendMessage(message);
                return;
            }
        }
    }
    
    private void enviarLinkVinculacao(CommandSender sender, String playerName) {
        String url = AUTH_URL + "?client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&response_type=code&scope=identify&state=" + playerName;

        TextComponent message = new TextComponent("\n§ePara vincular sua conta do Discord, clique ");
        TextComponent link = new TextComponent("§b§naqui\n");
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para vincular sua conta do Discord")));
        link.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        message.addExtra(link);

        sender.sendMessage(message);
    }
}

package me.fnfal113.sfheadconverter.Utils;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class Utils {

    public static String colorTranslator(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(Utils.colorTranslator("&6[SfHeadConverter] > &b" + message));
    }
}

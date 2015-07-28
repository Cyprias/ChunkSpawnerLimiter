package com.cyprias.ChunkSpawnerLimiter;

import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatUtils {

	private static final Pattern STRIP_COLOR_CODES = Pattern.compile("(?i)(&|" + String.valueOf(ChatColor.COLOR_CHAR) + ")[0-9A-FK-OR]", Pattern.CASE_INSENSITIVE);

	public static void broadcast(String format, Object... args) {
		broadcast(String.format(format, args));
	}

	public static void broadcast(String message) {
		message = replaceColorCodes(message);
		String[] messages = message.split("\n");
		String prefix = getChatPrefix();
		for (int cntr = 0; cntr < messages.length; cntr++)
			messages[cntr] = prefix + messages[cntr];
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(messages);
		}
		Bukkit.getConsoleSender().sendMessage(messages);
	}

	public static void broadcastRaw(String format, Object... args) {
		broadcastRaw(String.format(format, args));
	}

	public static void broadcastRaw(String message) {
		message = replaceColorCodes(message);
		String[] messages = message.split("\n");
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(messages);
		}
		Bukkit.getConsoleSender().sendMessage(messages);
	}

	public static void send(CommandSender sender, String message) {
		message = replaceColorCodes(message);
		String[] messages = message.split("\n");
		sender.sendMessage(messages);
	}

	public static void send(CommandSender sender, ChatColor color, String format, Object... args) {
		sender.sendMessage(color + getChatPrefix() + String.format(format, args));
	}

	public static void sendRaw(CommandSender sender, ChatColor color, String format, Object... args) {
		sender.sendMessage(color + String.format(format, args));
	}

	public static void sendCommandHelp(CommandSender sender, String line, org.bukkit.command.Command cmd) {
		sendCommandHelp(sender, "- ", line, cmd);
	}

	public static void sendCommandHelp(CommandSender sender, String prefix, String line, org.bukkit.command.Command cmd) {
		sendRaw(sender, ChatColor.GRAY, prefix + line, cmd.getLabel());
	}


	public static void error(CommandSender sender, String format, Object... args) {
		sender.sendMessage(getChatPrefix() + ChatColor.RED + String.format(format, args));
	}

	public static void errorRaw(CommandSender sender, String format, Object... args) {
		sender.sendMessage(ChatColor.RED + String.format(format, args));
	}

	public static final String getChatPrefix() {
		return replaceColorCodes(Plugin.chatPrefix);
	}

	// replace color codes with the colors
	public static final String replaceColorCodes(String mess) {
		return ChatColor.translateAlternateColorCodes('&', mess);
	}

	// get rid of color codes
	public static final String cleanColorCodes(String mess) {
		return STRIP_COLOR_CODES.matcher(mess).replaceAll("");
	}

}
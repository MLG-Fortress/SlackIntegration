package com.teej107.slack;

import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author teej107
 * @since Sep 12, 2015
 */
public class SlackSender implements HttpHandler
{
	private Slack plugin;
	private final String token, format;
	private HttpServer server;

	public SlackSender(Slack plugin, int port, String token, String format) throws IOException
	{
		this.plugin = plugin;
		this.token = token;
		this.format = format;
		String ip = Bukkit.getIp();
		if (ip == null || ip.isEmpty())
			ip = "0.0.0.0";
		InetSocketAddress address = new InetSocketAddress(ip, port);
		server = HttpServer.create(address, 0);
		server.createContext("/", this);
		server.setExecutor(Executors.newCachedThreadPool());
	}

	public void setEnabled(boolean enabled)
	{
		if (enabled)
		{
			server.start();
			debug("Slack outgoing webhook listener listening on " + server.getAddress().toString());
		}
		else
		{
			server.stop(0);
		}
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException
	{
		String requestMethod = exchange.getRequestMethod();
		if (requestMethod.equals("POST"))
		{
			handleSlackMessage(exchange);
		}
	}

	private void debug(String message)
	{
		plugin.getLogger().info(message);
	}

	Pattern leftAngleBracket = Pattern.compile("&lt;");
	Pattern rightAngleBracket = Pattern.compile("&lt;");
	Pattern ampersand = Pattern.compile("&lt;");

	private void handleSlackMessage(HttpExchange exchange) throws IOException
	{
		byte[] bytes = ByteStreams.toByteArray(exchange.getRequestBody());
		String fromBytes = new String(bytes, "UTF-8");
		String[] contents = fromBytes.split(Pattern.quote("&"));
		HashMap<String, String> map = new HashMap<>();
		for (String s : contents)
		{
			String[] array = s.split(Pattern.quote("="));
			if (array.length == 2)
			{
				map.put(array[0], array[1]);
			}
		}
		String token = map.get("token");
		if (token == null || !this.token.equals(token))
			return;

		String text = map.get("text");
		if (text == null)
			return;
		else
		{
			text = URLDecoder.decode(text, "UTF-8");
		}

		text = leftAngleBracket.matcher(text).replaceAll("<");
		text = rightAngleBracket.matcher(text).replaceAll(">");
		text = ampersand.matcher(text).replaceAll("&");

		//So all bots/integrations that post messages are reported as "slackbot" by Slack's outoging webhook.
		//Since I still want slackbot messages, I'm gonna make a hacky fix :P
		String username = map.get("user_name");
		if (username != null && username.equals("slackbot"))
		{
			if (plugin.isRecentlySent(text))
				return;
		}

		final String broadcast = String.format(ChatColor.translateAlternateColorCodes('&', format), username, text);
		final String finalName = username;
		final String finalText = text;
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				plugin.getServer().getPluginManager().callEvent(new MessageSentFromSlackEvent(finalName, finalText));
				Bukkit.broadcastMessage(broadcast);
			}
		}.runTask(plugin);
	}
}

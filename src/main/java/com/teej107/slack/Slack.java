package com.teej107.slack;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author teej107
 * @since Sep 11, 2015
 */
public class Slack extends JavaPlugin
{
	private static final List<String> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<String>());
	//private static final Map<String, Long> recentlySentMessages = new ConcurrentHashMap<>();
	private static final String NO_URL = "<url here>";

	private static final String WEBHOOK_URL = "webhook-url";
	private static final String PORT = "port";
	private static final String TOKEN = "token";
	private static final String CHANNELS = "channels";
	private static final String SLACK_TO_SERVER_FORMAT = "slack-to-server-format";

	private static final String SEND_ACHIEVEMENTS = "send-achievements";
	private static final String SEND_DEATHS = "send-deaths";

	private boolean enabled = false;
	private SlackReceiver slackReceiver;
	private SlackSender slackSender;

	@Override
	public void onEnable()
	{
		setupConfig();
		load();

		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new AsyncPlayerChatListener(this), this);
		pm.registerEvents(new ServerActivityListener(this), this);
		getCommand("slack").setExecutor(new SlackCommand(this));

		enabled = true;
	}

	@Override
	public void onDisable()
	{
		if (slackSender != null)
		{
			slackSender.setEnabled(false);
		}
		enabled = false;
	}

	@Override
	public void reloadConfig()
	{
		super.reloadConfig();
		if (enabled)
		{
			load();
		}
	}

	private void load()
	{
		if (slackSender != null)
		{
			slackSender.setEnabled(false);
		}
		if (getWebhookUrl().equals(NO_URL))
		{
			getLogger().warning("No URL specified! Go to the config and specify one, then run \'/slack reload\'");
			return;
		}
		try
		{
			slackReceiver = new SlackReceiver(new URL(getWebhookUrl()));
		}
		catch (IOException e)
		{
			getLogger().warning(e.getMessage());
		}
		try
		{
			slackSender = new SlackSender(this, getPort(), getToken(), getSlackToServerFormat());
			slackSender.setEnabled(true);
		}
		catch (IOException e)
		{
			getLogger().warning(e.getMessage());
		}
	}

	private void setupConfig()
	{
		getConfig().addDefault(WEBHOOK_URL, NO_URL);
		getConfig().addDefault(PORT, 25107);
		getConfig().addDefault(TOKEN, "<token here>");
		getConfig().addDefault(CHANNELS, EMPTY_LIST);
		getConfig().addDefault(SLACK_TO_SERVER_FORMAT, "(%s) %s");
		getConfig().addDefault(SEND_ACHIEVEMENTS, true);
		getConfig().addDefault(SEND_DEATHS, true);
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public boolean isSendAchievements()
	{
		return getConfig().getBoolean(SEND_ACHIEVEMENTS);
	}

	public void setSendAchievements(boolean b)
	{
		getConfig().set(SEND_ACHIEVEMENTS, b);
	}

	public boolean isSendDeaths()
	{
		return getConfig().getBoolean(SEND_DEATHS);
	}

	public void setSendDeaths(boolean b)
	{
		getConfig().set(SEND_DEATHS, b);
	}

	public String getWebhookUrl()
	{
		return getConfig().getString(WEBHOOK_URL);
	}

	public void setWebhookUrl(String s)
	{
		getConfig().set(WEBHOOK_URL, s);
	}

	public List<String> getChannels()
	{
		return getConfig().getStringList(CHANNELS);
	}

	public void setChannels(List<String> list)
	{
		getConfig().set(CHANNELS, list);
	}

	public int getPort()
	{
		return getConfig().getInt(PORT);
	}

	public void setPort(int n)
	{
		getConfig().set(PORT, n);
	}

	public String getToken()
	{
		return getConfig().getString(TOKEN);
	}

	public void setToken(String s)
	{
		getConfig().set(TOKEN, s);
	}

	public String getSlackToServerFormat()
	{
		return getConfig().getString(SLACK_TO_SERVER_FORMAT);
	}

	public void setSlackToServerFormat(String s)
	{
		getConfig().set(SLACK_TO_SERVER_FORMAT, s);
	}

	public void sendToSlack(CommandSender sender, String text)
	{
		JSONObject json = new JSONObject();
		if (text == null || text.isEmpty())
			return;
		text = ChatColor.stripColor(text);
		json.put("text", text);
		json.put("username", sender.getName());
		if(sender instanceof Player)
		{
			json.put("icon_url", "https://minotar.net/avatar/" + sender.getName() + ".png");
		}
		//recentlySentMessages.put(text, System.currentTimeMillis());
		for (String channel : getChannels())
		{
			json.put("channel", channel);
			slackReceiver.send(json.toJSONString());
		}
	}

	//yea yea (semi) copy paste I got it
	public void sendToSlack(String name, String text, boolean avatar)
	{
		JSONObject json = new JSONObject();
		if (text == null || text.isEmpty())
			return;
		text = ChatColor.stripColor(text);
		json.put("text", text);
		json.put("username", name);
		json.put("mrkdwn", false);
		json.put("parse", "full");
		if(avatar)
		{
			json.put("icon_url", "https://minotar.net/avatar/" + name + ".png");
		}

		//addRecentlySentMessage(text);

		for (String channel : getChannels())
		{
			json.put("channel", channel);
			slackReceiver.send(json.toJSONString());
		}
	}

	//private Pattern username = Pattern.compile("(@U)\\w+");
	//private Pattern channel = Pattern.compile("(#C)\\w+");
	//private Pattern angleBracketsAmpersand = Pattern.compile("[<>&]");
	//private Pattern whatever = Pattern.compile("<(.*?)>");
	//private Pattern tags = Pattern.compile("[@#]\\w+");
	//private Pattern url = Pattern.compile("(http)\\w+");

//	private void addRecentlySentMessage(String message)
//	{
//		message = ChatColor.stripColor(message);
//		message = whatever.matcher(message).replaceAll("");
//		message = tags.matcher(message).replaceAll("");
//		recentlySentMessages.put(message, System.currentTimeMillis());
//	}

//	public boolean isRecentlySent(String message)
//	{
//		message = ChatColor.stripColor(message);
//		message = whatever.matcher(message).replaceAll("");
//		message = tags.matcher(message).replaceAll("");
//
//		//Cleanup expired values. One alternative to this is to schedule a task to remove each time we send a message.
//		long currentTime = System.currentTimeMillis();
//		Iterator<String> sentMessagesIterator = recentlySentMessages.keySet().iterator();
//		while (sentMessagesIterator.hasNext())
//		{
//			String key = sentMessagesIterator.next();
//			if (currentTime - 5000L > recentlySentMessages.get(key))
//				recentlySentMessages.remove(key);
//		}
//
//		return recentlySentMessages.containsKey(message);
//	}
}

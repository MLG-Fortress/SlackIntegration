package com.teej107.slack;

import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * @author teej107
 * @since Sep 12, 2015
 */
public class AsyncPlayerChatListener implements Listener
{
	private Slack plugin;

	public AsyncPlayerChatListener(Slack plugin)
	{
		this.plugin = plugin;
	}
	GriefPrevention gp = (GriefPrevention)Bukkit.getPluginManager().getPlugin("GriefPrevention");
	DataStore ds = gp.dataStore;

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onAsyncChat(AsyncPlayerChatEvent event)
	{
		//If a player is softmuted in GriefPrevention, ignore
		if (ds.isSoftMuted(event.getPlayer().getUniqueId()))
			return;

		//If a player's spam is muted by GriefPrevention, ignore
		if (Bukkit.getOnlinePlayers().size() > 1) //Impossible to tell if GP is filtering spam when only one player is on
			if (event.getRecipients().size() < 2)
				return;

		plugin.sendToSlack(event.getPlayer(), event.getMessage());
	}
}

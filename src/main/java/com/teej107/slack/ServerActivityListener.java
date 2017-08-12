package com.teej107.slack;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author teej107
 * @since Sep 12, 2015
 */
public class ServerActivityListener implements Listener
{
    private Slack plugin;

    public ServerActivityListener(Slack plugin)
    {
        this.plugin = plugin;
    }

    private static String normalize(String string)
    {
        StringBuilder sb = new StringBuilder();
        for (String s : string.split(Pattern.quote("_")))
        {
            sb.append(s.charAt(0)).append(s.substring(1).toLowerCase()).append(" ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onJoin(PlayerJoinEvent event)
    {
        String joinMessage = event.getJoinMessage();
        if (joinMessage == null || joinMessage.isEmpty())
            joinMessage = event.getPlayer().getName() + " IZ BAK 4 MOAR MEINKRAFT!!!!!!1111111111!!!1";
        plugin.sendToSlack(SlackCommandSender.getInstance(), joinMessage);
    }

    Set<Player> kickedPlayers = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onQuit(PlayerQuitEvent event)
    {
        if (kickedPlayers.remove(event.getPlayer()))
            return;
        String quitMessage = event.getQuitMessage();
        if (quitMessage == null || quitMessage.isEmpty())
            quitMessage = event.getPlayer().getName() + " left us in loneliness :c";
        plugin.sendToSlack(SlackCommandSender.getInstance(), quitMessage);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onKick(PlayerKickEvent event)
    {
        String quitMessage = event.getPlayer().getName() + " was kicked for " + event.getReason();
        plugin.sendToSlack(SlackCommandSender.getInstance(), quitMessage);
        kickedPlayers.add(event.getPlayer());
    }
}
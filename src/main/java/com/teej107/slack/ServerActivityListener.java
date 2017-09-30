package com.teej107.slack;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
        if (event.getPlayer().hasPlayedBefore())
            return;

        String joinMessage = event.getJoinMessage();
        if (joinMessage == null || joinMessage.isEmpty())
            joinMessage = "*A wild `" + event.getPlayer().getName() + "` has appeared!*";
        plugin.sendToSlack(SlackCommandSender.getInstance(), joinMessage);
    }

    private Set<Player> kickedPlayers = new HashSet<>();
    private Set<Player> playerSentMessage = Collections.newSetFromMap(new ConcurrentHashMap<Player, Boolean>());

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onQuit(PlayerQuitEvent event)
    {
        if (kickedPlayers.remove(event.getPlayer()) || !playerSentMessage.remove(event.getPlayer()))
            return;

        String quitMessage = event.getQuitMessage();
        if (quitMessage == null || quitMessage.isEmpty())
            quitMessage = "`" + event.getPlayer().getName() + "` left";
        plugin.sendToSlack(SlackCommandSender.getInstance(), quitMessage);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onKick(PlayerKickEvent event)
    {
        String quitMessage = event.getPlayer().getName() + " left bcuz " + event.getReason();
        plugin.sendToSlack(SlackCommandSender.getInstance(), quitMessage);
        kickedPlayers.add(event.getPlayer());
        playerSentMessage.remove(event.getPlayer()); //cleanup
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onChat(AsyncPlayerChatEvent event)
    {
        playerSentMessage.add(event.getPlayer());
    }
}
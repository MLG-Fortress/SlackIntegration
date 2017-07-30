package com.teej107.slack;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created on 7/29/2017.
 *
 * @author RoboMWM
 */
public class MessageSentFromSlackEvent extends Event
{
    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private String username;
    private String message;

    MessageSentFromSlackEvent(String name, String message)
    {
        this.username = name;
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public String getUsername()
    {
        return username;
    }
}

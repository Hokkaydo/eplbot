package com.github.hokkaydo.eplbot.module.points;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PointsListener extends ListenerAdapter {

    private PointsProcessor processor;
    private final Map<User, Long> times = new HashMap<>();



    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (processor == null) {
            processor = new PointsProcessor(event.getGuild().getIdLong());
        }
        if (event.getAuthor().isBot()) return;
        String message = event.getMessage().getContentRaw();
        User user = event.getAuthor();
        if (times.containsKey(user)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - times.get(user) > TimeUnit.MINUTES.toMillis(3) && message.length() > 3) {
                times.put(user, currentTime);
                processor.addPoints(user.getName(), 1);
                processor.addPoints(STR."role_\{processor.getRole(user.getName())}", 1);
            }
        } else {
            if (message.length() > 3) {
            times.put(user, System.currentTimeMillis());
            processor.addPoints(user.getName(), 1);
            processor.addPoints(STR."role_\{processor.getRole(user.getName())}", 1);
            }
        }

    }
}


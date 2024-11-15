package com.github.hokkaydo.eplbot.module.menu;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.MessageUtil;
import com.github.hokkaydo.eplbot.configuration.Config;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MenuSender implements MenuRetriever{

    private static final String MENU_URL = "https://uclouvain.be/fr/decouvrir/resto-u/le-galilee-self.html";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Long guildId;

    MenuSender(Long guildId)  {
        this.guildId = guildId;
    }

    public void stop() {
        scheduler.shutdown();
    }

    public void start() {
        long delay = calculateDelayUntilNextMonday();
        scheduler.scheduleAtFixedRate(this::sendMenu, delay, 7 * 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
    }

    private long calculateDelayUntilNextMonday() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonday = now.with(TemporalAdjusters.next(java.time.DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return Duration.between(now, nextMonday).toMillis();
    }

    private void sendMenu() {
        TextChannel channel = Main.getJDA().getTextChannelById(Config.getGuildVariable(guildId, "MENU_CHANNEL_ID"));
        if(channel == null) {
            MessageUtil.sendAdminMessage("Menu channel not found", guildId);
            return;
        }
        retrieveMenu().ifPresent(menu -> {
            try {
                channel.sendMessage(menu).queue();
            } catch (ConcurrentModificationException e) {
                Main.LOGGER.log(Level.WARNING, "[MenuCommand] An error occurred while trying to send the menu", e);
            }
        });
    }

    @Override
    public Optional<String> retrieveMenu() {
        try {
            URL url = URI.create(MENU_URL).toURL();
            return Jsoup.parse(url, 10000).select("img")
                    .stream()
                    .filter(element -> element.attr("src").contains("cms-editors-resto-u/"))
                    .skip(2)
                    .findFirst()
                    .map(element -> {
                        String imageUrl = element.attr("src");
                        return STR."https://\{imageUrl.replace("//", "")}";
                    });
        } catch (IOException e) {
            Main.LOGGER.log(Level.WARNING, "[MenuCommand] An error occurred while trying to parse the URL", e);
            return Optional.empty();
        }
    }

}

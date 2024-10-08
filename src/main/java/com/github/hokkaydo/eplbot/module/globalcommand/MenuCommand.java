package com.github.hokkaydo.eplbot.module.globalcommand;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

public class MenuCommand implements Command {

    private static final String MENU_URL = "https://uclouvain.be/fr/decouvrir/resto-u/le-galilee-self.html";
    @Override
    public void executeCommand(CommandContext context) {
        try {
            URL url = URI.create(MENU_URL).toURL();
            Jsoup.parse(url, 10000).select("img")
                    .stream()
                    .filter(element -> element.attr("src").contains("cms-editors-resto-u/"))
                    .findFirst()
                    .ifPresentOrElse(element -> {
                        String imageUrl = element.attr("src");
                        context.replyCallbackAction().setContent(STR."https://\{imageUrl.replace("//", "")}").queue();
                    }, () -> context.replyCallbackAction().setContent(Strings.getString("MENU_COMMAND_NOT_FOUND")).queue());
        } catch (IOException e) {
            context.replyCallbackAction().setContent(Strings.getString("ERROR_OCCURRED")).queue();
            Main.LOGGER.log(Level.WARNING, "[MenuCommand] An error occurred while trying to parse the URL", e);
        }
    }

    @Override
    public String getName() {
        return "menu";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("MENU_COMMAND_DESCRIPTION");
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public boolean ephemeralReply() {
        return false;
    }

    @Override
    public boolean validateChannel(MessageChannel channel) {
        return true;
    }

    @Override
    public boolean adminOnly() {
        return false;
    }

    @Override
    public Supplier<String> help() {
        return () -> Strings.getString("MENU_COMMAND_HELP");
    }

}

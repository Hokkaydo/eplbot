package com.github.hokkaydo.eplbot.module.eplcommand;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

public class FrameworkCommand implements Command {

    private static final String FRAMEWORK_FILE = "framework.pdf";
    private final Logger logger;

    FrameworkCommand(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void executeCommand(CommandContext context) {
        context.replyCallbackAction().setContent(Strings.getString("COMMAND_FRAMEWORK_DO_NOT_FORGET")).setFiles(FileUpload.fromData(getFramework(), FRAMEWORK_FILE)).queue();
    }

    private byte[] getFramework() {
        try(InputStream framework = getClass().getClassLoader().getResourceAsStream(FRAMEWORK_FILE)) {
            if (framework == null) {
                logger.error("Could not find framework.pdf");
                return new byte[0];
            }
            return framework.readAllBytes();
        } catch (IOException e) {
            logger.error("Could not read framework.pdf", e);
            return new byte[0];
        }
    }

    @Override
    public String getName() {
        return "framework";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("COMMAND_FRAMEWORK_DESCRIPTION");
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return List.of();
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
        return () -> Strings.getString("COMMAND_FRAMEWORK_HELP");
    }

}

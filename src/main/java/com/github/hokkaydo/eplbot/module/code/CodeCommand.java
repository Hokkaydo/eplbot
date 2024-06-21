package com.github.hokkaydo.eplbot.module.code;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import com.github.hokkaydo.eplbot.module.code.java.JavaRunner;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;

public class CodeCommand extends ListenerAdapter implements Command {
    private static final Map<String, Runner> RUNNER_MAP;
    static {
        RUNNER_MAP = Map.of("java", new JavaRunner());
    }

    @Override
    public void executeCommand(CommandContext context) {
        Main.LOGGER.log(Level.INFO,"code command called :)");
        if (context.options().size() <= 1) {
            context.interaction().replyModal(Modal.create(STR."\{context.author().getId()}-code_submission","Execute du code")
                                                     .addActionRow(TextInput.create("body", "Code", TextInputStyle.PARAGRAPH).setPlaceholder("Code").setRequired(true).build())
                                                     .build()).queue();
        } else {
            Main.LOGGER.log(Level.INFO,"function not implemented yet");
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        Main.LOGGER.log(Level.INFO,"modal received");
        Main.LOGGER.log(Level.INFO,event.getModalId());
    }

    @Override
    public String getName() {
        return "compile";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("COMMAND_CODE_DESCRIPTION");
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
            new OptionData(OptionType.STRING, "language", Strings.getString("COMMAND_CODE_LANG_OPTION_DESCRIPTION"), true)
                .addChoice("java", "java"),
            new OptionData(OptionType.ATTACHMENT, "file", Strings.getString("COMMAND_CODE_FILE_OPTION_DESCRIPTION"), false)

        );
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
        return () -> Strings.getString("COMMAND_CODE_HELP");
    }
}

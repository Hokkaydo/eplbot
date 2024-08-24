package com.github.hokkaydo.eplbot.module.code.command;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import com.github.hokkaydo.eplbot.configuration.Config;
import com.github.hokkaydo.eplbot.module.code.GlobalProcessIdManager;
import com.github.hokkaydo.eplbot.module.code.Runner;
import com.github.hokkaydo.eplbot.module.code.c.CRunner;
import com.github.hokkaydo.eplbot.module.code.java.JavaRunner;
import com.github.hokkaydo.eplbot.module.code.python.PythonRunner;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CodeCommand extends ListenerAdapter implements Command {

    static GlobalProcessIdManager IDMANAGER = new GlobalProcessIdManager();
    private final static String INPUT_FILENAME = "input.txt";
    private final PerformResponse response = new PerformResponse(); // The class handling the response (sending trough discord etc. )
    private static final Map<String, Class<? extends Runner>> RUNNERMAP;
    /* /!\
     * This is where to put every new language added
     * /!\
     */
    static {
        RUNNERMAP = Map.of(
                "java", JavaRunner.class,
                "python", PythonRunner.class,
                "c", CRunner.class
        );
    }

    private final long guildId;
    public CodeCommand(long guildId) {
        this.guildId = guildId;
    }

    /**
     * @param type the language type
     * @return a new Runner for the code to run on
     */
    private Runner instantiateRunner(String type) {
        Class<? extends Runner> runnerClass = RUNNERMAP.get(type);
        try {
            return runnerClass.getDeclaredConstructor(String.class).newInstance(String.valueOf(IDMANAGER.getNextNumber()));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void executeCommand(CommandContext context) {

        if (context.options().size() <= 1) {
            // No file given
            String currentLang = context.options().getFirst().getAsString();
            context.interaction().replyModal(Modal.create(STR."\{context.author().getId()}-code_submission-\{currentLang}","Execute du code")
                                                     .addActionRow(TextInput.create("body", "Code", TextInputStyle.PARAGRAPH).setPlaceholder("Code").setRequired(true).build())
                                                     .build()).queue();
            return;
        }
        context.replyCallbackAction().setContent(STR."Processing since: <t:\{Instant.now().getEpochSecond()}:R>").setEphemeral(false).queue();
        context.options()
            .get(1)
            .getAsAttachment()
            .getProxy()
            .downloadToFile(new File((INPUT_FILENAME)))
            .thenAcceptAsync(file -> {
                String code = readFromFile(file,context.channel()).orElse(null);
                if (code == null){return;}
                Runner runner = instantiateRunner(context.options().getFirst().getAsString());
                CompletableFuture<Pair<String, Integer>> futureResult = CompletableFuture.supplyAsync(() ->
                    runner.run(code, Config.getGuildVariable(
                        Objects.requireNonNull(context.interaction().getGuild()).getIdLong(),
                        "COMMAND_CODE_TIMELIMIT"
                    ))
                );

                futureResult.thenAccept(result -> {
                    response.sendSubmittedCode(context.channel(), code, context.options().getFirst().getAsString());
                    response.sendResult(context.channel(), result.getLeft(), result.getRight());
                    if (file != null && !file.delete()) {
                        Main.LOGGER.log(Level.INFO, "File not deleted");
                    }
                });
            })
            .exceptionally(t -> {
                context.channel().sendMessage(STR."""
                    \{Strings.getString("COMMAND_CODE_UNEXPECTED_ERROR")}
                    The error is :\{t.getMessage()}""").queue();
                return null;
            });

    }

    /**
     * @param file the file with data to be read from
     * @param textChannel the channel of interaction, in case of an error
     * @return the data of the file
     */
    private Optional<String> readFromFile(File file, MessageChannel textChannel) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return Optional.of(reader.lines().collect(Collectors.joining(System.lineSeparator())));
        } catch (IOException e) {
            textChannel.sendMessage(Strings.getString("COMMAND_CODE_INACCESSIBLE_FILE")).queue();
            return Optional.empty();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        // Check for a valid modal
        if(event.getGuild() == null || event.getGuild().getIdLong() != guildId) return;
        if(event.getInteraction().getType() != InteractionType.MODAL_SUBMIT || !event.getModalId().contains("-code_submission-")) return;
        Optional<ModalMapping> body = Optional.ofNullable(event.getInteraction().getValue("body"));
        Guild guild = event.getGuild();
        if(body.isEmpty() || guild == null){
            event.getMessageChannel().sendMessage(Strings.getString("COMMAND_CODE_NO_LANGUAGE_SPECIFIED")).queue();
            return;
        }
        Integer runTimeout = Config.getGuildVariable(guild.getIdLong(), "COMMAND_CODE_TIMELIMIT");
        event.getInteraction().reply(STR."Processing since: <t:\{Instant.now().getEpochSecond()}:R>").queue();
        String languageOption = event.getModalId().split("-")[2];
        String code = Objects.requireNonNull(body.get().getAsString());
        Runner runner = instantiateRunner(languageOption);
        CompletableFuture<Pair<String, Integer>> futureResult = CompletableFuture.supplyAsync(() ->
            runner.run(code, runTimeout)
        );
        futureResult.thenAccept(result -> {
            response.sendSubmittedCode(event.getChannel(),code,languageOption);
            response.sendResult(event.getChannel(), result.getLeft(),result.getRight());
        });


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
        OptionData codeOptions = new OptionData(OptionType.STRING, "language", Strings.getString("COMMAND_CODE_LANG_OPTION_DESCRIPTION"), true);
        for (Map.Entry<String, Class<? extends Runner>> entry : RUNNERMAP.entrySet()) {
            codeOptions.addChoice(entry.getKey(), entry.getKey());
        }
        return List.of(
            codeOptions,
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
        return () -> Strings.getString("COMMAND_CODE_HELP") + String.join(", ", RUNNERMAP.keySet());
    }
}

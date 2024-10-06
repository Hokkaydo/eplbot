package com.github.hokkaydo.eplbot.module.tutor;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import com.github.hokkaydo.eplbot.configuration.Config;
import com.github.hokkaydo.eplbot.module.tutor.model.CourseTutor;
import com.github.hokkaydo.eplbot.module.tutor.repository.CourseTutorRepository;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class TutorCommand extends ListenerAdapter implements Command {

    private final long guildId;
    private final CourseTutorRepository courseTutorRepository;

    public TutorCommand(Long guildId, CourseTutorRepository courseTutorRepository) {
        this.guildId = guildId;
        this.courseTutorRepository = courseTutorRepository;
    }

    @Override
    public void executeCommand(CommandContext context) {
        String action = context.getOption("action").map(OptionMapping::getAsString).orElseThrow(() -> new IllegalStateException("Should not arise"));
        switch (action) {
            case "manage" -> checkCategoryParameter(context).ifPresent(s -> manage(context, s));
            case "list" -> list(context);
            case "categories" -> categories(context);
            case "ping" -> ping(context);
            default -> throw new IllegalStateException(STR."Unexpected value: \{action}");
        }
    }

    private Optional<String> checkCategoryParameter(CommandContext context) {
        Optional<String> category = context.getOption("category").map(OptionMapping::getAsString);
        if (category.isEmpty()) {
            context.replyCallbackAction().setContent(Strings.getString("TUTOR_COMMAND_CATEGORY_OPTION_EMPTY")).setEphemeral(true).queue();
            return Optional.empty();
        }
        String categoryString = category.get();

        if(!Config.<List<String>>getGuildVariable(guildId, "TUTOR_CATEGORY_IDS").contains(categoryString)) {
            context.replyCallbackAction().setContent(Strings.getString("TUTOR_COMMAND_CATEGORY_OPTION_INVALID")).setEphemeral(true).queue();
            return Optional.empty();
        }
        return category;
    }

    private void manage(CommandContext context, String categoryString) {

        StringSelectMenu.Builder menu = StringSelectMenu.create("courses");

        List<TextChannel> selectedCourses = courseTutorRepository.readByTutorId(context.user().getIdLong())
                                                    .stream()
                                                    .map(c -> Optional.ofNullable(Main.getJDA().getGuildChannelById(c.channelId()))
                                                                      .map(TextChannel.class::cast)
                                                                      .orElseGet(() -> {
                                                                          courseTutorRepository.deleteByChannelId(c.channelId());
                                                                          return null;
                                                                      }))
                                                    .toList();

        List<TextChannel> availableCourses = new ArrayList<>(Optional.ofNullable(Main.getJDA().getCategoryById(Long.parseLong(categoryString)))
                                                                     .orElseThrow(() -> new IllegalStateException("Category doesn't exist !"))
                                                                     .getChannels()
                                                                     .stream()
                                                                     .map(TextChannel.class::cast)
                                                                     .toList());

        availableCourses.removeAll(selectedCourses);

        menu.setRequiredRange(0, selectedCourses.size() + availableCourses.size());
        menu.setDefaultOptions(selectedCourses.stream().map(s -> SelectOption.of(s.getName(), s.getId())).toList());
        menu.addOptions(availableCourses.stream().map(s -> SelectOption.of(s.getName(), s.getId())).toList());

        context.replyCallbackAction().setActionRow(menu.build()).queue();
    }

    private void list(CommandContext context) {
        List<TutorPing> tutors = courseTutorRepository.readByChannelId(context.channel().getIdLong())
                                         .stream()
                                         .map(c -> Optional.ofNullable(Main.getJDA().getUserById(c.tutorId()))
                                                           .map(u -> new TutorPing(u, c.allowsPing()))
                                                           .orElseGet(() -> {
                                                               courseTutorRepository.delete(c);
                                                               return null;
                                                           }))
                                         .filter(Objects::nonNull)
                                         .sorted((t1, t2) -> t1.allowsPing ? t2.allowsPing ? 0 : 1 : -1)
                                         .toList();
        context.replyCallbackAction()
                .setContent(
                        tutors.isEmpty() ?
                                Strings.getString("TUTOR_COMMAND_LIST_NO_TUTOR") :
                                tutors.stream()
                                        .map(r -> STR."\{r.user.getAsMention()} \{r.allowsPing ? ":loudspeaker:" : ""}")
                                        .reduce("__Liste des tuteurs :__\n", (s0, s) -> STR."\{s0}\n\{s}")
                )
                .setSuppressedNotifications(true)
                .queue();
    }

    private void categories(CommandContext context) {
        List<String> categories = Config.getGuildVariable(guildId, "TUTOR_CATEGORY_IDS");
        List<String> toRemove = new ArrayList<>();
        context.replyCallbackAction()
                .setContent(categories.isEmpty() ?
                                    Strings.getString("TUTOR_COMMAND_CATEGORIES_NO_CATEGORIES") :
                                    categories.stream()
                                            .map(c -> {
                                                Category cat = Main.getJDA().getCategoryById(c);
                                                if (cat == null) {
                                                    toRemove.add(c);
                                                    return null;
                                                }
                                                return cat;
                                            })
                                            .filter(Objects::nonNull)
                                            .map(c -> STR."`\{c.getName()}` - `\{c.getId()}`")
                                            .reduce("__Liste des catégories :__\n", (s0, s) -> STR."\{s0}\n- \{s}")
                )
                .setEphemeral(true)
                .queue();
        categories = new ArrayList<>(categories);
        categories.removeAll(toRemove);
        Config.updateValue(guildId, "TUTOR_CATEGORY_IDS", categories);
    }

    private void ping(CommandContext context) {
        List<CourseTutor> courses = courseTutorRepository.readByTutorId(context.user().getIdLong());
        if(courses.isEmpty()) {
            context.replyCallbackAction().setContent(Strings.getString("TUTOR_COMMAND_PING_NO_COURSE")).queue();
            return;
        }

        StringSelectMenu.Builder pingMenu = StringSelectMenu.create("ping");

        List<SelectOption> options = courses.stream()
                                             .map(c -> {
                                                 TextChannel channel = Main.getJDA().getTextChannelById(c.channelId());
                                                 if (channel == null) {
                                                     courseTutorRepository.deleteByChannelId(c.channelId());
                                                     return null;
                                                 }
                                                 return SelectOption.of(channel.getName(), channel.getId()).withDefault(c.allowsPing());
                                             })
                                             .filter(Objects::nonNull)
                                             .toList();

        pingMenu.addOptions(options);
        pingMenu.setRequiredRange(0, options.size());
        context.replyCallbackAction().setActionRow(pingMenu.build()).queue();
    }

    @Override
    public String getName() {
        return "tutor";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("TUTOR_COMMAND_DESCRIPTION");
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING,"action", Strings.getString("TUTOR_COMMAND_ACTION_OPTION_DESCRIPTION"),true)
                        .addChoice("manage", "manage")
                        .addChoice("list", "list")
                        .addChoice("categories", "categories")
                        .addChoice("ping", "ping"),
                new OptionData(OptionType.STRING,"category", Strings.getString("TUTOR_COMMAND_CATEGORY_OPTION_DESCRIPTION"),false)
                        .addChoices(Config.<List<String>>getGuildVariable(guildId, "TUTOR_CATEGORY_IDS").stream().map(c -> new Choice(c, c)).toList())
        );
    }

    @Override
    public boolean ephemeralReply() {
        return false;
    }

    @Override
    public boolean validateChannel(MessageChannel channel) {
        return channel instanceof TextChannel;
    }

    @Override
    public boolean adminOnly() {
        return false;
    }

    @Override
    public Supplier<String> help() {
        return () -> Strings.getString("TUTOR_COMMAND_HELP");
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if(event.getComponentId().startsWith("courses")) {

            // Clear non-selected courses
            event.getSelectMenu().getOptions()
                    .stream()
                    .filter(o -> !event.getSelectedOptions().contains(o))
                    .forEach(o -> courseTutorRepository.delete(new CourseTutor(Long.parseLong(o.getValue()), event.getUser().getIdLong(), false)));

            // Avoid already selected courses
            List<String> oldIds = courseTutorRepository.readByTutorId(event.getUser().getIdLong())
                                          .stream()
                                          .map(c -> String.valueOf(c.channelId()))
                                          .toList();

            // Add new courses
            event.getSelectedOptions()
                    .stream()
                    .filter(o -> !oldIds.contains(o.getValue()))
                    .forEach(o -> courseTutorRepository.create(new CourseTutor(Long.parseLong(o.getValue()), event.getUser().getIdLong(), false)));
            event.reply(Strings.getString("TUTOR_COMMAND_MANAGE_SUCCESS")).queue();
        }

        if (event.getComponentId().startsWith("ping")) {
            event.getSelectedOptions().forEach(o -> courseTutorRepository.updatePing(Long.parseLong(o.getValue()), event.getUser().getIdLong(), true));

            event.getSelectMenu().getOptions()
                    .stream()
                    .filter(o -> !event.getSelectedOptions().contains(o))
                    .forEach(o -> courseTutorRepository.updatePing(Long.parseLong(o.getValue()), event.getUser().getIdLong(), false));
            event.reply(Strings.getString("TUTOR_COMMAND_MANAGE_SUCCESS")).queue();
        }

    }


    private record TutorPing(User user, boolean allowsPing) {}

}

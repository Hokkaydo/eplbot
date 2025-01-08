package com.github.hokkaydo.eplbot.module.tutor;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.MessageUtil;
import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import com.github.hokkaydo.eplbot.configuration.Config;
import com.github.hokkaydo.eplbot.module.tutor.model.CourseTutor;
import com.github.hokkaydo.eplbot.module.tutor.repository.CourseTutorRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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
            case "manage" -> manage(context);
            case "list" -> list(context);
            case "ping" -> ping(context);
            default -> throw new IllegalStateException("Unexpected value: " + action);
        }
    }

    private void manage(CommandContext context) {
        StringSelectMenu.Builder menu = StringSelectMenu.create("category");
        List<String> toRemove = new ArrayList<>();
        List<SelectOption> options = Config.<List<String>>getGuildVariable(guildId, "TUTOR_CATEGORY_IDS")
                                             .stream()
                                             .map(c -> {
                                                 Category cat = Main.getJDA().getCategoryById(c);
                                                 if (cat == null) {
                                                     toRemove.add(c);
                                                     return null;
                                                 }
                                                 return SelectOption.of(cat.getName(), cat.getId());
                                             })
                                             .filter(Objects::nonNull)
                                             .toList();

        menu.addOptions(options);
        menu.setRequiredRange(1, 1);
        context.replyCallbackAction().setActionRow(menu.build()).queue();
        if (toRemove.isEmpty()) return;
        List<String> newIds = new ArrayList<>(Config.getGuildState(guildId, "TUTOR_CATEGORY_IDS"));
        newIds.removeAll(toRemove);
        Config.updateValue(guildId, "TUTOR_CATEGORY_IDS", newIds);

    }

    private void list(CommandContext context) {
        List<TutorPing> tutors = courseTutorRepository.readByChannelId(context.channel().getIdLong())
                                         .stream()
                                         .map(c -> Main.getJDA()
                                                           .retrieveUserById(c.tutorId())
                                                           .map(u -> new TutorPing(u, c.allowsPing()))
                                                           .complete())
                                         .filter(Objects::nonNull)
                                         .sorted((t1, t2) -> t1.allowsPing ? t2.allowsPing ? 0 : 1 : -1)
                                         .toList();
        context.replyCallbackAction()
                .setContent(
                        tutors.isEmpty() ?
                                Strings.getString("TUTOR_COMMAND_LIST_NO_TUTOR") :
                                tutors.stream()
                                        .map(r -> r.user.getAsMention() + (r.allowsPing ? ":loudspeaker:" : ""))
                                        .reduce("__Liste des tuteurs :__\n", "%s%n%s"::formatted)
                )
                .queue();
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
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if(event.getGuild() == null || event.getGuild().getIdLong() != guildId) return;
        switch (event.getComponentId().split("-")[0]) {
            case "category" -> handleCategoryMenu(event);
            case "courses" -> handleCourseMenu(event);
            case "ping" -> handlePingMenu(event);
            default -> event.reply(Strings.getString("ERROR_OCCURRED")).setEphemeral(true).queue();
        }
    }

    private void handleCategoryMenu(StringSelectInteractionEvent event) {
        if(event.getSelectedOptions().isEmpty()) {
            event.reply(Strings.getString("ERROR_OCCURRED")).setEphemeral(true).queue();
            return;
        }
        String category = event.getSelectedOptions().getFirst().getValue();
        StringSelectMenu.Builder menu = StringSelectMenu.create("courses");

        List<Long> selectedCourses = courseTutorRepository.readByTutorId(event.getUser().getIdLong()).stream().map(CourseTutor::channelId).toList();

        List<SelectOption> availableCourses = new ArrayList<>(Optional.ofNullable(Main.getJDA().getCategoryById(Long.parseLong(category)))
                                                                      .orElseThrow(() -> new IllegalStateException("Category doesn't exist !"))
                                                                      .getChannels()
                                                                      .stream()
                                                                      .map(TextChannel.class::cast)
                                                                      .map(s -> SelectOption.of(s.getName(), s.getId()).withDefault(selectedCourses.contains(s.getIdLong())))
                                                                      .toList());

        if(availableCourses.isEmpty()) {
            MessageUtil.sendAdminMessage(Strings.getString("CATEGORY_WITHOUT_COURSES").formatted(category), guildId);
            event.getInteraction().reply(Strings.getString("ERROR_OCCURRED")).setEphemeral(true).queue();
            return;
        }
        menu.setRequiredRange(0, availableCourses.size());
        menu.addOptions(availableCourses);

        event.getInteraction().editSelectMenu(menu.build()).queue();
    }

    private void handleCourseMenu(StringSelectInteractionEvent event) {
        Guild guild = Main.getJDA().getGuildById(guildId);
        if (guild == null) {
            event.reply(Strings.getString("ERROR_OCCURRED")).queue();
            return;
        }

        // Clear non-selected courses
        event.getSelectMenu().getOptions()
                .stream()
                .filter(o -> !event.getSelectedOptions().contains(o))
                .map(o -> Main.getJDA().getTextChannelById(o.getValue()))
                .filter(Objects::nonNull)
                .forEach(channel -> {
                    channel.getManager().removePermissionOverride(event.getUser().getIdLong()).reason("Tutor deletion").queue();
                    courseTutorRepository.delete(new CourseTutor(channel.getIdLong(), event.getUser().getIdLong(), false));
                });

        // Avoid already selected courses
        List<String> oldIds = courseTutorRepository.readByTutorId(event.getUser().getIdLong())
                                      .stream()
                                      .map(c -> String.valueOf(c.channelId()))
                                      .toList();

        // Add new courses
        event.getSelectedOptions()
                .stream()
                .filter(o -> !oldIds.contains(o.getValue()))
                .map(o -> Main.getJDA().getTextChannelById(o.getValue()))
                .filter(Objects::nonNull)
                .forEach(channel -> {
                    channel.getManager().putMemberPermissionOverride(
                                    event.getUser().getIdLong(),
                                    Permission.VIEW_CHANNEL.getRawValue() | Permission.MESSAGE_SEND.getRawValue(),
                                    0
                            )
                            .reason("Tutor permission")
                            .queue();
                    courseTutorRepository.create(new CourseTutor(channel.getIdLong(), event.getUser().getIdLong(), false));
                });
        event.reply(Strings.getString("TUTOR_COMMAND_MANAGE_SUCCESS")).setEphemeral(true).queue();
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING,"action", Strings.getString("TUTOR_COMMAND_ACTION_OPTION_DESCRIPTION"),true)
                        .addChoice("manage", "manage")
                        .addChoice("list", "list")
                        .addChoice("ping", "ping")
        );
    }

    @Override
    public String getName() {
        return "tutor";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("TUTOR_COMMAND_DESCRIPTION");
    }

    private void handlePingMenu(StringSelectInteractionEvent event) {
        event.getSelectedOptions().forEach(o -> courseTutorRepository.updatePing(Long.parseLong(o.getValue()), event.getUser().getIdLong(), true));

        event.getSelectMenu().getOptions()
                .stream()
                .filter(o -> !event.getSelectedOptions().contains(o))
                .forEach(o -> courseTutorRepository.updatePing(Long.parseLong(o.getValue()), event.getUser().getIdLong(), false));
        event.reply(Strings.getString("TUTOR_COMMAND_MANAGE_SUCCESS")).setEphemeral(true).queue();
    }

    @Override
    public boolean ephemeralReply() {
        return true;
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

    private record TutorPing(User user, boolean allowsPing) {}

}
package com.github.hokkaydo.eplbot.module.graderetrieve;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import com.github.hokkaydo.eplbot.module.graderetrieve.repository.CourseGroupRepository;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class LoadCoursesCommand implements Command {

    private final CourseGroupRepository repository;
    LoadCoursesCommand(CourseGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    public void executeCommand(CommandContext context) {
        repository.loadCourses();
        context.replyCallbackAction().setContent("Courses loaded!").queue();
    }

    @Override
    public String getName() {
        return "loadcourses";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("COMMAND_LOAD_COURSES_DESCRIPTION");
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public boolean ephemeralReply() {
        return true;
    }

    @Override
    public boolean validateChannel(MessageChannel channel) {
        return true;
    }

    @Override
    public boolean adminOnly() {
        return true;
    }

    @Override
    public Supplier<String> help() {
        return () -> Strings.getString("COMMAND_LOAD_COURSES_HELP");
    }

}

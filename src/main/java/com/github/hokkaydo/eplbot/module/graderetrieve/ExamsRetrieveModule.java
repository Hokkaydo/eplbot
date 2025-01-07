package com.github.hokkaydo.eplbot.module.graderetrieve;

import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.database.DatabaseManager;
import com.github.hokkaydo.eplbot.module.Module;
import com.github.hokkaydo.eplbot.module.graderetrieve.repository.CourseGroupRepository;
import com.github.hokkaydo.eplbot.module.graderetrieve.repository.CourseGroupRepositorySQLite;
import com.github.hokkaydo.eplbot.module.graderetrieve.repository.CourseRepository;
import com.github.hokkaydo.eplbot.module.graderetrieve.repository.CourseRepositorySQLite;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ExamsRetrieveModule extends Module {

    private final SetupRetrieveChannelCommand setupRetrieveChannelCommand;
    private final LoadCoursesCommand loadCourseCommand;
    private final ExamsRetrieveListener examsRetrieveListener;
    public ExamsRetrieveModule(@NotNull Long guildId) {
        super(guildId);
        CourseRepository courseRepository = new CourseRepositorySQLite(DatabaseManager.getDataSource());
        CourseGroupRepository repository = new CourseGroupRepositorySQLite(DatabaseManager.getDataSource(), courseRepository);
        this.examsRetrieveListener = new ExamsRetrieveListener(guildId, repository);
        this.setupRetrieveChannelCommand = new SetupRetrieveChannelCommand(guildId, examsRetrieveListener);
        this.loadCourseCommand = new LoadCoursesCommand(repository);
    }

    @Override
    public String getName() {
        return "examsretrieve";
    }

    @Override
    public List<Command> getCommands() {
        return List.of(setupRetrieveChannelCommand, loadCourseCommand);
    }

    @Override
    public List<ListenerAdapter> getListeners() {
        return Collections.singletonList(examsRetrieveListener);
    }

}

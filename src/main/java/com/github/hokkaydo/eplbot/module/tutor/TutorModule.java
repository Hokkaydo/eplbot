package com.github.hokkaydo.eplbot.module.tutor;

import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.database.DatabaseManager;
import com.github.hokkaydo.eplbot.module.Module;
import com.github.hokkaydo.eplbot.module.tutor.repository.CourseTutorRepository;
import com.github.hokkaydo.eplbot.module.tutor.repository.CourseTutorRepositorySQLite;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TutorModule extends Module {

    private final TutorCommand tutorCommand;

    public TutorModule(@NotNull Long guildId) {
        super(guildId);
        CourseTutorRepository courseTutorRepository = new CourseTutorRepositorySQLite(DatabaseManager.getDataSource());
        this.tutorCommand = new TutorCommand(guildId, courseTutorRepository);
    }

    @Override
    public String getName() {
        return "tutor";
    }

    @Override
    public List<Command> getCommands() {
        return List.of(tutorCommand);
    }

    @Override
    public List<ListenerAdapter> getListeners() {
        return List.of(tutorCommand);
    }

}

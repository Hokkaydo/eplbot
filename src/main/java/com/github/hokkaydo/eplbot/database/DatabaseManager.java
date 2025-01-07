package com.github.hokkaydo.eplbot.database;

import com.github.hokkaydo.eplbot.module.graderetrieve.repository.CourseGroupRepository;
import com.github.hokkaydo.eplbot.module.graderetrieve.repository.CourseGroupRepositorySQLite;
import com.github.hokkaydo.eplbot.module.graderetrieve.repository.CourseRepositorySQLite;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public class DatabaseManager {

    private static final String INTEGER = "INTEGER";
    private static final String TEXT = "TEXT";
    private static final List<TableModel> TABLES = List.of(
            /* STATE : 0 = config, 1 = state */
            new TableModel("configuration", Map.of("key", TEXT, "value", TEXT, "state", INTEGER, "guild_id", INTEGER)),
            new TableModel("course_groups", Map.of("group_code", TEXT, "french_name", TEXT)),
            new TableModel("courses", Map.of("course_code", TEXT, "course_name", TEXT, "quarter", INTEGER, "group_id", INTEGER)),
            new TableModel("warned_confessions", Map.of("moderator_id", INTEGER, "author_id", INTEGER, "message_content", TEXT, "timestamp", INTEGER)),
            new TableModel("exams_thread", Map.of("message_id", INTEGER, "path", TEXT)),
            new TableModel("mirrors", Map.of("first_id", INTEGER, "second_id", INTEGER)),
            new TableModel("notices", Map.of("author_id", TEXT, "subject_id", TEXT, "content", TEXT, "timestamp", "timestamp", "type", TEXT)),
            new TableModel("bookmarks", Map.of("user_id", INTEGER, "message_id", INTEGER, "description", TEXT, "message_link", TEXT)),
            new TableModel("course_tutors", Map.of("channel_id", INTEGER, "tutor_id", INTEGER, "allows_ping", INTEGER))
    );


    private static DataSource dataSource;

    private DatabaseManager() {}

    public static void regenerateDatabase(boolean drop) {
        JdbcTemplate template = new JdbcTemplate(DatabaseManager.getDataSource());
        if (drop)
            template.update(TABLES.stream().map(TableModel::name).map("DROP TABLE %s;"::formatted).reduce("", (a, b) -> a+b));

        for (TableModel model : TABLES) {

            // Create table if not exists
            template.execute(
                    "CREATE TABLE IF NOT EXISTS %s ( id INTEGER PRIMARY KEY AUTOINCREMENT %s ); %s".formatted(
                            model.name(),
                            model.parameters().entrySet().stream().map(e -> STR."\{e.getKey()} \{e.getValue()}").reduce("", "%s,%s"::formatted),
                            drop ? "delete from sqlite_sequence where name='%s';".formatted(model.name()) : ""
                    )
            );

            // Remove columns that are not in the model
            template.queryForList(STR."PRAGMA table_info(\{model.name()});")
                    .stream()
                    .map(row -> row.get("name"))
                    .filter(col -> !model.parameters().containsKey((String)col))
                    .filter(col -> !col.equals("id"))
                    .forEach(col -> template.update(STR."ALTER TABLE \{model.name()} DROP COLUMN \{col};"));

            // Add columns that are in the model but not in the table
            List<String> columns = template.queryForList(STR."PRAGMA table_info(\{model.name()});")
                                           .stream()
                                           .map(row -> row.get("name"))
                                           .map(Object::toString)
                                           .toList();

            model.parameters()
                    .entrySet()
                    .stream()
                    .filter(e -> !columns.contains(e.getKey()))
                    .forEach(e -> template.update(STR."ALTER TABLE \{model.name()} ADD COLUMN \{e.getKey()} \{e.getValue()};"));
        }

        // Delete old tables that are still in the database and not in the TABLES list
        template.queryForList("SELECT name FROM sqlite_master WHERE type='table' AND NAME NOT LIKE 'sqlite_%';")
                .stream()
                .map(row -> row.get("name"))
                .filter(name -> !TABLES.stream().map(TableModel::name).toList().contains((String)name))
                .map(Object::toString)
                .forEach(name -> template.update(STR."DROP TABLE \{name};"));

        if(drop) {
            CourseGroupRepository repository = new CourseGroupRepositorySQLite(dataSource, new CourseRepositorySQLite(dataSource));
            repository.loadCourses();
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }


    public static void initialize(String persistenceDirPath) {
        dataSource = SQLiteDatasourceFactory.create(STR."\{persistenceDirPath}/database.sqlite");
    }

}

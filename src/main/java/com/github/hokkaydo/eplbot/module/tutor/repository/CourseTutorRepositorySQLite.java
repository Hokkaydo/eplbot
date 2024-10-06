package com.github.hokkaydo.eplbot.module.tutor.repository;

import com.github.hokkaydo.eplbot.module.tutor.model.CourseTutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.List;

public class CourseTutorRepositorySQLite implements CourseTutorRepository {

    private static final RowMapper<CourseTutor> mapper = (ResultSet rs, int ignored) -> new CourseTutor(rs.getLong("channel_id"), rs.getLong("tutor_id"), rs.getLong("allows_ping") == 1);
    private final JdbcTemplate jdbcTemplate;

    public CourseTutorRepositorySQLite(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void create(CourseTutor... courseTutors) {
        for (CourseTutor courseTutor : courseTutors) {
            jdbcTemplate.update("""
                INSERT INTO course_tutors (
                    channel_id,
                    tutor_id,
                    allows_ping
                    )
                VALUES (?,?,?)
                """, courseTutor.channelId(), courseTutor.tutorId(), courseTutor.allowsPing() ? 1 : 0);
        }
    }

    @Override
    public List<CourseTutor> readAll() {
        return jdbcTemplate.query("SELECT * FROM course_tutors", mapper);
    }

    @Override
    public void delete(CourseTutor courseTutor) {
        jdbcTemplate.update("DELETE FROM course_tutors WHERE channel_id = ? AND tutor_id = ?", courseTutor.channelId(), courseTutor.tutorId());
    }

    @Override
    public List<CourseTutor> readByChannelId(Long channelId) {
        return jdbcTemplate.queryForStream(
                "SELECT * FROM course_tutors WHERE channel_id = ?",
                mapper,
                channelId
        ).toList();
    }

    @Override
    public List<CourseTutor> readByTutorId(Long tutorId) {
        return jdbcTemplate.queryForStream(
                "SELECT * FROM course_tutors WHERE tutor_id = ?",
                mapper,
                tutorId
        ).toList();
    }

    @Override
    public void deleteByChannelId(Long channelId) {
        jdbcTemplate.update("DELETE FROM course_tutors WHERE channel_id = ?", channelId);
    }

    @Override
    public void updatePing(Long channelId, Long tutorId, boolean allowPing) {
        jdbcTemplate.update("UPDATE course_tutors SET allows_ping = ? WHERE channel_id = ? AND tutor_id = ?", allowPing ? 1 : 0, channelId, tutorId);
    }
}

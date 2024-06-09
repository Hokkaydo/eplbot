package com.github.hokkaydo.eplbot.module.points.repository;

import com.github.hokkaydo.eplbot.module.points.model.Points;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class PointsRepositorySQLite implements PointsRepository {
    private final JdbcTemplate jdbcTemplate;

    private List<String> roles;
    private static final RowMapper<Points> mapper = (ResultSet rs, int _) ->
            new Points(
                    rs.getString("username"),
                    rs.getInt("points"),
                    rs.getString("role"),
                    rs.getInt("day"),
                    rs.getInt("month")
            );

    public PointsRepositorySQLite(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.roles = getRoles();
    }



    @Override
    public void create(Points... points) {
        for (Points point : points) {
            jdbcTemplate.update("""
                    INSERT INTO points (
                        username,
                        points,
                        role,
                        day,
                        month
                        )
                    VALUES (?,?,?,?,?)
                    """, point.username(), point.points(), point.role(), point.day(), point.month());


    }}

    @Override
    public List<Points> readAll() {
        return jdbcTemplate.query("SELECT * FROM points", mapper);
    }

    @Override
    @Transactional
    public int get(String username) {

        List<Points> userPoints = jdbcTemplate.query(
                "SELECT * FROM points WHERE username = ?",
                mapper,
                username
        );

        if (userPoints.isEmpty()) {
            return 0;
        }

        return userPoints.getFirst().points();
    }

    public Points getUser(String username) {
        List<Points> userPoints = jdbcTemplate.query(
                "SELECT * FROM points WHERE username = ?",
                mapper,
                username
        );

        if (userPoints.isEmpty()) {
            return null;
        }

        return userPoints.getFirst();
    }

    public void updateDate(String username, int day, int month) {
        jdbcTemplate.update("""
                UPDATE points
                SET day = ?,
                month = ?
                WHERE username = ?
                """, day, month, username);
    }
    public void delete() {
        jdbcTemplate.update("DELETE FROM points");
    }

    public List<String> getRoles() {
        //Get all users that start with role_
        List<Points> rolePoints = jdbcTemplate.query(
                "SELECT * FROM points WHERE username LIKE 'role_%'",
                mapper
        );
        List<String> roles = new ArrayList<>();
        for (Points rolePoint : rolePoints) {
            roles.add(rolePoint.username().substring(5));
        }
        return roles;

    }
    public void update(String username, int points) {
        jdbcTemplate.update("""
                UPDATE points
                SET points = ?
                WHERE username = ?
                """, points, username);

    }
    public void resetAll() {
        jdbcTemplate.update("""
                UPDATE points
                SET points = 0
                """);

}
    public boolean dailyStatus(String username, int day, int month) {
        //Get day and month
        List<Points> userPoints = jdbcTemplate.query(
                "SELECT * FROM points WHERE username = ?",
                mapper,
                username
        );
        if (userPoints.isEmpty()) {
            System.out.println("User not found");
            return false;
        }
        return userPoints.getFirst().day() == day && userPoints.getFirst().month() == month;
    }

    @Transactional
    public boolean checkPresence(Member author) {
        //Check if author is in the database
        List<Points> userPoints = jdbcTemplate.query(
                "SELECT * FROM points WHERE username = ?",
                mapper,
                author.getUser().getName()
        );
        String autRole = author.getRoles().stream().map(role -> role.getName()).findFirst().orElse("membre");
        if (userPoints.isEmpty()) {
            return false;
        }
        Points user = userPoints.getFirst();
        if (!user.role().equals(autRole)) {
            //Update role in the database
            jdbcTemplate.update("""
                    UPDATE points
                    SET role = ?
                    WHERE username = ?
                    """, autRole, author.getUser().getName());
        }
        return true;
    }






}

package com.github.hokkaydo.eplbot.module.points;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.database.DatabaseManager;
import com.github.hokkaydo.eplbot.module.points.model.Points;
import com.github.hokkaydo.eplbot.module.points.repository.PointsRepositorySQLite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.max;

public class PointsProcessor extends ListenerAdapter {



        private long guildId;

        private final PointsRepositorySQLite pointsRepo;

        public PointsProcessor (long guildId) {
            super();
            Main.getJDA().addEventListener(this);
            DataSource datasource = DatabaseManager.getDataSource();
            this.guildId = guildId;
            this.pointsRepo = new PointsRepositorySQLite(datasource);

        }
        public int getPoints(String username) {
            return this.pointsRepo.get(username);
        }

        public void addPoints(String username, int points) {
            int currentPoints = getPoints(username);
            this.pointsRepo.update(username, max(currentPoints + points, 0));
        }



        public void setPoints(String username, int points) {
            this.pointsRepo.update(username, points);
        }

        public void resetPoints(String username) {
            this.pointsRepo.update(username, 0);
        }

        public void resetAllPoints() {
            this.pointsRepo.resetAll();

        }

        public boolean hasClaimedDaily(String username,int day, int month) {
            return this.pointsRepo.dailyStatus(username,day, month);
        }

    public void daily(String username, int currentDay, int currentMonth) {
        //Get day and month
        Points userPoints = pointsRepo.getUser(username);
        int day = userPoints.day();
        int month = userPoints.month();
        if (day == currentDay && month == currentMonth) {
            return ;
        }
        addPoints(username, 25);
        //Update day and month
        this.pointsRepo.updateDate(username, currentDay, currentMonth);

    }

    public void activateAuthor(Member author) {
            if (pointsRepo.checkPresence(author)) {
                return;
            }
        List<String> roles = this.pointsRepo.getRoles();
        //Get author roles
        List<String> authorRoles = author.getRoles().stream().map(role -> role.getName()).toList();
        //Check if author has a role in the list of relevant roles
        if (authorRoles.stream().anyMatch(role -> roles.contains(role))) {
            //If yes, add the author to the list of active authors
            String autRole = authorRoles.stream().filter(role -> roles.contains(role)).toList().getFirst();
            this.pointsRepo.create(new Points(author.getUser().getName(), 0, autRole, 0, 0));
        }
        else {
            //If not, add the author to the list of active authors with the default role
            this.pointsRepo.create(new Points(author.getUser().getName(), 0, "membre", 0, 0));
        }

    }

    public List<Points> getLeaderboard() {
        List <Points> all = this.pointsRepo.readAll();
        all = all.stream().filter(p -> !(p.username().startsWith("role_"))).collect(Collectors.toList());
        all.sort((p1, p2) -> p2.points() - p1.points());
        return all.subList(0, Math.min(10, all.size()));
    }

    public List<Points> getRoleLB() {
        List <Points> all = this.pointsRepo.readAll();
        all = all.stream().filter(p -> (p.username().startsWith("role_"))).collect(Collectors.toList());
        all.sort((p1, p2) -> p2.points() - p1.points());
        return all.subList(0, Math.min(10, all.size()));
    }


    public int addRole(String role) {
        if (this.pointsRepo.getUser(role) != null) {
            return -1;
        }
        else {
            this.pointsRepo.create(new Points(role, 0, role.substring(5), 0, 0));
            return 0;
        }

    }

    public String getRole(String username) {
            Points user = this.pointsRepo.getUser(username);
            return user.role();

    }


}

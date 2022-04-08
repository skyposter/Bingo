package de.amin.bingo.gamestates.impl;

import de.amin.bingo.BingoPlugin;
import de.amin.bingo.game.BingoGame;
import de.amin.bingo.gamestates.GameState;
import de.amin.bingo.team.BingoTeam;
import de.amin.bingo.team.TeamManager;
import de.amin.bingo.utils.Localization;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

public class EndState extends GameState {

    private final BingoPlugin plugin;
    private final TeamManager teamManager;
    private final BingoGame game;


    public EndState(BingoPlugin plugin, BingoGame game, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.game = game;
    }

    @Override
    public void start() {
        HashMap<Team, Integer> scores = new HashMap<>();
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        Objective score = scoreboard.getObjective("score") == null ? scoreboard.registerNewObjective("score","dummy","Successfull tasks", RenderType.INTEGER) : scoreboard.getObjective("score");

        score.unregister();

        plugin.getServer().getOnlinePlayers().forEach(player ->  {
            player.setGameMode(GameMode.ADVENTURE);
            //player.setBedSpawnLocation(plugin.getServer().getWorld("world_bingo").getSpawnLocation());
        });

        for (Team team : teamManager.getTeams()) {
            if (team.getSize() > 0) {
                scores.put(team,game.getBoard(team).getFoundItems());
            }
        }

        Bukkit.getOnlinePlayers().forEach(player -> {
            for (Map.Entry<Team, Integer> entry : scores.entrySet()) {
                player.sendMessage(BingoTeam.get(entry.getKey().getName()).getLocalizedName(player) + "[" + entry.getValue() + "]");
            }
        });

        /*
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            player.sendMessage(Localization.get(player, "game.endingstate.server_restart"));
        });
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getServer().shutdown(),15*20);
         */
    }

    @Override
    public void end() {

    }
}

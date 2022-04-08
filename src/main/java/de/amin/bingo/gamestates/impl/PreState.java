package de.amin.bingo.gamestates.impl;

import de.amin.bingo.BingoPlugin;
import de.amin.bingo.gamestates.GameState;
import de.amin.bingo.gamestates.GameStateManager;
import de.amin.bingo.listeners.ConnectionListener;
import de.amin.bingo.utils.Config;
import de.amin.bingo.utils.ItemBuilder;
import de.amin.bingo.utils.Localization;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PreState extends GameState {

    private final BingoPlugin plugin;
    private final GameStateManager gameStateManager;
    private int time = Config.PRESTATE_TIME;
    private BukkitTask timerTask;

    public PreState(BingoPlugin plugin, GameStateManager gameStateManager) {
        this.plugin = plugin;
        this.gameStateManager = gameStateManager;
    }

    @Override
    public void start() {
        Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false));

        File propertiesFile = new File(Bukkit.getWorldContainer(), "server.properties");
        try (FileInputStream stream = new FileInputStream(propertiesFile)) {
            Properties properties = new Properties();
            properties.load(stream);
            plugin.getServer().getOnlinePlayers().forEach(player -> player.teleport(plugin.getServer().getWorld(properties.get("level-name").toString()).getSpawnLocation()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        Objective place = scoreboard.getObjective("Place") == null ? scoreboard.registerNewObjective("Place","dummy",ChatColor.GOLD + "Team position", RenderType.INTEGER) : scoreboard.getObjective("Place");
        Objective score = scoreboard.getObjective("score") == null ? scoreboard.registerNewObjective("score","dummy","Successfull tasks", RenderType.INTEGER) : scoreboard.getObjective("score");
        place.unregister();
        score.unregister();



        plugin.getServer().getOnlinePlayers().forEach(ConnectionListener::setup);
        startTimer();
    }

    @Override
    public void end() {
        timerTask.cancel();
    }

    private void startTimer() {
        Server server = plugin.getServer();

        WorldBorder border = server.getWorlds().get(0).getWorldBorder();
        border.setCenter(server.getWorlds().get(0).getSpawnLocation());
        border.setSize(32);

        timerTask = server.getScheduler().runTaskTimer(plugin, () -> {
            if (time > 0) {
                if (server.getOnlinePlayers().size() >= Config.MIN_PLAYERS) {
                    if (time == Config.FORCESTART_TIME) {
                        plugin.resetWorld();
                    }
                    server.getOnlinePlayers().forEach(player -> {

                        switch (time) {
                            case 60:
                            case 30:
                            case 15:
                            case 10:
                            case 5:
                            case 3:
                            case 2:
                            case 1: {
                                player.sendMessage(Localization.get(player, "game.prestate.timer", String.valueOf(time)));
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                            }

                        }

                        player.setLevel(time);
                    });
                    time--;
                } else {
                    time = Config.PRESTATE_TIME;
                    plugin.getServer().getOnlinePlayers().forEach(player -> {
                        int missingPlayers = Config.MIN_PLAYERS - plugin.getServer().getOnlinePlayers().size();
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(Localization.get(player, "game.prestate.player_missing", String.valueOf(missingPlayers))).create());
                    });
                }
            } else {
                gameStateManager.setGameState(GameState.MAIN_STATE);
            }
        }, 0, 20);
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}

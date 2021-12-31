package de.amin.bingo.gamestates.impl;

import de.amin.bingo.BingoPlugin;
import de.amin.bingo.gamestates.GameState;
import de.amin.bingo.gamestates.GameStateManager;
import de.amin.bingo.utils.Constants;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;

import java.util.Random;

public class PreState extends GameState {

    private final BingoPlugin plugin;
    private final GameStateManager gameStateManager;
    private int time = Constants.PRESTATE_TIME;
    private int timerId;

    public PreState(BingoPlugin plugin, GameStateManager gameStateManager) {
        this.plugin = plugin;
        this.gameStateManager = gameStateManager;
    }

    @Override
    public void start() {
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            player.setGameMode(GameMode.SURVIVAL);
            player.setLevel(0);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            player.setFoodLevel(100);
            player.getInventory().clear();
            player.teleport(player.getWorld().getHighestBlockAt(new Random().nextInt(-25, 25), new Random().nextInt(-25, 25)).getLocation());
        });
        startTimer();
    }

    @Override
    public void end() {
        plugin.getServer().getScheduler().cancelTask(timerId);
    }

    private void startTimer() {
        Server server = plugin.getServer();

        timerId = server.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (time > 0) {
                if (server.getOnlinePlayers().size() >= Constants.MIN_PLAYERS) {
                    server.getOnlinePlayers().forEach(player -> {

                        switch (time) {
                            case 60, 30, 15, 10, 5, 3, 2, 1 -> {
                                player.sendMessage(ChatColor.AQUA + "The Game will start in "
                                        + ChatColor.YELLOW + time + (time == 1 ? " Second" : " Seconds") + ChatColor.AQUA + "!");
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                            }

                        }

                        player.setLevel(time);
                        time--;
                    });
                } else {
                    time = Constants.PRESTATE_TIME;
                    plugin.getServer().getOnlinePlayers().forEach(player -> {
                        int missingPlayers = Constants.MIN_PLAYERS - plugin.getServer().getOnlinePlayers().size();
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(ChatColor.RED + "Waiting for " +
                                ChatColor.YELLOW + missingPlayers +
                                " " +  (missingPlayers==1 ? "player" : "players") + ChatColor.RED + "!").create());
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
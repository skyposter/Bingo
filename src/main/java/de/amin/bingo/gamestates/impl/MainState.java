package de.amin.bingo.gamestates.impl;

import de.amin.bingo.BingoPlugin;
import de.amin.bingo.game.BingoGame;
import de.amin.bingo.game.board.BingoItem;
import de.amin.bingo.game.board.map.BoardRenderer;
import de.amin.bingo.gamestates.GameState;
import de.amin.bingo.gamestates.GameStateManager;
import de.amin.bingo.team.BingoTeam;
import de.amin.bingo.team.TeamManager;
import de.amin.bingo.utils.Config;
import de.amin.bingo.utils.Localization;
import de.amin.bingo.utils.TimeUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.*;

public class MainState extends GameState {

    private int time = Config.GAME_DURATION;
    private BukkitTask timerTask;
    private BukkitTask gameLoop;
    private final BingoPlugin plugin;
    private final GameStateManager gameStateManager;
    private final BingoGame game;
    private final BoardRenderer renderer;
    private final TeamManager teamManager;


    public MainState(BingoPlugin plugin, GameStateManager gameStateManager, BingoGame game, BoardRenderer renderer, TeamManager teamManager) {
        this.plugin = plugin;
        this.gameStateManager = gameStateManager;
        this.game = game;
        this.renderer = renderer;
        this.teamManager = teamManager;
    }

    @Override
    public void start() {
        Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true));
        game.createBoards();
        ItemStack boardMap = getRenderedMapItem();




        plugin.getServer().getOnlinePlayers().forEach(player -> {
            player.setLevel(0);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            player.setFoodLevel(100);
            player.getInventory().clear();
            player.getInventory().setItemInOffHand(boardMap);
            player.teleport(plugin.getServer().getWorld("world_bingo").getSpawnLocation());
            player.setBedSpawnLocation(plugin.getServer().getWorld("world_bingo").getSpawnLocation());

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            player.sendMessage(Localization.get(player, "game.mainstate.start"));

            if (teamManager.getTeam(player) == null) {
                BingoTeam bingoTeam = teamManager.addToSmallest(player);
                player.sendMessage(Localization.get(player, "team.auto_assign", bingoTeam.getLocalizedName(player)));
            }
            renderer.updateImages();

        });

        WorldBorder border = plugin.getServer().getWorlds().get(0).getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(Config.BORDER_SIZE);


        startTimer();
    }

    @Override
    public void end() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (gameLoop != null) {
            gameLoop.cancel();
        }
    }

    private void startTimer() {

        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();

        Objective place = scoreboard.getObjective("Place") == null ? scoreboard.registerNewObjective("Place","dummy",ChatColor.GOLD + "Team position", RenderType.INTEGER) : scoreboard.getObjective("Place");
        Objective score = scoreboard.getObjective("score") == null ? scoreboard.registerNewObjective("score","dummy","Successfull tasks", RenderType.INTEGER) : scoreboard.getObjective("score");

        score.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        place.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<Team> winners = new ArrayList<>();

        gameLoop = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            plugin.getServer().getOnlinePlayers().forEach(player -> {

                score.getScore(player.getName()).setScore(game.getBoard(teamManager.getTeam(player)).getFoundItems());

                //check for all players if they have a new item from the board
                for (BingoItem item : game.getBoard(teamManager.getTeam(player)).getItems()) {
                    if (!item.isFound()) {
                        for (ItemStack content : player.getInventory().getContents()) {
                            if (content != null && content.getType().equals(item.getMaterial())) {
                                item.setFound(true);
                                plugin.getServer().broadcastMessage(Localization.get(player, "game.mainstate.itemfound", teamManager.getTeam(player).getColor() + player.getName(),
                                        String.valueOf(game.getBoard(teamManager.getTeam(player)).getFoundItems()),
                                        String.valueOf(Config.BOARD_SIZE)));
                                plugin.getServer().getOnlinePlayers().forEach(all -> all.playSound(all.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1));
                            }
                        }
                    }
                }
            });

            teamManager.getTeams().forEach(team -> {
                if (winners.size() >= plugin.getServer().getOnlinePlayers().size() || winners.size() > Config.WINNING_TEAMS) {
                    gameLoop.cancel();
                    HashMap<Team, Integer> scores = new HashMap<>();

                    for (Team wteam : teamManager.getTeams()) {
                        if (wteam.getSize() > 0) {
                            scores.put(team,game.getBoard(wteam).getFoundItems());
                        }
                    }

                    Bukkit.getOnlinePlayers().forEach(player -> {
                        for (Map.Entry<Team, Integer> entry : scores.entrySet()) {
                            player.sendMessage(BingoTeam.get(entry.getKey().getName()).getLocalizedName(player) + "[" + entry.getValue() + "]");
                        }
                    });
                    gameStateManager.setGameState(GameState.END_STATE);
                }

                if (game.checkWin(team) && !winners.contains(team)) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendMessage(Localization.get(player, "game.mainstate.win", BingoTeam.get(team.getName()).getLocalizedName(player)));
                    });

                    for (String player : team.getEntries()) {
                        Player thisplayer = plugin.getServer().getPlayer(player);
                        thisplayer.setGameMode(GameMode.SPECTATOR);

                    }
                    winners.add(team);
                    place.getScore(team.getDisplayName()).setScore(winners.size());


                }
            });
        }, 0, 5);

        timerTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (time > 0) {
                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(ChatColor.GREEN + TimeUtils.formatTime(time)).create());

                    switch (time) {
                        case 30:
                        case 15:
                        case 10:
                        case 5:
                        case 3:
                        case 2:
                        case 1: {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
                            player.sendMessage(Localization.get(player, "game.mainstate.end", String.valueOf(time)));
                        }
                    }
                });


                time--;
            } else {
                gameLoop.cancel();
                HashMap<Team, Integer> scores = new HashMap<>();
                Map.Entry<Team,Integer> maxEntry = null;

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

                while (winners.size() < scores.size() && winners.size() <= Config.WINNING_TEAMS) {
                    for (Map.Entry<Team, Integer> entry : scores.entrySet()) {
                        if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
                            if (!winners.contains(entry.getKey())) {
                                maxEntry = entry;
                            }
                        }
                    }
                    if (maxEntry != null) {
                        winners.add(maxEntry.getKey());
                        place.getScore(maxEntry.getKey().getDisplayName()).setScore(winners.size());
                    }
                }

                score.unregister();
                gameStateManager.setGameState(GameState.END_STATE);
            }
        }, 0, 20);

    }

    private ItemStack getRenderedMapItem() {
        ItemStack itemStack = new ItemStack(Material.FILLED_MAP);
        MapView view = Bukkit.createMap(Bukkit.getWorlds().get(0));
        //clear renderers one by one
        for (MapRenderer renderer : view.getRenderers())
            view.removeRenderer(renderer);

        view.addRenderer(renderer);
        MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
        mapMeta.setMapView(view);
        mapMeta.setUnbreakable(true);
        mapMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Bingo Board");
        itemStack.setItemMeta(mapMeta);
        return itemStack;
    }

    public void setTime(int time) {
        this.time = time;
    }
}

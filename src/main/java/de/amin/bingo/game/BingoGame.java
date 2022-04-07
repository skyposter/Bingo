package de.amin.bingo.game;

import de.amin.bingo.BingoPlugin;
import de.amin.bingo.game.board.BingoBoard;
import de.amin.bingo.game.board.BingoMaterial;
import de.amin.bingo.game.board.map.BoardRenderer;
import de.amin.bingo.team.TeamManager;
import de.amin.bingo.utils.Config;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class BingoGame {

    private BingoPlugin plugin;
    private TeamManager teamManager;
    private HashMap<Team, BingoBoard> boards;
    //BingoMaterial[] items = new BingoMaterial[Config.BOARD_SIZE];
    private BoardRenderer renderer;
    private final List<UUID> rejoinPlayer;
    private final List<BingoMaterial> itemnames = Config.getItems();

    public BingoGame(BingoPlugin plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        boards = new HashMap<>();
        rejoinPlayer = new ArrayList<>();
    }

    public void createBoards() {
        BingoMaterial[] items = getRandomBoard();
        teamManager.getTeams().forEach(team -> {
            boards.put(team, new BingoBoard(items));
        });

    }

    private BingoMaterial[] getRandomBoard() {
        BingoMaterial[] items = new BingoMaterial[Config.BOARD_SIZE];
        //generation of random items
        for (int i = 0; i < items.length; i++) {
            BingoMaterial bingoMaterial = getRandomMaterial();
            while (Arrays.asList(items).contains(bingoMaterial)) {
                bingoMaterial = getRandomMaterial();
            }
            items[i] = bingoMaterial;
        }
        return items;
    }

    public boolean checkWin(Team team) {
        int[][] winSituations = new int[][]{
                //horizonzal
                {0, 1, 2, 3},
                {4, 5, 6, 7},
                {8, 9, 10, 11},
                {12, 13, 14, 15},
                //vertical
                {0, 4, 8, 12},
                {1, 5, 9, 13},
                {2, 6, 10, 14},
                {3, 7, 11, 15},
                //diagonal
                {0, 5, 10, 15},
                {3, 6, 9, 12}
        };
        for (int[] winSituation : winSituations) {
            boolean win = true;
            for (int i : winSituation) {
                if (!getBoard(team).getItems()[i].isFound()) {
                    win = false;
                }
            }
            if (win) {
                return true;
            }
        }
        return false;
    }

    public BingoBoard getBoard(Team team) {
        return boards.get(team);
    }

    public BingoMaterial[] getItems(Team team) {
        return boards.get(team).getMaterials();
    }

    public BingoMaterial getRandomMaterial() {
        return itemnames.get(new Random().nextInt(itemnames.size()));
    }

    public List<UUID> getRejoinPlayer() {
        return rejoinPlayer;
    }
}

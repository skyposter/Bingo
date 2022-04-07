package de.amin.bingo.utils;

import de.amin.bingo.BingoPlugin;
import de.amin.bingo.game.board.BingoMaterial;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Config {

    static FileConfiguration config = BingoPlugin.INSTANCE.getConfig();
    private static final double CURRENT_CONFIG_VERSION = 1.2;

    public static double CONFIG_VERSION = config.getDouble("configVersion");
    public static int PRESTATE_TIME = config.getInt("lobbyTime");
    public static int MIN_PLAYERS = config.getInt("minPlayers");
    public static int BORDER_SIZE = config.getInt("borderSize");
    public static int FORCESTART_TIME = config.getInt("forcestartTime");
    public static int GAME_DURATION = config.getInt("gameDuration");
    public static String DEFAULT_LOCALE = config.getString("defaultLocale");
    public static boolean TEAM_LIMIT = config.getBoolean("teamlimit");
    public static boolean PVP = config.getBoolean("pvp");
    public static boolean WORLD_RESET = config.getBoolean("worldReset");
    public static int BOARD_SIZE = 16;
    public static int WINNING_TEAMS = config.getInt("winningTeams") - 1;
    public static List<String> ITEMS = config.getStringList("items");


    public static List<BingoMaterial> getItems() {
        List<String> it = ITEMS;
        List<BingoMaterial> eit = new ArrayList<>();

        for (String item : it) {
            try {
                BingoMaterial mat = BingoMaterial.valueOf(item);
                eit.add(mat);
            } catch (IllegalArgumentException ignored) {}
        }

        if (eit.size() < 16) {
            BingoPlugin.INSTANCE.getLogger().severe("There must be at least 16 items in configuration!");
            BingoPlugin.INSTANCE.getPluginLoader().disablePlugin(BingoPlugin.INSTANCE);
        }

        return eit;
    }

    public static boolean isDeprecated() {
        return CURRENT_CONFIG_VERSION>CONFIG_VERSION;
    }

}

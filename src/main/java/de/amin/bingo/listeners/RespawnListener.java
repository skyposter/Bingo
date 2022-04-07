package de.amin.bingo.listeners;

import de.amin.bingo.BingoPlugin;
import de.amin.bingo.gamestates.GameStateManager;
import de.amin.bingo.gamestates.impl.MainState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnListener implements Listener {
    private final GameStateManager gameStateManager;
    private final BingoPlugin plugin;

    public RespawnListener(GameStateManager gameStateManager, BingoPlugin plugin) {
        this.gameStateManager = gameStateManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e){
        if (gameStateManager.getCurrentGameState() instanceof MainState) {
            e.getPlayer().teleport(plugin.getServer().getWorld("world_bingo").getSpawnLocation());
        }
    }

}

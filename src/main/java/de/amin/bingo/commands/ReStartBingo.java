package de.amin.bingo.commands;

import de.amin.bingo.gamestates.GameState;
import de.amin.bingo.gamestates.GameStateManager;
import de.amin.bingo.gamestates.impl.MainState;
import de.amin.bingo.gamestates.impl.PreState;
import de.amin.bingo.utils.Config;
import de.amin.bingo.utils.Localization;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReStartBingo implements CommandExecutor {
    private final GameStateManager gameStateManager;

    public ReStartBingo(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player))return false;
        Player player = (Player) sender;

        if(gameStateManager.getCurrentGameState() instanceof PreState) {
            sender.sendMessage(Localization.get(player,"command.not_now"));
            return false;
        }

        gameStateManager.setGameState(GameState.PRE_STATE);
        return false;
    }

}

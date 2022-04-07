package de.amin.bingo.game.board;

import de.amin.bingo.utils.Config;

public class BingoBoard {

    private BingoItem[] bingoItems = new BingoItem[Config.BOARD_SIZE];
    private BingoMaterial[] materials;

    public BingoBoard(BingoMaterial[] items) {
        materials = items;
        for (int i = 0; i < items.length; i++) {
            bingoItems[i] = new BingoItem(items[i]);
        }
    }

    public BingoItem[] getItems() {
        return bingoItems;
    }

    public BingoMaterial[] getMaterials() {
        return materials;
    }



    public int getFoundItems() {
        int count = 0;
        for (BingoItem bingoItem : bingoItems) {
            if(bingoItem.isFound()) count++;
        }
        return count;
    }
}

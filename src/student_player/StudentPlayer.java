// TODO: drop maps after nugget is revealed

package student_player;

import Saboteur.SaboteurMove;
import Saboteur.cardClasses.*;
import boardgame.Move;

import Saboteur.SaboteurPlayer;
import Saboteur.SaboteurBoardState;

import java.util.*;

import static Saboteur.SaboteurBoardState.BOARD_SIZE;
import static student_player.MyTools.handToString;
import static student_player.MyTools.pathToHidden;

public class StudentPlayer extends SaboteurPlayer {
    private final static ArrayList<String> DEAD_END_TILES = new ArrayList<>(
        Arrays.asList("1", "1_flip", "2", "2_flip", "3", "3_flip", "4", "4_flip", "11",
            "11_flip", "12", "12_flip", "13", "14", "14_flip", "15")
    );

    private final static HashMap<String, int[][]> DIRECTIONS = new HashMap<String, int[][]>() {{
        put("8",        new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
        put("9",        new int[][]{{1, 0},          {0, 1}, {0, -1}});
        put("9_flip",   new int[][]{        {-1, 0}, {0, 1}, {0, -1}});
        put("10",       new int[][]{                 {0, 1}, {0, -1}});
        put("7",        new int[][]{        {-1, 0}, {0, 1}});
        put("7_flip",   new int[][]{        {-1, 0},         {0, -1}});
        put("5",        new int[][]{{1, 0},          {0, 1}});
        put("5_flip",   new int[][]{        {-1, 0},         {0, -1}});
        put("6",        new int[][]{{1, 0}, {-1, 0},         {0, -1}});
        put("6_flip",   new int[][]{{1, 0}, {-1, 0}, {0, 1}});
        put("0",        new int[][]{{1, 0}, {-1, 0}});
    }};

    private final boolean[] targetRevealed = {false, false, false};
    private final boolean[] hiddenRevealed = {false, false, false};
    private Coord nugget = null;

    public StudentPlayer() {
        super("260773460, 260805212");
    }

    public Move chooseMove(SaboteurBoardState boardState) {
        int round = (int) Math.ceil(boardState.getTurnNumber() / 2.0);

        int id = boardState.getTurnPlayer();

        ArrayList<SaboteurCard> myHand = boardState.getCurrentPlayerCards();
        ArrayList<SaboteurMove> possibleMoves = boardState.getAllLegalMoves();

        SaboteurTile[][] board = boardState.getHiddenBoard();
        checkNugget(board);

        Coord targetPos = null;

        if (nugget != null) {
            targetPos = nugget;
        } else {
            for (int i : new int[]{1, 0, 2}) {
                if (targetRevealed[i]) {
                    continue;
                }

                targetPos = new Coord(12, (2 * i) + 3);
                break;
            }
        }

        Coord midpointTarget = targetPos;
        if (nugget == null) {
            if (midpointTarget.y == 3) {
                midpointTarget = new Coord(12, 4);
            } else if (midpointTarget.y == 7) {
                midpointTarget = new Coord(12, 6);
            }
        }

        for (int i = 0; i < 3; ++i) {
            if (hiddenRevealed[i])
                continue;

            if (pathToHidden(board, new int[]{12, 2 * i + 3}))
                hiddenRevealed[i] = true;
        }

        boolean isMalus = true;
        boolean shouldDrop = false;

        SaboteurMove bestTileMove = null;
        double bestTileDistance = 100;

        SaboteurMove bestDropMove = null;
        int bestDropScore = -1;
        int destroyCards = 0;
        int bonusCards = 0;

        SaboteurMove bonusMove = null;
        SaboteurMove malusMove = null;
        SaboteurMove mapMove = null;

        boolean overrideSequenceSearch = false;

        for (SaboteurMove move : possibleMoves) {
            String cardName = move.getCardPlayed().getName();

            int xPosPlayed = move.getPosPlayed()[0];
            int yPosPlayed = move.getPosPlayed()[1];

            switch (cardName.split(":")[0]) {
                case "Map":
                    mapMove = move;
                    continue;
                case "Malus":
                    malusMove = move;
                    continue;
                case "Bonus":
                    bonusMove = move;
                    continue;
                case "Destroy":
                    continue;
                case "Drop":
                    int dropScore = -1;
                    String cardDroppedName = myHand.get(xPosPlayed).getName();
                    switch (cardDroppedName.split(":")[0]) {
                        case "Map":
                            dropScore = nugget == null ? 0 : 5;
                            if (nugget != null) {
                                shouldDrop = true;
                            }
                            break;
                        case "Malus":
                            dropScore = 2;
                            break;
                        case "Bonus":
                            bonusCards += 1;
                            if (bonusCards > 2) {
                                shouldDrop = true;
                                dropScore = 5;
                            } else {
                                dropScore = 0;
                            }
                            break;
                        case "Destroy":
                            destroyCards += 1;
                            if (destroyCards > 1) {
                                shouldDrop = true;
                                dropScore = 5;
                            } else {
                                dropScore = 3;
                            }
                            break;
                        case "Tile":
                            if (DEAD_END_TILES.contains(cardDroppedName.split(":")[1])) {
                                shouldDrop = true;
                                dropScore = 4;
                            } else {
                                dropScore = 1;
                            }
                            break;
                    }
                    if (dropScore > bestDropScore) {
                        bestDropScore = dropScore;
                        bestDropMove = move;
                    }
                    continue;
                case "Tile":
                    String tileName = cardName.split(":")[1];
                    isMalus = false; //we know we are not injured (by mallus) because we can still play

                    if (DEAD_END_TILES.contains(tileName))
                        continue;

                    double smallestDistance = 100;

                    for (int[] dir : DIRECTIONS.get(tileName)) {
                        int x = xPosPlayed + dir[0];
                        int y = yPosPlayed + dir[1];
                        if (x >= 0 && y >= 0 && x < BOARD_SIZE && y < BOARD_SIZE && board[x][y] != null) {
                            continue;
                        }

                        double distance = Math.abs(x - midpointTarget.x) + Math.abs(y - midpointTarget.y);
                        if (distance < smallestDistance) {
                            smallestDistance = distance;
                        }
                    }

                    boolean isNineOrEight = tileName.equals("8") || tileName.equals("9") || tileName.equals("9_flip");
                    if ((round == 1 && tileName.equals("5") && (xPosPlayed == 5)) ||
                        (round <= 2 && tileName.equals("0") && (xPosPlayed == 6) && (yPosPlayed == 5)) ||
                        (isNineOrEight && midpointTarget.x == 4 && xPosPlayed == 12 && yPosPlayed == 4) ||
                        (isNineOrEight && midpointTarget.x == 6 && xPosPlayed == 12 && yPosPlayed == 6) ||
                        (tileName.equals("10") && (midpointTarget.x == 6 || midpointTarget.x == 4) && xPosPlayed == 12 && (yPosPlayed == 6 || yPosPlayed == 4)))
                    {
                        bestTileDistance = smallestDistance;
                        bestTileMove = move;
                        overrideSequenceSearch = true;
                    }
                    else if (smallestDistance < bestTileDistance) {
                        bestTileDistance = smallestDistance;
                        bestTileMove = move;
                    }
            }
        }

        ArrayList<SaboteurCard> tilesAndDestroys = new ArrayList<>();
        for (SaboteurCard card : myHand) {
            String cardName = card.getName();
            if (cardName.equals("Destroy")) {
                tilesAndDestroys.add(card);
            } else if (cardName.contains("Tile")) {
                String tileName = cardName.split(":")[1];
                if (!DEAD_END_TILES.contains(tileName)) {
                    tilesAndDestroys.add(card);
                }
            }
        }

        ArrayList<SaboteurMove> winningMoves =
                MyTools.lookForWinningSequence(boardState, tilesAndDestroys, new int[]{targetPos.x, targetPos.y}, hiddenRevealed);

        SaboteurCard bestDropCard = myHand.get(bestDropMove.getPosPlayed()[0]);

        if (mapMove != null && nugget == null) {
            for (int i : new int[]{0, 2, 1}) {
                if (!targetRevealed[i]) {
                    return new SaboteurMove(new SaboteurMap(), 12, (2 * i) + 3, id);
                }
            }
        }

        if (malusMove != null && bestTileDistance <= 3) {
            return malusMove;
        }

        if (isMalus) {
            if (bonusMove != null) {
                return bonusMove;
            }

            return bestDropMove;
        }

        if (overrideSequenceSearch) {
            return bestTileMove;
        }

        if (destroyCards > 0 && bestTileDistance <= 3 && boardState.getTurnNumber() <= 30) {
            SaboteurMove destroyEntranceMove = null;
            if (board[6][5] != null && (board[6][4] != null || board[7][5] != null || board[6][6] != null)) {
                destroyEntranceMove = new SaboteurMove(new SaboteurDestroy(), 6, 5, id);
            } else if (board[5][4] != null && (board[5][3] != null || board[6][4] != null)) {
                destroyEntranceMove = new SaboteurMove(new SaboteurDestroy(), 5, 4, id);
            } else if (board[5][6] != null && (board[5][7] != null || board[6][6] != null)) {
                destroyEntranceMove = new SaboteurMove(new SaboteurDestroy(), 5, 6, id);
            }

            if (destroyEntranceMove != null) {
                return destroyEntranceMove;
            }
        }

        if (winningMoves != null) {
            SaboteurMove firstMove = winningMoves.get(0);
            if (winningMoves.size() > 1 && firstMove.getPosPlayed()[0] == 6 && firstMove.getPosPlayed()[1] == 5) {
                // save repairing the entrance for last
                return winningMoves.get(1);
            } else {
                return winningMoves.get(0);
            }
        }

        // TODO: improve this to calculate the actual minimum number of moves to victory
        if (bestTileDistance <= 0) {
            //near the end but can't win, dropping a card
            return bestDropMove;
        }

        if (shouldDrop) {
            //far from the end and should drop, dropping a bad card
            return bestDropMove;
        }

        if (bestTileMove != null) {
            return bestTileMove;
        }

        if (boardState.getTurnNumber() > 42) {
            return boardState.getRandomMove();
        }

        return bestDropMove;
    }

    private void checkNugget(SaboteurTile[][] board) {
        if (nugget != null)
            return;

        int numHiddenRevealed = 0;
        for (int i = 0; i < 3; i++) {
            int y = (2 * i) + 3;
            String tileName = board[12][y].getName();
            if (tileName.equals("Tile:nugget")) {
                numHiddenRevealed++;
                targetRevealed[i] = true;
                nugget = new Coord(12, y);
            } else if (tileName.startsWith("Tile:hidden")) {
                numHiddenRevealed++;
                targetRevealed[i] = true;
            }
        }

        if (numHiddenRevealed == 2 && nugget == null) {
            for (int i = 0; i < 3; i++) {
                if (!targetRevealed[i]) {
                    targetRevealed[i] = true;
                    nugget = new Coord(12, (2 * i) + 3);
                }
            }
        }
    }
}
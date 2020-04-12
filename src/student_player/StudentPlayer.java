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
    private static ArrayList<String> DEAD_END_TILES = new ArrayList<>(
        Arrays.asList("1", "1_flip", "2", "2_flip", "3", "3_flip", "4", "4_flip", "11",
            "11_flip", "12", "12_flip", "13", "14", "14_flip", "15")
    );

    private static HashMap<String, int[][]> DIRECTIONS = new HashMap<String, int[][]>() {{
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
    private boolean destroyedEntrance = false;

    public StudentPlayer() {
        super("SUCCESS");
    }

    public Move chooseMove(SaboteurBoardState boardState) {
        int round = (int) Math.ceil(boardState.getTurnNumber() / 2.0);

        int id = boardState.getTurnPlayer();

        ArrayList<SaboteurCard> myHand = boardState.getCurrentPlayerCards();
        ArrayList<SaboteurMove> possibleMoves = boardState.getAllLegalMoves();

        System.out.println("\n\n\nTurn " + boardState.getTurnNumber() + "\n");
        System.out.println("Hand: " + handToString(myHand));
        System.out.println("Moves: " + MyTools.movesToString(possibleMoves));

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
        System.out.println("targetPos: " + targetPos.x + "," + targetPos.y);

        Coord midpointTarget = targetPos;
        if (nugget == null) {
            if (midpointTarget.y == 3) {
                midpointTarget = new Coord(12, 4);
            } else if (midpointTarget.y == 7) {
                midpointTarget = new Coord(12, 6);
            }
        }
        System.out.println("midpointTarget: " + midpointTarget.x + "," + midpointTarget.y);

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
        SaboteurMove destroyEntranceMove = null;

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
                    if (xPosPlayed == 6 && yPosPlayed == 5 && board[7][5] != null) {
                        destroyEntranceMove = move;
                    }
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

                    System.out.println("Move " + move.toTransportable() + " distance " + smallestDistance);

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

        if (winningMoves == null) {
            System.out.println("Can't win yet");
        } else {
            System.out.println("Winning sequence found, size " + winningMoves.size());
            for (SaboteurMove move : winningMoves) {
                System.out.println("\t" + move.toTransportable());
            }
        }

        System.out.println("best tile move: " + (bestTileMove == null ? "null" : bestTileMove.toTransportable()) + ", distance: " + bestTileDistance + ", overrideSequenceSearch: " + overrideSequenceSearch);

        SaboteurCard bestDropCard = myHand.get(bestDropMove.getPosPlayed()[0]);
        System.out.println("best drop move: " + bestDropCard.getName() + ", score: " + bestDropScore);

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
            System.out.println("MALUS");
            if (bonusMove != null) {
                return bonusMove;
            }

            return bestDropMove;
        }

        if (overrideSequenceSearch) {
            System.out.println("playing an override card");
            return bestTileMove;
        }

        if (destroyEntranceMove != null) {
            System.out.println("!!!!DESTROYING THE ENTRANCE!!!!");
            destroyedEntrance = true;
            return destroyEntranceMove;
        }

        if (winningMoves != null) {
            System.out.println("playing a winning move");
            return winningMoves.get(0);
        }

        // TODO: improve this to calculate the actual minimum number of moves to victory
        if (bestTileDistance <= 0) {
            System.out.println("near the end but can't win, dropping a card");
            return bestDropMove;
        }

        if (shouldDrop) {
            System.out.println("far from the end and should drop, dropping a bad card");
            return bestDropMove;
        }

        if (bestTileMove != null) {
            System.out.println("playing bestTileMove");
            return bestTileMove;
        }

        if (boardState.getTurnNumber() > 42) {
            System.out.println("we should have won by now :(");
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
                System.out.println("Found the nugget at " + nugget.x + "," + nugget.y);
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
                    System.out.println("Deduced the nugget at " + nugget.x + "," + nugget.y);
                }
            }
        }
    }
}
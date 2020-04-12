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
        put("5_flip",   new int[][]{{1, 0},                  {0, -1}});
        put("6",        new int[][]{{1, 0}, {-1, 0},         {0, -1}});
        put("6_flip",   new int[][]{{1, 0}, {-1, 0}, {0, 1}});
        put("0",        new int[][]{{1, 0}, {-1, 0}});
    }};

    private boolean[] hiddenRevealed = {false, false, false};
    private Coord nugget = null;

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

        int[] targetPos = new int[]{12, 5};
        if (nugget != null) {
            targetPos = new int[]{nugget.x, nugget.y};
        } else {
            for (int i : new int[]{0, 2, 1}) {
                if (hiddenRevealed[i]) {
                    continue;
                }

                targetPos = new int[]{12, (2 * i) + 3};
                break;
            }
        }
        System.out.println("targetPos: " + targetPos[0] + "," + targetPos[1]);

        Coord midpointTarget = new Coord(targetPos[0], targetPos[1]);
        if (midpointTarget.y == 3) {
            midpointTarget = new Coord(12, 4);
        } else if (midpointTarget.y == 7) {
            midpointTarget = new Coord(12, 6);
        }
        System.out.println("midpointTarget: " + midpointTarget.x + "," + midpointTarget.y);

        // check for winning move
        ArrayList<int[]> targets = new ArrayList<>();

        if (nugget != null) {
            targets.add(new int[]{nugget.x, nugget.y});
        }
        else {
            for (int i = 0; i < 3; i++) {
                if (!hiddenRevealed[i]) {
                    targets.add(new int[]{12, (3 + (i*2))});
                }
            }
        }

        SaboteurMove winningMove = MyTools.lookForWinningMove(myHand, possibleMoves, board, targets);

//        ArrayList<SaboteurMove> winningMoves;
//        try {
//            winningMoves = MyTools.lookForWinningSequence(hiddenRevealed, boardState, tilesAndDestroys, targetPos);
//
//            if (winningMoves != null) {
//                System.out.println("Winning sequence found:");
//                for (SaboteurMove move : winningMoves) {
//                    System.out.println(move.toTransportable());
//                }
//            }
//        } catch (Exception e) {
//            System.out.println(">>> MyTools.lookForWinningSequence FAILED <<<");
//        }

        boolean isMalus = true;

        SaboteurMove bestTileMove = null;
        double bestTileDistance = 100;

        SaboteurMove bestDropMove = null;
        int bestDropScore = -1;

        SaboteurMove bonusMove = null;
        SaboteurMove malusMove = null;
        SaboteurMove mapMove = null;

        boolean overrideSequenceSearch = false;

        for (SaboteurMove move : possibleMoves) {
            String cardName = move.getCardPlayed().getName();

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
                    int dropScore = calcDropScore(myHand.get(move.getPosPlayed()[0]).getName());
                    if (dropScore > bestDropScore) {
                        bestDropScore = dropScore;
                        bestDropMove = move;
                    }
                    continue;
                case "Tile":
                    String tileName = cardName.split(":")[1];
                    isMalus = false; //we know we are not injured (by mallus) because we can still play

                    if (DEAD_END_TILES.contains(tileName)) {
                        continue;
                    }

                    int xPosPlayed = move.getPosPlayed()[0];
                    int yPosPlayed = move.getPosPlayed()[1];

                    double smallestCartesianDistance = 100;
                    for (int[] dir : DIRECTIONS.get(tileName)) {
                        int x = xPosPlayed + dir[0];
                        int y = yPosPlayed + dir[1];
                        if (x >= 0 && y >= 0 && x < BOARD_SIZE && y < BOARD_SIZE && board[x][y] != null) {
                            continue;
                        }

                        int xDistance = x - midpointTarget.x;
                        int yDistance = y - midpointTarget.y;
                        double cartesianDistance = Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));

                        if (cartesianDistance < smallestCartesianDistance) {
                            smallestCartesianDistance = cartesianDistance;
                        }
                    }

                    boolean isNineOrEight = tileName.equals("8") || tileName.equals("9") || tileName.equals("9_flip");
                    if ((round == 1 && tileName.equals("5") && (xPosPlayed == 5)) ||
                        (round <= 2 && tileName.equals("0") && (xPosPlayed == 6) && (yPosPlayed == 5)) ||
                        (isNineOrEight && midpointTarget.x == 4 && xPosPlayed == 12 && yPosPlayed == 4) ||
                        (isNineOrEight && midpointTarget.x == 6 && xPosPlayed == 12 && yPosPlayed == 6) ||
                        (tileName.equals("10") && (midpointTarget.x == 6 || midpointTarget.x == 4) && xPosPlayed == 12 && (yPosPlayed == 6 || yPosPlayed == 4)))
                    {
                        bestTileDistance = smallestCartesianDistance;
                        bestTileMove = move;
                        overrideSequenceSearch = true;
                    }

                    if ((smallestCartesianDistance < bestTileDistance) && !overrideSequenceSearch) {
                        bestTileDistance = smallestCartesianDistance;
                        bestTileMove = move;
                    }
            }
        }

        System.out.println("best tile move: " + (bestTileMove == null ? "null" : bestTileMove.toTransportable()) + ", distance: " + bestTileDistance + ", overrideSequenceSearch: " + overrideSequenceSearch);

        System.out.println("best drop move: " + bestDropMove.toTransportable() + ", score: " + bestDropMove);

        if (mapMove != null && nugget == null) {
            for (int i : new int[]{0, 2, 1}) {
                if (!hiddenRevealed[i]) {
                    return new SaboteurMove(new SaboteurMap(), 12, (2 * i) + 3, id);
                }
            }
        }

        if (malusMove != null && bestTileDistance <= 4) {
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

        if (winningMove != null) {
            System.out.println("playing a winning move");
            return winningMove;
        }

        if (bestTileDistance <= 3) {
            System.out.println("near the end but can't win, dropping a card");
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
                hiddenRevealed[i] = true;
                nugget = new Coord(12, y);
                System.out.println("Found the nugget at " + nugget.x + "," + nugget.y);
            } else if (tileName.startsWith("Tile:hidden")) {
                numHiddenRevealed++;
                hiddenRevealed[i] = true;
            }
        }

        if (numHiddenRevealed == 2 && nugget == null) {
            for (int i = 0; i < 3; i++) {
                if (!hiddenRevealed[i]) {
                    hiddenRevealed[i] = true;
                    nugget = new Coord(12, (2 * i) + 3);
                }
            }
        }
    }

    private int calcDropScore(String cardName) {
        int dropScore = -1;
        switch (cardName.split(":")[0]) {
            case "Map":
                dropScore = nugget == null ? 0 : 5;
                break;
            case "Malus":
                dropScore = 2;
                break;
            case "Bonus":
                dropScore = 0;
                break;
            case "Destroy":
                dropScore = 3;
                break;
            case "Tile":
                if (DEAD_END_TILES.contains(cardName.split(":")[1])) {
                    dropScore = 4;
                } else {
                    dropScore = 1;
                }
                break;
        }
        return dropScore;
    }
}
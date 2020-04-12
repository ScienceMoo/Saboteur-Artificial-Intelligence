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

        // LOOK AT THE BOARD
        SaboteurTile[][] board = boardState.getHiddenBoard();
        checkNugget(board);

        // FIND OUT WHAT CARDS I HAVE
        ArrayList<SaboteurCard> tilesAndDestroys = new ArrayList<>();
        int deadEndCardIndex = -1;
        int droppableCardIndex = -1;

        for (int i = 0; i < myHand.size(); i++) {
            SaboteurCard c = myHand.get(i);

            if (c.getName().equals("Destroy")) {
                tilesAndDestroys.add(c);
            }
            else if (c.getName().split(":")[0].equals("Tile")) {
                String tileNumber = c.getName().split(":")[1];
                if (tileNumber.equals("1") || tileNumber.equals("2") || tileNumber.equals("3") ||
                    tileNumber.equals("4") || tileNumber.equals("11") || tileNumber.equals("12") ||
                    tileNumber.equals("13") || tileNumber.equals("14") || tileNumber.equals("15")
                ) {
                    deadEndCardIndex = i;
                } else {
                    tilesAndDestroys.add(c);
                }
            } else {
                droppableCardIndex = i;
            }
        }


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

        ArrayList<SaboteurMove> winningMoves;

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

        try {
            winningMoves = MyTools.lookForWinningSequence(hiddenRevealed, boardState, tilesAndDestroys, targetPos);

            if (winningMoves != null) {
                System.out.println("Winning sequence found:");
                for (SaboteurMove move : winningMoves) {
                    System.out.println(move.toTransportable());
                }
            }
        } catch (Exception e) {
            System.out.println(">>> MyTools.lookForWinningSequence FAILED <<<");
        }

        boolean isMalus = true;
        double bestTileDistance = 100;

        SaboteurMove bestTileMove = null;
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
            }

            if (cardName.startsWith("Tile")) {
                String tileName = cardName.split(":")[1];
                isMalus = false; //we know we are not injured (by mallus) because we can still play

                if (DEAD_END_TILES.contains(tileName)) {
                    continue;
                }

                System.out.println("analyzing tile move " + move.toTransportable());

                // this is the actual position that the cards are played at
                int xPosPlayed = move.getPosPlayed()[0];
                int yPosPlayed = move.getPosPlayed()[1];

                int target = 0;
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
                    (isNineOrEight && (target == 4) && (xPosPlayed == 12) && (yPosPlayed == 4)) ||
                    (isNineOrEight && (target == 6) && (xPosPlayed == 12) && (yPosPlayed == 6)) ||
                    (tileName.equals("10") && ((target == 6) || (target == 4)) && (xPosPlayed == 12) && ((yPosPlayed == 6) || (yPosPlayed == 4))))
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

        boolean nearTheEnd = bestTileDistance <= 2;

        if (malusMove != null && nearTheEnd) {
            return malusMove;
        }

        if (mapMove != null && nugget == null) {
            for (int i : new int[]{0, 2, 1}) {
                if (!hiddenRevealed[i]) {
                    return new SaboteurMove(new SaboteurMap(),12,(2 * i) + 3,id);
                }
            }
        }

        if (isMalus) {
            System.out.println("MALUS");
            if (bonusMove != null) {
                return bonusMove;
            }

            if (deadEndCardIndex != -1) {
                return new SaboteurMove(new SaboteurDrop(), deadEndCardIndex, 0, id);
            }

            if (droppableCardIndex != -1 && (boardState.getTurnNumber() > 42)) {
                return new SaboteurMove(new SaboteurDrop(), droppableCardIndex, 0, id);
            }

            if (boardState.getTurnNumber() > 42) {
                return new SaboteurMove(new SaboteurDrop(), 0, 0, id);
            }
        }

        if (overrideSequenceSearch) {
            System.out.println("playing an 'override' card");
            return bestTileMove;
        }

        if (winningMove != null) {
            System.out.println("playing a winning move");
            return winningMove;
        }

        if ((bestTileDistance == 2) && (nugget != null)) {
            System.out.println("near the end but can't win, dropping a card");
            if (deadEndCardIndex != -1) {
                return new SaboteurMove(new SaboteurDrop(), deadEndCardIndex, 0, id);
            }

            if (malusMove != null) {
                return malusMove;
            }

            if (droppableCardIndex != -1) {
                return new SaboteurMove(new SaboteurDrop(), droppableCardIndex, 0, id);
            }
        }

        if (bestTileMove != null) {
            System.out.println("playing bestTileMove");
            return bestTileMove;
        }

        return new SaboteurMove(new SaboteurDrop(), 0, 0, id);
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
            } else if (tileName.startsWith("Tile:hidden")) {
                numHiddenRevealed++;
                hiddenRevealed[i] = true;
            }
        }

        if (numHiddenRevealed == 2) {
            for (int i = 0; i < 3; i++) {
                if (!hiddenRevealed[i]) {
                    hiddenRevealed[i] = true;
                    nugget = new Coord(12, (2 * i) + 3);
                }
            }
        }
    }
}
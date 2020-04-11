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
        super("260805212");
    }

    public Move chooseMove(SaboteurBoardState boardState) {
        int round = (int) Math.ceil(boardState.getTurnNumber() / 2.0);

        int id = boardState.getTurnPlayer();

        ArrayList<SaboteurCard> myHand = boardState.getCurrentPlayerCards();
        ArrayList<SaboteurMove> possibleMoves = boardState.getAllLegalMoves();

        System.out.println("\n\n\nRound " + round);
        System.out.println("Turn " + boardState.getTurnNumber());
        System.out.println("The moves I can actually play:");
        System.out.println(MyTools.movesToString(possibleMoves));

        ArrayList<SaboteurCard> myTilesAndDestroys = new ArrayList<>();

        boolean winningSequencePossible = false;
        boolean winningPossibleForThem = false;

        // LOOK AT THE BOARD
        SaboteurTile[][] board = boardState.getHiddenBoard();
        checkNugget(board);

        // FIND OUT WHAT CARDS I HAVE
        int deadEndCardIndex = -1;

        System.out.println(handToString(myHand));
        for (int i = 0; i < myHand.size(); i++) {
            SaboteurCard c = myHand.get(i);

            if (c.getName().equals("Destroy")) {
                myTilesAndDestroys.add(c);
            }
            else if (c.getName().split(":")[0].equals("Tile")) {
                myTilesAndDestroys.add(c);
                String tileNumber = c.getName().split(":")[1];
                if (tileNumber.equals("1") || tileNumber.equals("2") || tileNumber.equals("3")
                        || tileNumber.equals("4") || tileNumber.equals("11") || tileNumber.equals("12")
                        || tileNumber.equals("13") || tileNumber.equals("14") || tileNumber.equals("15")
                ) {
                    deadEndCardIndex = i;
                }
            }
        }

        ArrayList<Integer> cardsToAvoidDropping = new ArrayList<>();

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

        ArrayList<SaboteurMove> moves = null;

        try {
            moves = MyTools.lookForWinningSequence(hiddenRevealed, boardState, myTilesAndDestroys, targetPos);
            if (moves != null) {
                winningSequencePossible = true;
                for (int i = 0; i < moves.size(); i++) {
                    SaboteurCard card = moves.get(i).getCardPlayed();
                    for (int index = 0; index < myHand.size(); index++) {
                        if ((myHand.get(index)).getName().equals(card.getName())){
                            cardsToAvoidDropping.add(index);
                        }
                    }
                }
            }
            else {
                winningSequencePossible = false;
            }
        } catch (Exception e) {
            System.out.println("mytools failed");
        }

        // TODO: implement winningPossibleForThem
        //winningPossibleForThem = MyTools.checkIfEnemyCanWin(hiddenRevealed, boardState, targetPos);
        //System.out.println("WINNING POSSIBLE FOR THEM: " + winningPossibleForThem);

        System.out.println("nugget location known: " + nugget != null);
        System.out.println("winningSequencePossible: " + winningSequencePossible);

        boolean isMalus = true;
        boolean shouldDestroy = false;
        double bestDestroyDistance = 100;

        boolean canPath = false;
        double bestDistance = 100;
        SaboteurMove bestTileMove = null;
        SaboteurMove bestDestroyMove = null;
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

            if (cardName.startsWith("Tile") && !DEAD_END_TILES.contains(cardName.split(":")[1])) {
                String tileName = cardName.split(":")[1];
                isMalus = false; //we know we are not injured (by mallus) because we can still play
                canPath = true;

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

                    Coord pos = new Coord(x, y);

                    int xDistance = (pos.x - 12);
                    int yDistance = 0;
                    if (nugget != null) {
                        yDistance = (pos.y - nugget.y);
                        target = nugget.y;
                    } else if (hiddenRevealed[0]) {
                        yDistance = (pos.y - 6);
                        target = 6;
                    } else if (hiddenRevealed[2]) {
                        yDistance = (pos.y - 4);
                        target = 4;
                    } else {
                        yDistance = (pos.y - 5);
                        target = 5;
                    }
                    double cartesianDistance = Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));
                    System.out.println("tile move: " + move.toTransportable());
                    System.out.println("cartesianDistance: " + cartesianDistance);
                    if (cartesianDistance < smallestCartesianDistance) {
                        smallestCartesianDistance = cartesianDistance;
                    }
                }

                ///////////////////
                // SPECIAL MOVES //
                ///////////////////
                boolean isNineOrEight = tileName.equals("8") || tileName.equals("9") || tileName.equals("9_flip");
                if ((round == 1 && tileName.equals("5")) ||
                        (round <= 2 && tileName.equals("0") && (xPosPlayed == 6) && (yPosPlayed == 5)) ||
                        (isNineOrEight && (target == 4) && (xPosPlayed == 12) && (yPosPlayed == 4)) ||
                        (isNineOrEight && (target == 6) && (xPosPlayed == 12) && (yPosPlayed == 6)) ||
                        (tileName.equals("0") && ((target == 6) || (target == 4)) && (xPosPlayed == 12) && ((yPosPlayed == 6) || (yPosPlayed == 4)))) {
                    overrideSequenceSearch = true;
                }

                System.out.println("smallestCartesianDistance: " + smallestCartesianDistance);
                System.out.println("bestDistance: " + bestDistance);
                System.out.println("overrideSequenceSearch: " + overrideSequenceSearch);

                if (smallestCartesianDistance < bestDistance || overrideSequenceSearch) {
                    System.out.println("smallestCartesianDistance: " + smallestCartesianDistance);
                    System.out.println("bestDistance: " + bestDistance);
                    bestDistance = smallestCartesianDistance;
                    System.out.println("smallestCartesianDistance: " + smallestCartesianDistance);
                    System.out.println("bestDistance: " + bestDistance);
                    bestTileMove = move;
                }
            }

            try {
                if (cardName.startsWith("Destroy")) {
                    canPath = true;
                    int xPosPlayed = move.getPosPlayed()[0];
                    int yPosPlayed = move.getPosPlayed()[1];

                    ArrayList<Integer> xPosPotential = new ArrayList<>();
                    ArrayList<Integer> yPosPotential = new ArrayList<>();

                    SaboteurCard cardBelow = board[xPosPlayed + 1][yPosPlayed];
                    SaboteurCard cardAbove = board[xPosPlayed - 1][yPosPlayed];
                    SaboteurCard cardRight = board[xPosPlayed][yPosPlayed + 1];
                    SaboteurCard cardLeft = board[xPosPlayed][yPosPlayed - 1];
                    if (cardBelow != null) {
                        String cardNum = cardBelow.getName().split(":")[1];
                        if (cardNum.equals("0") || cardNum.equals("7") || cardNum.equals("7_flip") || cardNum.equals("6") || cardNum.equals("6_flip") || cardNum.equals("8") || cardNum.equals("9_flip")) {
                            xPosPotential.add(xPosPlayed + 1);
                            yPosPotential.add(yPosPlayed);
                        }
                    }
                    if (cardAbove != null) {
                        String cardNum = cardAbove.getName().split(":")[1];
                        if (cardNum.equals("0") || cardNum.equals("5") || cardNum.equals("5_flip") || cardNum.equals("6") || cardNum.equals("6_flip") || cardNum.equals("8") || cardNum.equals("9")) {
                            xPosPotential.add(xPosPlayed - 1);
                            yPosPotential.add(yPosPlayed);
                        }
                    }
                    if (cardRight != null) {
                        String cardNum = cardRight.getName().split(":")[1];
                        if (cardNum.equals("6") || cardNum.equals("8") || cardNum.equals("10") || cardNum.equals("9") || cardNum.equals("9_flip")) {
                            xPosPotential.add(xPosPlayed);
                            yPosPotential.add(yPosPlayed + 1);
                        }
                    }
                    if (cardLeft != null) {
                        String cardNum = cardLeft.getName().split(":")[1];
                        if (cardNum.equals("6_flip") || cardNum.equals("8") || cardNum.equals("10") || cardNum.equals("9") || cardNum.equals("9_flip")) {
                            xPosPotential.add(xPosPlayed);
                            yPosPotential.add(yPosPlayed - 1);
                        }
                    }
                    for (int xxx = 0; xxx < xPosPotential.size(); xxx++) {
                        int xDistance = (xPosPotential.get(xxx) - 12);
                        int yDistance = 0;
                        if (nugget != null) {
                            yDistance = (yPosPotential.get(xxx) - nugget.y);
                        } else if (hiddenRevealed[0]) {
                            yDistance = (yPosPotential.get(xxx) - 6);
                        } else if (hiddenRevealed[2]) {
                            yDistance = (yPosPotential.get(xxx) - 4);
                        } else {
                            yDistance = (yPosPotential.get(xxx) - 5);
                        }
                        double cartesianDistance = Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));
                        System.out.println("cartesianDistance: " + cartesianDistance);
                        if (cartesianDistance < bestDestroyDistance) {
                            System.out.println("cartesianDistance: " + cartesianDistance);
                            System.out.println("bestDestroyDistance: " + bestDestroyDistance);
                            bestDestroyMove = move;
                            bestDestroyDistance = cartesianDistance;
                            System.out.println("cartesianDistance: " + cartesianDistance);
                            System.out.println("bestDestroyDistance: " + bestDestroyDistance);

                            if (cartesianDistance < bestDistance) {
                                System.out.println("cartesianDistance: " + cartesianDistance);
                                System.out.println("bestDestroyDistance: " + bestDestroyDistance);
                                bestDistance = cartesianDistance;
                                System.out.println("cartesianDistance: " + cartesianDistance);
                                System.out.println("bestDestroyDistance: " + bestDestroyDistance);
                                shouldDestroy = true;
                                bestDestroyMove = move;
                                System.out.println("DESTROY MOVE IS BEST");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("DESTROY FAIL");
            }
        }

        System.out.println("isMalus? " + isMalus);

        // TODO: implement winningPossibleForThem
        if (malusMove != null && (winningPossibleForThem || round > 3)) {
            return malusMove;
        }

        else if (mapMove != null && nugget == null) {
            for (int i : new int[]{0, 2, 1}) {
                if (!hiddenRevealed[i]) {
                    return new SaboteurMove(new SaboteurMap(),12,(2 * i) + 3,id);
                }
            }
        }

        else if (isMalus) {
            if (bonusMove != null) {
                return bonusMove;
            }

            // if there is a dead end card to drop, drop it
            if (deadEndCardIndex != -1) {
                return new SaboteurMove(new SaboteurDrop(), deadEndCardIndex, 0, id);
            }

            if (malusMove != null) {
                return malusMove;
            }

            // else, drop any card that's not a potentially valuable card
            for (int i = 0; i < myHand.size(); i++) {
                if (!cardsToAvoidDropping.contains(i)) {
                    return new SaboteurMove(new SaboteurDrop(), i, 0, id);
                }
            }

            if (bestDestroyMove != null) {
                // TODO: fix destroy
//                return bestDestroyMove;
            }

            return new SaboteurMove(new SaboteurDrop(), 0, 0, id);
        }

        else if (canPath) {
            if (shouldDestroy && bestDestroyMove != null) {
                // TODO: fix destroy
//                return bestDestroyMove;
            }

            if (winningSequencePossible && !overrideSequenceSearch) {
                System.out.println("Winning sequence is possible!");

                SaboteurMove myMove = moves.get(0);
                for (SaboteurMove move : possibleMoves) {
                    int x = move.getPosPlayed()[0];
                    int y = move.getPosPlayed()[1];
                    if (move.getCardPlayed().getName().equals(myMove.getCardPlayed().getName()) &&
                        x == myMove.getPosPlayed()[0] && y == myMove.getPosPlayed()[1])
                    {
                        return move;
                    }
                }

                System.out.println("INVALID MOVE DETERMINED BY ALGO THING!!!");
            }

            if (bestTileMove != null) {
                return bestTileMove;
            }
        }

        //TODO: destroy strategically near the end
        /*else if (isMalus && winningPossibleForThem) {
            // get best destroy
        }*/

        System.out.println("SHOULD NEVER REACH HERE!!!");
        return boardState.getRandomMove();
    }

    private void checkNugget(SaboteurTile[][] board) {
        if (nugget != null)
            return;

        int numHiddenRevealed = 0;
        for (int i = 0; i < 3; i++) {
            int y = (2 * i) + 3;
            String tileName = board[12][y].getName();
            System.out.println("hidden tile: " + tileName);
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
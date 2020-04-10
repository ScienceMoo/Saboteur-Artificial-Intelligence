// TODO: drop maps after nugget is revealed

package student_player;

import Saboteur.SaboteurMove;
import Saboteur.cardClasses.*;
import boardgame.Move;

import Saboteur.SaboteurPlayer;
import Saboteur.SaboteurBoardState;

import java.util.*;

import static student_player.MyTools.handToString;

public class StudentPlayer extends SaboteurPlayer {
    private static int round = 1;

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
    private int numHiddenRevealed = 0;
    private Coord nugget = null;

    public StudentPlayer() {
        super("260805212");
    }

    public Move chooseMove(SaboteurBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...

        //boardState.printBoard();

        // Is random the best you can do?
        SaboteurMove myMove = boardState.getRandomMove();

        int id = boardState.getTurnPlayer();

        //System.out.println("MY PLAYER");
        //System.out.println("Current player is " + boardState.getTurnPlayer());

        ArrayList<SaboteurCard> myHand = boardState.getCurrentPlayerCards();

        ArrayList<SaboteurMove> possibleMoves = boardState.getAllLegalMoves();

        System.out.println("\n\n\nRound Number " + round + "!!!!");

        System.out.println("My actual real moves that I can actually play");
        System.out.println(MyTools.movesToString(possibleMoves));

        ArrayList<SaboteurCard> myTilesAndDestroys = new ArrayList();

        boolean winningSequencePossible = false;
        //boolean winningPossibleForThem = false;

        // LOOK AT THE BOARD
        SaboteurTile[][] board = boardState.getHiddenBoard();
        checkNugget(board);

        // FIND OUT WHAT CARDS I HAVE
        boolean hasMapCard = false;
        boolean hasMalusCard = false;
        boolean hasBonusCard = false;
        boolean hasDeadEndCard = false;
        int deadEndCardIndex = -1;
        ArrayList<Integer> droppableCards = new ArrayList<>();

        System.out.println(handToString(myHand));
        for (int i = 0; i < myHand.size(); i++) {
            SaboteurCard c = myHand.get(i);

            if (c.getName().equals("Map")) {
                hasMapCard = true;
            }
            else if (c.getName().equals("Malus")) {
                hasMalusCard = true;
            }
            else if (c.getName().equals("Bonus")) {
                hasBonusCard = true;
            }
            else if (c.getName().equals("Destroy")) {
                myTilesAndDestroys.add(c);
            }
            else if (c.getName().split(":")[0].equals("Tile")) {
                myTilesAndDestroys.add(c);
                String tileNumber = c.getName().split(":")[1];
                if (tileNumber.equals("1") || tileNumber.equals("2") || tileNumber.equals("3")
                        || tileNumber.equals("4") || tileNumber.equals("11") || tileNumber.equals("12")
                        || tileNumber.equals("13") || tileNumber.equals("14") || tileNumber.equals("15")
                ) {
                    hasDeadEndCard = true;
                    deadEndCardIndex = i;
                }
            }
        }
        System.out.println("Map card? " + hasMapCard);
        System.out.println("Malus card? " + hasMalusCard);
        System.out.println("Bonus card? " + hasBonusCard);

        int bestCardToPlayIndex = 0;
        int bestCardToPlayPositionX = 0;
        int bestCardToPlayPositionY = 0;
        boolean firstCardFlipped = false;
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

        int[] resultArray = MyTools.lookForWinningSequence(hiddenRevealed, boardState, myTilesAndDestroys, targetPos);

        //winningPossibleForThem = MyTools.checkIfEnemyCanWin(hiddenRevealed, boardState, targetPos);
        if (resultArray[0] != -1) {
            bestCardToPlayIndex = resultArray[0];
            bestCardToPlayPositionX = resultArray[1];
            bestCardToPlayPositionY = resultArray[2];
            winningSequencePossible = true;
            firstCardFlipped = resultArray[3] == 1;
            for (int r = 4; r < 10; r++) {
                if (resultArray[r] != -1){
                    cardsToAvoidDropping.add(resultArray[r]);
                }
            }
        }
        else {
            winningSequencePossible = false;
        }
        System.out.println("nugget location known: " + nugget != null);
        System.out.println("winningSequencePossible: " + winningSequencePossible);

        boolean isMalus = true;
        boolean canDestroy = false;
        boolean shouldDestroy = false;
        int bestDestroyableX = 0;
        int bestDestroyableY = 0;
        double bestDestroyDistance = 100;

        boolean canBonus = false;

        boolean canPath = false;
        double bestDistance = 100;
        String bestTile = "";
        Coord bestTileCoord = null;

        boolean overrideSequenceSearch = false;

        for (SaboteurMove c : possibleMoves) {
            String cardName = c.getCardPlayed().getName();
            String tileName = cardName.split(":")[1];

            if (cardName.startsWith("Tile") && !DEAD_END_TILES.contains(tileName)) {
                isMalus = false; //we know we are not injured (by mallus) because we can still play
                canPath = true;

                // this is the actual position that the cards are played at
                int xPosPlayed = c.getPosPlayed()[0];
                int yPosPlayed = c.getPosPlayed()[1];

                ArrayList<Coord> posPotential = new ArrayList<>();

                int target = 0;
                double smallestCartesianDistance = 100;

                for (int[] dir : DIRECTIONS.get(tileName)) {
                    int x = xPosPlayed + dir[0];
                    int y = yPosPlayed + dir[1];
                    if (board[x][y] != null) {
                        continue;
                    }

                    Coord pos = new Coord(x, y);

                    int xDistance = (pos.x - 12);
                    int yDistance = 0;
                    if (nugget != null) {
                        yDistance = (pos.y - nugget.y);
                        target = nugget.y;
                    }
                    else if (hiddenRevealed[0]) {
                        yDistance = (pos.y - 6);
                        target = 6;
                    }
                    else if (hiddenRevealed[2]) {
                        yDistance = (pos.y - 4);
                        target = 4;
                    }
                    else {
                        yDistance = (pos.y - 5);
                        target = 5;
                    }
                    double cartesianDistance = Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));
                    if (cartesianDistance < smallestCartesianDistance) {
                        System.out.println("smallestCartesianDistance: " + smallestCartesianDistance);
                        System.out.println("cartesianDistance: " + cartesianDistance);
                        smallestCartesianDistance = cartesianDistance;
                        System.out.println("smallestCartesianDistance: " + smallestCartesianDistance);
                        System.out.println("cartesianDistance: " + cartesianDistance);
                    }
                }

                ///////////////////
                // SPECIAL MOVES //
                ///////////////////
                boolean isNineOrEight = tileName.equals("8") || tileName.equals("9") || tileName.equals("9_flip");
                if ((round == 1 && tileName.equals("5")) ||
                    (round <= 2 && tileName.equals("0")) ||
                    (isNineOrEight && (target == 4) && (xPosPlayed == 12) && (yPosPlayed == 4)) ||
                    (isNineOrEight && (target == 6) && (xPosPlayed == 12) && (yPosPlayed == 6)) ||
                    (tileName.equals("0") && ((target == 6) || (target == 4)) && (xPosPlayed == 12) && ((yPosPlayed == 6) || (yPosPlayed == 4))))
                {
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
                    bestTile = c.getCardPlayed().getName().split(":")[1];
                    bestTileCoord = new Coord(c.getPosPlayed()[0], c.getPosPlayed()[1]);
                }
            }

            if (c.getCardPlayed().getName().startsWith("Destroy")) {
                canPath = true;
                canDestroy = true;
                int xPosPlayed = c.getPosPlayed()[0];
                int yPosPlayed = c.getPosPlayed()[1];

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
                        yPosPotential.add( yPosPlayed);
                    }
                }
                if (cardAbove != null) {
                    String cardNum = cardBelow.getName().split(":")[1];
                    if (cardNum.equals("0") || cardNum.equals("5") || cardNum.equals("5_flip") || cardNum.equals("6") || cardNum.equals("6_flip") || cardNum.equals("8") || cardNum.equals("9")) {
                        xPosPotential.add( xPosPlayed - 1);
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
                    String cardNum = cardRight.getName().split(":")[1];
                    if (cardNum.equals("6_flip") || cardNum.equals("8") || cardNum.equals("10") || cardNum.equals("9") || cardNum.equals("9_flip")) {
                        xPosPotential.add( xPosPlayed);
                        yPosPotential.add(yPosPlayed - 1);
                    }
                }
                for (int xxx = 0; xxx < xPosPotential.size(); xxx++){
                    int xDistance = (xPosPotential.get(xxx) - 12);
                    int yDistance = 0;
                    if (nugget != null) {
                        yDistance = (yPosPotential.get(xxx) - nugget.y);
                    }
                    else if (hiddenRevealed[0]) {
                        yDistance = (yPosPotential.get(xxx) - 6);
                    }
                    else if (hiddenRevealed[2]) {
                        yDistance = (yPosPotential.get(xxx) - 4);
                    }
                    else {
                        yDistance = (yPosPotential.get(xxx) - 5);
                    }
                    double cartesianDistance = Math.sqrt((xDistance * xDistance) + (yDistance * yDistance));
                    System.out.println("cartesianDistance: " + cartesianDistance);
                    if (cartesianDistance < bestDestroyDistance) {
                        System.out.println("cartesianDistance: " + cartesianDistance);
                        System.out.println("bestDestroyDistance: " + bestDestroyDistance);
                        canDestroy = true;
                        bestDestroyableX = c.getPosPlayed()[0];
                        bestDestroyableY = c.getPosPlayed()[1];
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
                            bestDestroyableX = c.getPosPlayed()[0];
                            bestDestroyableY = c.getPosPlayed()[1];
                            System.out.println("DESTROY MOVE IS BEST");
                        }
                    }
                }
            }
            if (c.getCardPlayed().getName().startsWith("Bonus")) {
                canBonus = true;
            }
        }

        System.out.println("isMalus? " + isMalus);

        //System.out.println("WINNING POSSIBLE FOR THEM: " + winningPossibleForThem);
        // TODO: only use malusCard if winningPossibleForThem
        if (hasMalusCard) {
            myMove = new SaboteurMove((new SaboteurMalus()),0,0,id);
            return myMove;
        }

        else if (hasMapCard && nugget == null) {
            for (int i : new int[]{0, 2, 1}) {
                if (!hiddenRevealed[0]) {
                    myMove = new SaboteurMove(new SaboteurMap(),12,(2 * i) + 3,id);
                    return myMove;
                }
            }
        }

        else if (isMalus && hasBonusCard && canBonus) {
            myMove = new SaboteurMove((new SaboteurBonus()),0,0,id);
        }

        else if (isMalus && !hasBonusCard && hasDeadEndCard) {
            if (deadEndCardIndex != -1) {
                myMove = new SaboteurMove(new SaboteurDrop(),deadEndCardIndex,0,id);
            }
            boolean cardFound = false;
            for (int i =0; i<cardsToAvoidDropping.size(); i++) {
                if (cardsToAvoidDropping.contains(i)){
                    continue;
                }
                else {
                    cardFound = true;
                    myMove = new SaboteurMove(new SaboteurDrop(),i,0,id);
                }
            }
            if (!cardFound) {
                myMove = new SaboteurMove((new SaboteurDestroy()),bestDestroyableX,bestDestroyableY,id);
            }

        }

        else if (isMalus && hasMalusCard) {
            myMove = new SaboteurMove((new SaboteurMalus()),0,0,id);
            return myMove;
        }

        else if (!isMalus && canPath) {
            if ((winningSequencePossible) && (!overrideSequenceSearch) && (!shouldDestroy)) {
                System.out.println("Winning sequence is possible!");
                System.out.println("bestCardToPlayIndex: " + bestCardToPlayIndex);
                SaboteurCard c = myHand.get(bestCardToPlayIndex);
                if (c instanceof SaboteurDestroy) {
                    myMove = new SaboteurMove((new SaboteurDestroy()), bestCardToPlayPositionX , bestCardToPlayPositionY,id);
                }
                else if (c instanceof SaboteurTile) {
                    String cardName = c.getName().split(":")[1];
                    if (firstCardFlipped) {
                        cardName += "_flip";
                    }
                    System.out.println("bestCardToPlayPositionX: "+ bestCardToPlayPositionX);
                    System.out.println("bestCardToPlayPositionY: "+ bestCardToPlayPositionY);
                    myMove = new SaboteurMove((new SaboteurTile(cardName)), bestCardToPlayPositionX , bestCardToPlayPositionY,id);
                }
            }
            else if (shouldDestroy) {
                myMove = new SaboteurMove((new SaboteurDestroy()),bestDestroyableX,bestDestroyableY,id);
            }
            else {
                myMove = new SaboteurMove((new SaboteurTile(bestTile)),bestTileCoord.x,bestTileCoord.x,id);
            }
        }

        //TODO: destroy strategically near the end
        /*else if (isMalus && winningPossibleForThem) {
            // get best destroy
        }*/

        round++;

        return myMove;
    }

    private void checkNugget(SaboteurTile[][] board) {
        if (nugget != null)
            return;

        for (int y : new int[]{3, 5, 7}) {
            if (!board[12][3].getName().equals("Tile:8")) {
                numHiddenRevealed++;
                nugget = new Coord(12, y);
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
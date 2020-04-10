package student_player;

import Saboteur.SaboteurMove;
import Saboteur.cardClasses.*;
import boardgame.Move;

import Saboteur.SaboteurPlayer;
import Saboteur.SaboteurBoardState;
import com.sun.codemodel.internal.JCase;

import java.util.ArrayList;

import static student_player.MyTools.handToString;

/** A player file submitted by a student. */
public class StudentPlayer extends SaboteurPlayer {

    public static int round = 1;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260805212");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
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


        boolean [] hiddenRevealed = new boolean[3];
        hiddenRevealed[0] = false;
        hiddenRevealed[1] = false;
        hiddenRevealed[2] = false;

        boolean hiddenCard = false;
        boolean nuggetLocationKnown = false;
        int nuggetX = 0;
        int nuggetY = 0;


        // LOOK AT THE BOARD
        SaboteurTile[][] board = boardState.getHiddenBoard();
        int[][] intBoard = boardState.getHiddenIntBoard();

        //System.out.println(intBoard.toString());

        int numHiddenRevealed = 0;

        if (board[12][3].getName().equals("Tile:8")) {
            hiddenRevealed[0] = false;
            hiddenCard = true;
        }
        else {
            numHiddenRevealed++;
            nuggetLocationKnown = true;
            nuggetX = 12;
            nuggetY = 3;
        }
        if (board[12][5].getName().equals("Tile:8")) {
            hiddenRevealed[0] = false;
            hiddenCard = true;
        }
        else {
            numHiddenRevealed++;
            nuggetLocationKnown = true;
            nuggetX = 12;
            nuggetY = 5;
        }
        if (board[12][7].getName().equals("Tile:8")) {
            hiddenRevealed[0] = false;
            hiddenCard = true;
        }
        else {
            numHiddenRevealed++;
            nuggetLocationKnown = true;
            nuggetX = 12;
            nuggetY = 7;
        }

        if (numHiddenRevealed == 2) {
            for (int i = 0; i < 3; i++) {
                if (hiddenRevealed[i] == false) {
                    hiddenRevealed[i] = true;
                    nuggetLocationKnown = true;
                    nuggetX = 12;
                    nuggetY = (2 * i) + 3;
                    hiddenCard = false;
                }
            }
        }

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

        if (nuggetLocationKnown) {
            int[] targetPos = new int[]{nuggetX, nuggetY};
            int[] resultArray = MyTools.lookForWinningSequence(hiddenRevealed, boardState, myTilesAndDestroys, targetPos);



            //winningPossibleForThem = MyTools.checkIfEnemyCanWin(hiddenRevealed, boardState, targetPos);
            if (resultArray[0] != -1) {
                bestCardToPlayIndex = resultArray[0];
                bestCardToPlayPositionX = resultArray[1];
                bestCardToPlayPositionY = resultArray[2];
                winningSequencePossible = true;
                if (resultArray[3] == 1) {
                    firstCardFlipped = true;
                }
                else {
                    firstCardFlipped = false;
                }
                for (int r = 4; r < 10; r++) {
                    if (resultArray[r] != -1){
                        cardsToAvoidDropping.add(resultArray[r]);
                    }
                }
            }
            else {
                winningSequencePossible = false;
            }
        }
        else {
            if (!hiddenRevealed[0]) {
                int[] targetPos = new int[]{12, 3};
                int[] resultArray = MyTools.lookForWinningSequence(hiddenRevealed, boardState, myTilesAndDestroys, targetPos);
                //winningPossibleForThem = MyTools.checkIfEnemyCanWin(hiddenRevealed, boardState, targetPos);
                if (resultArray[0] != -1) {
                    bestCardToPlayIndex = resultArray[0];
                    bestCardToPlayPositionX = resultArray[1];
                    bestCardToPlayPositionY = resultArray[2];
                    winningSequencePossible = true;
                    if (resultArray[3] == 1) {
                        firstCardFlipped = true;
                    }
                    else {
                        firstCardFlipped = false;
                    }
                }
                else {
                    winningSequencePossible = false;
                }
            }
            else if (!hiddenRevealed[2]) {
                int[] targetPos = new int[]{12, 7};
                int[] resultArray = MyTools.lookForWinningSequence(hiddenRevealed, boardState, myTilesAndDestroys, targetPos);
                //winningPossibleForThem = MyTools.checkIfEnemyCanWin(hiddenRevealed, boardState, targetPos);
                if (resultArray[0] != -1) {
                    bestCardToPlayIndex = resultArray[0];
                    bestCardToPlayPositionX = resultArray[1];
                    bestCardToPlayPositionY = resultArray[2];
                    winningSequencePossible = true;
                    if (resultArray[3] == 1) {
                        firstCardFlipped = true;
                    }
                    else {
                        firstCardFlipped = false;
                    }
                }
                else {
                    winningSequencePossible = false;
                }
            }
            else if (!hiddenRevealed[1]) {
                int[] targetPos = new int[]{12, 5};
                int[] resultArray = MyTools.lookForWinningSequence(hiddenRevealed, boardState, myTilesAndDestroys, targetPos);
                //winningPossibleForThem = MyTools.checkIfEnemyCanWin(hiddenRevealed, boardState, targetPos);
                if (resultArray[0] != -1) {
                    bestCardToPlayIndex = resultArray[0];
                    bestCardToPlayPositionX = resultArray[1];
                    bestCardToPlayPositionY = resultArray[2];
                    winningSequencePossible = true;
                    if (resultArray[3] == 1) {
                        firstCardFlipped = true;
                    }
                    else {
                        firstCardFlipped = false;
                    }
                }
                else {
                    winningSequencePossible = false;
                }
            }
        }
        System.out.println("nugget location known: " + nuggetLocationKnown);
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
        int bestTileX = 0;
        int bestTileY = 0;

        ArrayList<Integer> xPosPotential = new ArrayList<>();
        ArrayList<Integer> yPosPotential = new ArrayList<>();

        boolean overrideSequenceSearch = false;

        for (int i = 0; i < possibleMoves.size(); i++) {
            SaboteurMove c = possibleMoves.get(i);
            String cardName = c.getCardPlayed().getName();

            if (cardName.startsWith("Tile")
                    && !cardName.split(":")[1].equals("1")
                    && !cardName.split(":")[1].equals("1_flip")
                    && !cardName.split(":")[1].equals("2")
                    && !cardName.split(":")[1].equals("2_flip")
                    && !cardName.split(":")[1].equals("3")
                    && !cardName.split(":")[1].equals("3_flip")
                    && !cardName.split(":")[1].equals("4")
                    && !cardName.split(":")[1].equals("4_flip")
                    && !cardName.split(":")[1].equals("11")
                    && !cardName.split(":")[1].equals("11_flip")
                    && !cardName.split(":")[1].equals("12")
                    && !cardName.split(":")[1].equals("12_flip")
                    && !cardName.split(":")[1].equals("13")
                    && !cardName.split(":")[1].equals("14")
                    && !cardName.split(":")[1].equals("14_flip")
                    && !cardName.split(":")[1].equals("15")

            ) {
                isMalus = false; //we know we are not injured (by mallus) because we can still play
                canPath = true;

                // this is the actual position that the cards are played at
                int xPosPlayed = c.getPosPlayed()[0];
                int yPosPlayed = c.getPosPlayed()[1];

                // clear the arrays to make new space
                xPosPotential.removeAll(xPosPotential);
                yPosPotential.removeAll(yPosPotential);

                //some booleans for calculations below
                boolean isNineOrEight = false;
                boolean isFive = false;
                boolean isZero = false;

                int rank = 0;
                if (cardName.split(":")[1].equals("8")) {
                    if (board[xPosPlayed + 1][yPosPlayed] == null) {
                        xPosPotential.add(xPosPlayed + 1);
                        yPosPotential.add( yPosPlayed);
                    }
                    if (board[xPosPlayed - 1][yPosPlayed] == null) {
                        xPosPotential.add( xPosPlayed - 1);
                        yPosPotential.add(yPosPlayed);
                    }
                    if (board[xPosPlayed][yPosPlayed + 1] == null) {
                        xPosPotential.add(xPosPlayed);
                        yPosPotential.add(yPosPlayed + 1);
                    }
                    if (board[xPosPlayed][yPosPlayed - 1] == null) {
                        xPosPotential.add( xPosPlayed);
                        yPosPotential.add(yPosPlayed - 1);
                    }
                    isNineOrEight = true;
                }
                else if (cardName.split(":")[1].equals("9")) {
                    if (board[xPosPlayed + 1][yPosPlayed] == null) {
                        xPosPotential.add(xPosPlayed + 1);
                        yPosPotential.add( yPosPlayed);
                    }
                    if (board[xPosPlayed][yPosPlayed - 1] == null) {
                        xPosPotential.add(xPosPlayed);
                        yPosPotential.add(yPosPlayed - 1);
                    }
                    if (board[xPosPlayed][yPosPlayed + 1] == null) {
                        xPosPotential.add(xPosPlayed);
                        yPosPotential.add(yPosPlayed + 1);
                    }
                    isNineOrEight = true;
                }
                else if (cardName.split(":")[1].equals("9_flip")) {
                    if (board[xPosPlayed - 1][yPosPlayed] == null) {
                        xPosPotential.add(xPosPlayed - 1);
                        yPosPotential.add( yPosPlayed);
                    }
                    if (board[xPosPlayed][yPosPlayed - 1] == null) {
                        xPosPotential.add(xPosPlayed);
                        yPosPotential.add(yPosPlayed - 1);
                    }
                    if (board[xPosPlayed][yPosPlayed + 1] == null) {
                        xPosPotential.add(xPosPlayed);
                        yPosPotential.add(yPosPlayed + 1);
                    }
                    isNineOrEight = true;
                }
                else if (cardName.split(":")[1].equals("10")) {
                    if (board[xPosPlayed][yPosPlayed + 1] == null) {
                        xPosPotential.add(xPosPlayed);
                        yPosPotential.add(yPosPlayed + 1);
                    }
                    if (board[xPosPlayed][yPosPlayed - 1] == null) {
                        xPosPotential.add(xPosPlayed);
                        yPosPotential.add(yPosPlayed - 1);
                    }
                }
                else if (cardName.split(":")[1].equals("7")) {
                    if (board[xPosPlayed - 1][yPosPlayed] == null) {
                        xPosPotential.add(xPosPlayed - 1);
                        yPosPotential.add(yPosPlayed);
                    }
                    if (board[xPosPlayed][yPosPlayed + 1] == null) {
                        xPosPotential.add( xPosPlayed);
                        yPosPotential.add(yPosPlayed + 1);
                    }
                }
                else if (cardName.split(":")[1].equals("7_flip")) {
                    if (board[xPosPlayed - 1][yPosPlayed] == null) {
                        xPosPotential.add(xPosPlayed - 1);
                        yPosPotential.add(yPosPlayed);
                    }
                    if (board[xPosPlayed][yPosPlayed - 1] == null) {
                        xPosPotential.add( xPosPlayed);
                        yPosPotential.add(yPosPlayed - 1);
                    }
                }
                else if (cardName.split(":")[1].equals("5")) {
                    if (board[xPosPlayed + 1][yPosPlayed] == null) {
                        xPosPotential.add(xPosPlayed + 1);
                        yPosPotential.add( yPosPlayed);
                    }
                    if (board[xPosPlayed][yPosPlayed + 1] == null) {
                        xPosPotential.add(xPosPlayed);
                        yPosPotential.add(yPosPlayed + 1);
                    }
                    isFive = true;
                }
                else if (cardName.split(":")[1].equals("5_flip")) {
                    if (board[xPosPlayed + 1][yPosPlayed] == null) {
                        xPosPotential.add(xPosPlayed + 1);
                        yPosPotential.add( yPosPlayed);
                    }
                    if (board[xPosPlayed][yPosPlayed - 1] == null) {
                        xPosPotential.add(1, xPosPlayed);
                        yPosPotential.add(1, yPosPlayed - 1);
                    }
                    isFive = true;
                }
                else if (cardName.split(":")[1].equals("6")) {
                    if (board[xPosPlayed -1][yPosPlayed] == null) {
                        xPosPotential.add(xPosPlayed - 1);
                        yPosPotential.add(yPosPlayed);
                    }
                    if (board[xPosPlayed + 1][yPosPlayed] == null) {
                        xPosPotential.add(xPosPlayed + 1);
                        yPosPotential.add(yPosPlayed);
                    }
                    if (board[xPosPlayed][yPosPlayed - 1] == null) {
                        xPosPotential.add(xPosPlayed);
                        yPosPotential.add(yPosPlayed - 1);
                    }
                }
                else if (cardName.split(":")[1].equals("6_flip")) {
                    if (board[xPosPlayed -1][yPosPlayed] == null) {
                        xPosPotential.add(xPosPlayed - 1);
                        yPosPotential.add(yPosPlayed);
                    }
                    if (board[xPosPlayed + 1][yPosPlayed] == null) {
                        xPosPotential.add(xPosPlayed + 1);
                        yPosPotential.add(yPosPlayed);
                    }
                    if (board[xPosPlayed][yPosPlayed + 1] == null) {
                        xPosPotential.add(xPosPlayed);
                        yPosPotential.add(yPosPlayed + 1);
                    }
                }
                else if (cardName.split(":")[1].equals("0")) {
                    if (board[xPosPlayed -1][yPosPlayed] == null) {
                        xPosPotential.add(xPosPlayed - 1);
                        yPosPotential.add(yPosPlayed);
                    }
                    if (board[xPosPlayed + 1][yPosPlayed] == null) {
                        xPosPotential.add(xPosPlayed + 1);
                        yPosPotential.add(yPosPlayed);
                    }
                    isZero = true;
                }

                int target = 0;

                double smallestCartesianDistance = 100;

                for (int xxx = 0; xxx < xPosPotential.size(); xxx++){
                    int xDistance = (xPosPotential.get(xxx) - 12);
                    int yDistance = 0;
                    if (nuggetLocationKnown) {
                        yDistance = (yPosPotential.get(xxx) - nuggetY);
                        target = nuggetY;
                    }
                    else if (hiddenRevealed[0]) {
                        yDistance = (yPosPotential.get(xxx) - 6);
                        target = 6;
                    }
                    else if (hiddenRevealed[2]) {
                        yDistance = (yPosPotential.get(xxx) - 4);
                        target = 4;
                    }
                    else {
                        yDistance = (yPosPotential.get(xxx) - 5);
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


                if ((round == 1) && (isFive)){
                    smallestCartesianDistance = -100;
                    overrideSequenceSearch = true;
                }
                else if ((round <= 2) && (isZero)){
                    smallestCartesianDistance = -100;
                    overrideSequenceSearch = true;
                }
                else if (isNineOrEight && (target == 4) && (xPosPlayed == 12) && (yPosPlayed == 4)) {
                    smallestCartesianDistance = -100;
                    overrideSequenceSearch = true;
                }
                else if (isNineOrEight && (target == 6) && (xPosPlayed == 12) && (yPosPlayed == 6)) {
                    smallestCartesianDistance = -100;
                    overrideSequenceSearch = true;
                }
                else if (isZero && ((target == 6) || (target == 4)) && (xPosPlayed == 12) && ((yPosPlayed == 6) || (yPosPlayed == 4))) {
                    smallestCartesianDistance = 100;
                }

                System.out.println("smallestCartesianDistance: " + smallestCartesianDistance);
                System.out.println("bestDistance: " + bestDistance);


                if (smallestCartesianDistance < bestDistance) {
                    System.out.println("smallestCartesianDistance: " + smallestCartesianDistance);
                    System.out.println("bestDistance: " + bestDistance);
                    bestDistance = smallestCartesianDistance;
                    System.out.println("smallestCartesianDistance: " + smallestCartesianDistance);
                    System.out.println("bestDistance: " + bestDistance);
                    bestTile = c.getCardPlayed().getName().split(":")[1];
                    bestTileX = c.getPosPlayed()[0];
                    bestTileY = c.getPosPlayed()[1];
                }
            }
            if (c.getCardPlayed().getName().startsWith("Destroy")) {
                canPath = true;
                canDestroy = true;
                int xPosPlayed = c.getPosPlayed()[0];
                int yPosPlayed = c.getPosPlayed()[1];

                // clear the arrays to make new space
                xPosPotential.removeAll(xPosPotential);
                yPosPotential.removeAll(yPosPotential);
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
                    if (nuggetLocationKnown) {
                        yDistance = (yPosPotential.get(xxx) - nuggetY);
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

        else if (hasMapCard && hiddenCard && !nuggetLocationKnown) {
            if (!hiddenRevealed[0]) {
                myMove = new SaboteurMove(new SaboteurMap(),12,3,id);
                return myMove;
            }
            else if (!hiddenRevealed[2]) {
                myMove = new SaboteurMove(new SaboteurMap(),12,7,id);
                return myMove;
            }
            else if (!hiddenRevealed[1]) {
                myMove = new SaboteurMove(new SaboteurMap(),12,5,id);
                return myMove;
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
                myMove = new SaboteurMove((new SaboteurTile(bestTile)),bestTileX,bestTileY,id);
            }
        }

        //TODO: destroy strategically near the end
        /*else if (isMalus && winningPossibleForThem) {
            // get best destroy
        }*/

        round++;

        return myMove;
    }
}
package student_player;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurBonus;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurTile;

import java.util.ArrayList;
import java.util.Arrays;

public class MyTools {
    public static final int BOARD_SIZE = 14;
    public static final int originPos = 5;
    public static final int[][] hiddenPos = {{originPos+7,originPos-2},{originPos+7,originPos},{originPos+7,originPos+2}};

    public static SaboteurTile[][] addCardToBoard(SaboteurTile[][] board, SaboteurMove move){
        SaboteurTile[][] newBoard = new SaboteurTile[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null) {
                    newBoard[i][j] = (SaboteurTile) SaboteurCard.copyACard(board[i][j].getName());
                }
                else {
                    newBoard[i][j] = null;
                }
            }
        }
        SaboteurCard card = move.getCardPlayed();
        int[] pos = move.getPosPlayed();
        System.out.print("Adding card to the board: ");
        System.out.println(card.getName() + "," + pos[0] + "," + pos[1] + ".");
        if (card instanceof SaboteurDestroy) {
            newBoard[pos[0]][pos[1]] = null;
        }
        else if (card instanceof SaboteurTile) {
            newBoard[pos[0]][pos[1]] = new SaboteurTile(((SaboteurTile) card).getIdx());
        }
        return newBoard;
    }

    public static int[] lookForWinningSequence(boolean[] hiddenRevealed, SaboteurBoardState boardState, ArrayList<SaboteurCard> myHand, int[] targetPos) {
        int numCards = myHand.size();
        int minimumNumberOfMoves = numCards + 1;
        boolean firstMoveFlipped = false;
        boolean firstMoveFlippedCurrent = false;

        // int[0] is the index of the card to play
        // int[1] is the x index to play it at
        // int[2] is the y index to play it at
        // int[3] isFlipped
        // int[4-9] is the cards we want to keep (-1 for null)
        int[] result = new int[10];

        if (numCards == 0) {
            result[0] = -1;
            for (int i = 1; i < 10; i++) {
                result[i] = -1;
            }
            return result;
        }

        SaboteurTile[][] board = boardState.getHiddenBoard();

        int id = boardState.getTurnPlayer();


        ArrayList<SaboteurCard> currentHandA = new ArrayList<>();
        ArrayList<SaboteurCard> currentHandB = new ArrayList<>();
        ArrayList<SaboteurCard> currentHandC = new ArrayList<>();
        ArrayList<SaboteurCard> currentHandD = new ArrayList<>();
        ArrayList<SaboteurCard> currentHandE = new ArrayList<>();
        ArrayList<SaboteurCard> currentHandF = new ArrayList<>();
        ArrayList<SaboteurCard> currentHandG = new ArrayList<>();
        for (int sa = 0; sa < myHand.size(); sa++) {
            currentHandA.add(myHand.get(sa));
        }
        //System.out.println(currentHandA.toString());
        ArrayList<SaboteurMove> possibleMovesA = getPossibleMoves(hiddenRevealed, board, myHand, id);
        ArrayList<SaboteurMove> possibleMovesB = new ArrayList<>();
        ArrayList<SaboteurMove> possibleMovesC = new ArrayList<>();
        ArrayList<SaboteurMove> possibleMovesD = new ArrayList<>();
        ArrayList<SaboteurMove> possibleMovesE = new ArrayList<>();
        ArrayList<SaboteurMove> possibleMovesF = new ArrayList<>();
        ArrayList<SaboteurMove> possibleMovesG = new ArrayList<>();

        //System.out.println("The moves that my AI came up with");
        //System.out.println(movesToString(possibleMovesA));

        int currentFirstMoveX = -1;
        int currentFirstMoveY = -1;
        int bestFirstMoveX = -1;
        int bestFirstMoveY = -1;
        boolean[] playedCard = new boolean[numCards];
        int[] currentSequence = new int[numCards];
        int[] bestSequence = new int[numCards];
        bestSequence[0] = 0;

        int i = 0;
        A: while (i < possibleMovesA.size()) {
            //System.out.println("start function");
            //System.out.println(currentHandA.toString());
            //System.out.println(movesToString(possibleMovesA));
            currentHandA.removeAll(currentHandA);
            possibleMovesA.removeAll(possibleMovesA);
            //System.out.println(currentHandA.toString());
            //System.out.println(movesToString(possibleMovesA));
            for (int mh=0; mh<myHand.size(); mh++) {
                currentHandA.add(myHand.get(mh));
            }
            if (currentHandA.size() > 0) {
                possibleMovesA = getPossibleMoves(hiddenRevealed, board, currentHandA, id);
                //System.out.println(movesToString(possibleMovesA));
            }
            else {
                break A;
            }
            //intialize the board with no moves added to it
            SaboteurTile[][] boardA = boardState.getHiddenBoard();
            // reset current hand

            //initialize the playedCardList
            for (int p = 0; p < numCards; p++) {
                playedCard[p] = false;
            }

            //set the first card as played
            for (int cardIndex = 0; ((cardIndex < numCards) && (!playedCard[cardIndex])); cardIndex++) {
                String cardName = myHand.get(cardIndex).getName();
                String cardNameFlipped = cardName + "_flip";
                String moveCardName = possibleMovesA.get(i).getCardPlayed().getName();
                if (cardName.equals(moveCardName) || cardNameFlipped.equals(moveCardName)) {
                    if (playedCard[cardIndex]) {
                        continue A;
                    }
                    if (cardNameFlipped.equals(moveCardName)) {
                        firstMoveFlippedCurrent = true;
                    }
                    playedCard[cardIndex] = true;
                    currentSequence[0] = cardIndex;
                    SaboteurTile[][] boardB = addCardToBoard(boardA, possibleMovesA.get(i));

                    if (pathToHidden(boardB, targetPos)) {
                        if (cardNameFlipped.equals(moveCardName)) {
                            firstMoveFlipped = true;
                        }
                        bestSequence[0] = currentSequence[0];
                        bestFirstMoveX = possibleMovesA.get(i).getPosPlayed()[0];
                        bestFirstMoveY = possibleMovesA.get(i).getPosPlayed()[1];
                        minimumNumberOfMoves = 1;
                        System.out.println("Can win in one move!");
                        System.out.println("bestSequence[0]: " + bestSequence[0]);
                        //System.out.println(movesToString(possibleMovesA));
                        break A;
                    }
                    else {
                        //System.out.println(currentHandA.toString());
                        //System.out.println(possibleMovesA.toString());
                        int actualCardIndex = 0;
                        for (int ha=0; ha < currentHandA.size(); ha++) {
                            if (currentHandA.get(ha).getName().equals(possibleMovesA.get(i).getCardPlayed().getName())) {
                                actualCardIndex = ha;
                            }
                        }
                        currentHandB.removeAll(currentHandB);
                        for (int sa = 0; sa < currentHandA.size(); sa++) {
                            if (sa != actualCardIndex) {
                                currentHandB.add(currentHandA.get(sa));
                            }
                        }
                        possibleMovesB.removeAll(possibleMovesB);
                        System.out.println("currentHandB: " + currentHandB.toString());
                        if (currentHandB.size() > 0) {
                            possibleMovesB = getPossibleMoves(hiddenRevealed, boardB, currentHandB, id);
                        }

                        System.out.println("possibleMovesB: " + movesToString(possibleMovesB));
                        int b = 0;
                        B: while (b < possibleMovesB.size()) {
                            //set the card as played
                            for (cardIndex = 0; ((cardIndex < numCards) && (!playedCard[cardIndex])); cardIndex++) {
                                cardName = myHand.get(cardIndex).getName();
                                cardNameFlipped = cardName + "_flip";
                                moveCardName = possibleMovesB.get(b).getCardPlayed().getName();

                                if (cardName.equals(moveCardName) || cardNameFlipped.equals(moveCardName)) {
                                    if (playedCard[cardIndex]) {
                                        continue B;
                                    }
                                    playedCard[cardIndex] = true;
                                    currentSequence[1] = cardIndex;
                                    SaboteurTile[][] boardC = addCardToBoard(boardB, possibleMovesB.get(b));

                                    if (pathToHidden(boardC, targetPos) && (2 < minimumNumberOfMoves)) {
                                        firstMoveFlipped = firstMoveFlippedCurrent;
                                        System.out.println("Can win in 2 moves!");
                                        System.out.println("possibleMovesB: " + possibleMovesB);
                                        minimumNumberOfMoves = 2;
                                        bestFirstMoveX = possibleMovesA.get(i).getPosPlayed()[0];
                                        bestFirstMoveY = possibleMovesA.get(i).getPosPlayed()[1];
                                        bestSequence[0] = currentSequence[0];
                                        bestSequence[1] = cardIndex;
                                        break B;
                                    }
                                    else {
                                        System.out.println(currentHandB.toString());
                                        actualCardIndex = 0;
                                        for (int ha=0; ha < currentHandB.size(); ha++) {
                                            if (currentHandB.get(ha).getName().equals(possibleMovesB.get(b).getCardPlayed().getName())) {
                                                actualCardIndex = ha;
                                            }
                                        }
                                        //System.out.println(currentHandB.toString());
                                        currentHandC.removeAll(currentHandC);
                                        for (int sa = 0; sa < currentHandB.size(); sa++) {
                                            if (sa != actualCardIndex) {
                                                currentHandC.add(currentHandB.get(sa));
                                            }
                                        }
                                        //System.out.println(currentHandC.toString());
                                        possibleMovesC.removeAll(possibleMovesC);
                                        if (currentHandC.size() > 0) {
                                            possibleMovesC = getPossibleMoves(hiddenRevealed, boardC, currentHandC, id);
                                        }
                                        //System.out.println(movesToString(possibleMovesC));
                                        int c = 0;
                                        C: while (c < possibleMovesC.size()) {
                                            //set the card as played
                                            for (cardIndex = 0; ((cardIndex < numCards) && (!playedCard[cardIndex])); cardIndex++) {
                                                cardName = myHand.get(cardIndex).getName();
                                                cardNameFlipped = cardName + "_flip";
                                                moveCardName = possibleMovesC.get(c).getCardPlayed().getName();

                                                if (cardName.equals(moveCardName) || cardNameFlipped.equals(moveCardName)) {
                                                    if (playedCard[cardIndex]) {
                                                        continue C;
                                                    }
                                                    playedCard[cardIndex] = true;
                                                    currentSequence[2] = cardIndex;
                                                    SaboteurTile[][] boardD = addCardToBoard(boardC, possibleMovesC.get(c));

                                                    if (pathToHidden(boardD, targetPos) && (3 < minimumNumberOfMoves)) {
                                                        firstMoveFlipped = firstMoveFlippedCurrent;
                                                        System.out.println("Can win in 3 moves!");
                                                        //System.out.println(possibleMovesC);
                                                        minimumNumberOfMoves = 3;
                                                        bestFirstMoveX = possibleMovesA.get(i).getPosPlayed()[0];
                                                        bestFirstMoveY = possibleMovesA.get(i).getPosPlayed()[1];
                                                        bestSequence[0] = currentSequence[0];
                                                        bestSequence[1] = currentSequence[1];
                                                        bestSequence[2] = cardIndex;
                                                        break C;
                                                    }
                                                    else {
                                                        actualCardIndex = 0;
                                                        for (int ha=0; ha < currentHandC.size(); ha++) {
                                                            cardName = currentHandC.get(ha).getName();
                                                            cardNameFlipped = cardName + "_flip";
                                                            moveCardName = possibleMovesC.get(c).getCardPlayed().getName();
                                                            if (cardName.equals(moveCardName) || cardNameFlipped.equals(moveCardName)) {
                                                                actualCardIndex = ha;
                                                            }
                                                        }
                                                        currentHandD.removeAll(currentHandD);
                                                        for (int sa = 0; sa < currentHandC.size(); sa++) {
                                                            if (sa != actualCardIndex) {
                                                                currentHandD.add(currentHandC.get(sa));
                                                            }
                                                        }
                                                        //System.out.println(currentHandD.toString());
                                                        possibleMovesD.removeAll(possibleMovesD);
                                                        if (currentHandD.size() > 0) {
                                                            possibleMovesD = getPossibleMoves(hiddenRevealed, boardD, currentHandD, id);
                                                        }
                                                        //System.out.println(possibleMovesD.toString());
                                                        int d = 0;
                                                        D: while (d < possibleMovesD.size()) {
                                                            //set the card as played
                                                            for (cardIndex = 0; ((cardIndex < numCards) && (!playedCard[cardIndex])); cardIndex++) {
                                                                cardName = myHand.get(cardIndex).getName();
                                                                cardNameFlipped = cardName + "_flip";
                                                                moveCardName = possibleMovesD.get(d).getCardPlayed().getName();

                                                                if (cardName.equals(moveCardName) || cardNameFlipped.equals(moveCardName)) {
                                                                    if (playedCard[cardIndex]) {
                                                                        continue D;
                                                                    }
                                                                    playedCard[cardIndex] = true;
                                                                    currentSequence[3] = cardIndex;
                                                                    SaboteurTile[][] boardE = addCardToBoard(boardD, possibleMovesD.get(d));

                                                                    if (pathToHidden(boardE, targetPos) && (4 < minimumNumberOfMoves)) {
                                                                        firstMoveFlipped = firstMoveFlippedCurrent;
                                                                        System.out.println("Can win in 4 moves!");
                                                                        //System.out.println(possibleMovesD);
                                                                        minimumNumberOfMoves = 4;
                                                                        bestFirstMoveX = possibleMovesA.get(i).getPosPlayed()[0];
                                                                        bestFirstMoveY = possibleMovesA.get(i).getPosPlayed()[1];
                                                                        bestSequence[0] = currentSequence[0];
                                                                        bestSequence[1] = currentSequence[1];
                                                                        bestSequence[2] = currentSequence[2];
                                                                        bestSequence[3] = cardIndex;
                                                                        break D;
                                                                    }
                                                                    else {
                                                                        actualCardIndex = 0;
                                                                        for (int ha=0; ha < currentHandD.size(); ha++) {
                                                                            cardName = currentHandD.get(ha).getName();
                                                                            cardNameFlipped = cardName + "_flip";
                                                                            moveCardName = possibleMovesD.get(d).getCardPlayed().getName();
                                                                            if (cardName.equals(moveCardName) || cardNameFlipped.equals(moveCardName)) {
                                                                                actualCardIndex = ha;
                                                                            }
                                                                        }
                                                                        currentHandE.removeAll(currentHandE);
                                                                        for (int sa = 0; sa < currentHandD.size(); sa++) {
                                                                            if (sa != actualCardIndex) {
                                                                                currentHandE.add(currentHandD.get(sa));
                                                                            }
                                                                        }
                                                                        //System.out.println(currentHandE.toString());
                                                                        possibleMovesE.removeAll(possibleMovesE);
                                                                        if (currentHandE.size() > 0) {
                                                                            possibleMovesE = getPossibleMoves(hiddenRevealed, boardE, currentHandE, id);
                                                                        }
                                                                        //System.out.println(possibleMovesE.toString());
                                                                        int e = 0;
                                                                        E: while (e < possibleMovesE.size()) {
                                                                            //set the card as played
                                                                            for (cardIndex = 0; ((cardIndex < numCards) && (!playedCard[cardIndex])); cardIndex++) {
                                                                                cardName = myHand.get(cardIndex).getName();
                                                                                cardNameFlipped = cardName + "_flip";
                                                                                moveCardName = possibleMovesE.get(e).getCardPlayed().getName();

                                                                                if (cardName.equals(moveCardName) || cardNameFlipped.equals(moveCardName)) {
                                                                                    if (playedCard[cardIndex]) {
                                                                                        continue E;
                                                                                    }
                                                                                    playedCard[cardIndex] = true;
                                                                                    currentSequence[4] = cardIndex;
                                                                                    SaboteurTile[][] boardF = addCardToBoard(boardE, possibleMovesE.get(e));

                                                                                    if (pathToHidden(boardF, targetPos) && (5 < minimumNumberOfMoves)) {
                                                                                        firstMoveFlipped = firstMoveFlippedCurrent;
                                                                                        System.out.println("Can win in 5 moves!");
                                                                                        //System.out.println(possibleMovesE);
                                                                                        minimumNumberOfMoves = 5;
                                                                                        bestFirstMoveX = possibleMovesA.get(i).getPosPlayed()[0];
                                                                                        bestFirstMoveY = possibleMovesA.get(i).getPosPlayed()[1];
                                                                                        bestSequence[0] = currentSequence[0];
                                                                                        bestSequence[1] = currentSequence[1];
                                                                                        bestSequence[2] = currentSequence[2];
                                                                                        bestSequence[3] = currentSequence[3];
                                                                                        bestSequence[4] = cardIndex;
                                                                                        break E;
                                                                                    }
                                                                                    else {
                                                                                        actualCardIndex = 0;
                                                                                        for (int ha=0; ha < currentHandE.size(); ha++) {
                                                                                            cardName = currentHandE.get(ha).getName();
                                                                                            cardNameFlipped = cardName + "_flip";
                                                                                            moveCardName = possibleMovesE.get(e).getCardPlayed().getName();
                                                                                            if (cardName.equals(moveCardName) || cardNameFlipped.equals(moveCardName)) {
                                                                                                actualCardIndex = ha;
                                                                                            }
                                                                                        }
                                                                                        currentHandF.removeAll(currentHandF);
                                                                                        for (int sa = 0; sa < currentHandE.size(); sa++) {
                                                                                            if (sa != actualCardIndex) {
                                                                                                currentHandF.add(currentHandE.get(sa));
                                                                                            }
                                                                                        }
                                                                                        //System.out.println(currentHandF.toString());
                                                                                        possibleMovesF.removeAll(possibleMovesF);
                                                                                        if (currentHandF.size() > 0) {
                                                                                            possibleMovesF = getPossibleMoves(hiddenRevealed, boardF, currentHandF, id);
                                                                                        }
                                                                                        //System.out.println(possibleMovesF.toString());
                                                                                        int f = 0;
                                                                                        F: while (f < possibleMovesF.size()) {
                                                                                            //set the card as played
                                                                                            for (cardIndex = 0; ((cardIndex < numCards) && (!playedCard[cardIndex])); cardIndex++) {
                                                                                                cardName = myHand.get(cardIndex).getName();
                                                                                                cardNameFlipped = cardName + "_flip";
                                                                                                moveCardName = possibleMovesF.get(f).getCardPlayed().getName();

                                                                                                if (cardName.equals(moveCardName) || cardNameFlipped.equals(moveCardName)) {
                                                                                                    if (playedCard[cardIndex]) {
                                                                                                        continue F;
                                                                                                    }
                                                                                                    playedCard[cardIndex] = true;
                                                                                                    currentSequence[5] = cardIndex;
                                                                                                    SaboteurTile[][] boardG = addCardToBoard(boardF, possibleMovesF.get(f));

                                                                                                    if (pathToHidden(boardG, targetPos) && (6 < minimumNumberOfMoves)) {
                                                                                                        firstMoveFlipped = firstMoveFlippedCurrent;
                                                                                                        System.out.println("Can win in 6 moves!");
                                                                                                        //System.out.println(possibleMovesF);
                                                                                                        minimumNumberOfMoves = 6;
                                                                                                        bestFirstMoveX = possibleMovesA.get(i).getPosPlayed()[0];
                                                                                                        bestFirstMoveY = possibleMovesA.get(i).getPosPlayed()[1];
                                                                                                        bestSequence[0] = currentSequence[0];
                                                                                                        bestSequence[1] = currentSequence[1];
                                                                                                        bestSequence[2] = currentSequence[2];
                                                                                                        bestSequence[3] = currentSequence[3];
                                                                                                        bestSequence[4] = currentSequence[4];
                                                                                                        bestSequence[5] = cardIndex;
                                                                                                        break F;
                                                                                                    }
                                                                                                    else {
                                                                                                        actualCardIndex = 0;
                                                                                                        for (int ha=0; ha < currentHandF.size(); ha++) {
                                                                                                            cardName = currentHandF.get(ha).getName();
                                                                                                            cardNameFlipped = cardName + "_flip";
                                                                                                            moveCardName = possibleMovesF.get(f).getCardPlayed().getName();
                                                                                                            if (cardName.equals(moveCardName) || cardNameFlipped.equals(moveCardName)) {
                                                                                                                actualCardIndex = ha;
                                                                                                            }
                                                                                                        }
                                                                                                        currentHandG.removeAll(currentHandG);
                                                                                                        for (int sa = 0; sa < currentHandF.size(); sa++) {
                                                                                                            if (sa != actualCardIndex) {
                                                                                                                currentHandG.add(currentHandF.get(sa));
                                                                                                            }
                                                                                                        }
                                                                                                        //System.out.println(currentHandG.toString());
                                                                                                        possibleMovesG.removeAll(possibleMovesG);
                                                                                                        if (currentHandG.size() > 0) {
                                                                                                            possibleMovesG = getPossibleMoves(hiddenRevealed, boardG, currentHandG, id);
                                                                                                        }
                                                                                                        //System.out.println(possibleMovesG.toString());
                                                                                                        int g = 0;
                                                                                                        G: while (g < possibleMovesG.size()) {
                                                                                                            //set the card as played
                                                                                                            for (cardIndex = 0; ((cardIndex < numCards) && (!playedCard[cardIndex])); cardIndex++) {
                                                                                                                cardName = myHand.get(cardIndex).getName();
                                                                                                                cardNameFlipped = cardName + "_flip";
                                                                                                                moveCardName = possibleMovesG.get(g).getCardPlayed().getName();

                                                                                                                if (cardName.equals(moveCardName) || cardNameFlipped.equals(moveCardName)) {
                                                                                                                    if (playedCard[cardIndex]) {
                                                                                                                        continue G;
                                                                                                                    }
                                                                                                                    playedCard[cardIndex] = true;
                                                                                                                    currentSequence[6] = cardIndex;
                                                                                                                    SaboteurTile[][] boardH = addCardToBoard(boardG, possibleMovesG.get(g));

                                                                                                                    if (pathToHidden(boardH, targetPos) && (7 < minimumNumberOfMoves)) {
                                                                                                                        firstMoveFlipped = firstMoveFlippedCurrent;
                                                                                                                        System.out.println("Can win in 7 moves!");
                                                                                                                        //System.out.println(possibleMovesG);
                                                                                                                        minimumNumberOfMoves = 7;
                                                                                                                        bestFirstMoveX = possibleMovesA.get(i).getPosPlayed()[0];
                                                                                                                        bestFirstMoveY = possibleMovesA.get(i).getPosPlayed()[1];
                                                                                                                        bestSequence[0] = currentSequence[0];
                                                                                                                        bestSequence[1] = currentSequence[1];
                                                                                                                        bestSequence[2] = currentSequence[2];
                                                                                                                        bestSequence[3] = currentSequence[3];
                                                                                                                        bestSequence[4] = currentSequence[4];
                                                                                                                        bestSequence[5] = currentSequence[5];
                                                                                                                        bestSequence[6] = cardIndex;
                                                                                                                        break G;
                                                                                                                    }
                                                                                                                    break;
                                                                                                                }
                                                                                                            }
                                                                                                            g++;
                                                                                                        }
                                                                                                    }
                                                                                                    break;
                                                                                                }
                                                                                            }
                                                                                            f++;
                                                                                        }
                                                                                    }
                                                                                    break;
                                                                                }
                                                                            }
                                                                            e++;
                                                                        }
                                                                    }
                                                                    break;
                                                                }
                                                            }
                                                            d++;
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                            c++;
                                        }
                                    }
                                    break;
                                }
                            }
                            b++;
                        }
                    }

                    break;
                }
            }
            i++;
        }

        if (minimumNumberOfMoves == numCards + 1) {
            result[0] = -1;
            for (int l = 1; l < 10; l++) {
                result[l] = -1;
            }
        }
        else {
            //System.out.println("\n\nRESULTS OF LOOKING FOR WINNING SEQUENCE");
            //System.out.println(handToString(myHand));
            result[0] = bestSequence[0];
            //System.out.println("result[0]: " + result[0]);
            result[1] = bestFirstMoveX;
            //System.out.println("result[1]: " + result[1]);
            result[2] = bestFirstMoveY;
            //System.out.println("result[2]: " + result[2]);
            if (firstMoveFlipped) {
                result[3] = 1;
            }
            else {
                result[3] = 0;
            }
            //System.out.println("result[4]: " + result[4]);
            for (int z = 0; z < minimumNumberOfMoves - 1; z++) {
                result[z+4] = bestSequence[z + 1];
            }
            for (int z = minimumNumberOfMoves - 1; z < 14; z++) {
                result[z+4] = -1;
            }
        }
        return result;
    }

    public static boolean checkIfEnemyCanWin(boolean[] hiddenRevealed, SaboteurBoardState boardState, int[] targetPos) {
        boolean result = false;

        for (int i = 0; i < 50; i++) {
            ArrayList<SaboteurCard> randomHand = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                int randomNum = (int)(Math.random()*((4-1)+1))+1;
                //System.out.println("randomNum: " + randomNum);
                int randomTile = (int)(Math.random()*((10-0)+0))+0;
                //System.out.println("otherRandomNum: " + randomTile);
                int randomFlip = (int)(Math.random()*((2-1)+1))+1;
                //System.out.println("randomFlip: " + randomFlip);


                if (randomNum < 4) {
                    if (randomTile == 0 || randomTile == 1 || randomTile == 10) {
                        //System.out.println("creating non-flippable tile");
                        SaboteurCard randomCard = new SaboteurTile("" + randomTile);
                        randomHand.add(randomCard);
                    }
                    else {
                        //System.out.println("creating flippable tile");
                        if (randomFlip == 1) {
                            SaboteurCard randomCard = new SaboteurTile("" + randomTile);
                            randomHand.add(randomCard);
                        }
                        else if (randomFlip == 2) {
                            SaboteurCard randomCard = new SaboteurTile("" + randomTile + "_flip");
                            randomHand.add(randomCard);
                        }
                    }

                }
                if (randomNum == 4) {
                    SaboteurCard randomCard = new SaboteurDestroy();
                    randomHand.add(randomCard);
                }
            }

            System.out.println("Randomly generated enemy hand: " + randomHand);
            int[] resultArray = lookForWinningSequence(hiddenRevealed, boardState, randomHand, targetPos);
            if (resultArray[0] != -1) {
                result = true;
            }
        }
        //System.out.println(result);
        return result;
    }


    public static ArrayList<SaboteurMove> getPossibleMoves(boolean[] hiddenRevealed, SaboteurTile[][] board, ArrayList<SaboteurCard> hand, int id) {
        ArrayList<SaboteurMove> legalMoves = new ArrayList<>();

        for(SaboteurCard card : hand){
            if( card instanceof SaboteurTile) {
                ArrayList<int[]> allowedPositions = positionsForTile(hiddenRevealed, board, (SaboteurTile)card);
                for(int[] pos:allowedPositions){
                    legalMoves.add(new SaboteurMove(card,pos[0],pos[1],id));
                }
                //if the card can be flipped, we also had legal moves where the card is flipped;
                if(SaboteurTile.canBeFlipped(((SaboteurTile)card).getIdx())){
                    SaboteurTile flippedCard = ((SaboteurTile)card).getFlipped();
                    ArrayList<int[]> allowedPositionsflipped = positionsForTile(hiddenRevealed, board, flippedCard);
                    for(int[] pos:allowedPositionsflipped){
                        legalMoves.add(new SaboteurMove(flippedCard,pos[0],pos[1],id));
                    }
                }
            }
            else if(card instanceof SaboteurDestroy){
                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) { //we can't destroy an empty tile, the starting, or final tiles.
                        if(board[i][j] != null && (i!=originPos || j!= originPos) && (i != hiddenPos[0][0] || j!=hiddenPos[0][1] )
                                && (i != hiddenPos[1][0] || j!=hiddenPos[1][1] ) && (i != hiddenPos[2][0] || j!=hiddenPos[2][1] ) ){
                            legalMoves.add(new SaboteurMove(card,i,j,id));
                        }
                    }
                }
            }
        }
        return legalMoves;
    }

    public static ArrayList<int[]> positionsForTile(boolean[] hiddenRevealed, SaboteurTile[][] board, SaboteurTile card) {
        ArrayList<int[]> positions = new ArrayList<int[]>();
        int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}}; //to make the test faster, we simply verify around all already placed tiles.
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null) {
                    for (int m = 0; m < 4; m++) {
                        if (0 <= i+moves[m][0] && i+moves[m][0] < BOARD_SIZE && 0 <= j+moves[m][1] && j+moves[m][1] < BOARD_SIZE) {
                            if (verifyLegit(hiddenRevealed, board, card.getPath(), new int[]{i + moves[m][0], j + moves[m][1]} )){
                                positions.add(new int[]{i + moves[m][0], j +moves[m][1]});
                            }
                        }
                    }
                }
            }
        }
        return positions;
    }

    public static boolean verifyLegit(boolean[] hiddenRevealed, SaboteurTile[][] board, int[][] path,int[] pos){
        if (!(0 <= pos[0] && pos[0] < BOARD_SIZE && 0 <= pos[1] && pos[1] < BOARD_SIZE)) {
            return false;
        }
        if(board[pos[0]][pos[1]] != null) return false;

        //the following integer are used to make sure that at least one path exists between the possible new tile to be added and existing tiles.
        // There are 2 cases:  a tile can't be placed near an hidden objective and a tile can't be connected only by a wall to another tile.
        int requiredEmptyAround=4;
        int numberOfEmptyAround=0;

        ArrayList<SaboteurTile> objHiddenList=new ArrayList<>();
        for(int i=0;i<3;i++) {
            if (!hiddenRevealed[i]){
                objHiddenList.add(board[hiddenPos[i][0]][hiddenPos[i][1]]);
            }
        }
        //verify left side:
        if(pos[1]>0) {
            SaboteurTile neighborCard = board[pos[0]][pos[1] - 1];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                if (path[0][0] != neighborPath[2][0] || path[0][1] != neighborPath[2][1] || path[0][2] != neighborPath[2][2] ) return false;
                else if(path[0][0] == 0 && path[0][1]== 0 && path[0][2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify right side
        if(pos[1]<BOARD_SIZE-1) {
            SaboteurTile neighborCard = board[pos[0]][pos[1] + 1];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                //System.out.println("neighborPath: " + neighborPath);
                //System.out.println("path: " + path);
                if (path[2][0] != neighborPath[0][0] || path[2][1] != neighborPath[0][1] || path[2][2] != neighborPath[0][2]) return false;
                else if(path[2][0] == 0 && path[2][1]== 0 && path[2][2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify upper side
        if(pos[0]>0) {
            SaboteurTile neighborCard = board[pos[0]-1][pos[1]];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                int[] p={path[0][2],path[1][2],path[2][2]};
                int[] np={neighborPath[0][0],neighborPath[1][0],neighborPath[2][0]};
                if (p[0] != np[0] || p[1] != np[1] || p[2] != np[2]) return false;
                else if(p[0] == 0 && p[1]== 0 && p[2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify bottom side:
        if(pos[0]<BOARD_SIZE-1) {
            SaboteurTile neighborCard = board[pos[0]+1][pos[1]];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                int[] p={path[0][0],path[1][0],path[2][0]};
                int[] np={neighborPath[0][2],neighborPath[1][2],neighborPath[2][2]};
                if (p[0] != np[0] || p[1] != np[1] || p[2] != np[2]) return false;
                else if(p[0] == 0 && p[1]== 0 && p[2] ==0 ) numberOfEmptyAround +=1; //we are touching by a wall
            }
        }
        else numberOfEmptyAround+=1;

        if(numberOfEmptyAround==requiredEmptyAround)  return false;

        return true;
    }

    private static boolean pathToHidden(SaboteurTile[][] board, int[] targetPos){
        /* This function look if a path is linking the starting point to the states among objectives.
            :return: if there exists one: true
                     if not: false
                     In Addition it changes each reached states hidden variable to true:  self.hidden[foundState] <- true
            Implementation details:
            For each hidden objectives:
                We verify there is a path of cards between the start and the hidden objectives.
                    If there is one, we do the same but with the 0-1s matrix!

            To verify a path, we use a simple search algorithm where we propagate a front of visited neighbor.
               TODO To speed up: The neighbor are added ranked on their distance to the origin... (simply use a PriorityQueue with a Comparator)
        */
        int[][] intBoard = new int[44][44];

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if(board[i][j] == null){
                    for (int k = 0; k < 3; k++) {
                        for (int h = 0; h < 3; h++) {
                            intBoard[i * 3 + k][j * 3 + h] = -1;
                        }
                    }
                }
                else {
                    int[][] path = board[i][j].getPath();
                    for (int k = 0; k < 3; k++) {
                        for (int h = 0; h < 3; h++) {
                            intBoard[i * 3 + k][j * 3 + h] = path[h][2-k];
                        }
                    }
                }
            }
        }

        boolean atLeastOnefound = false;
        ArrayList<int[]> originTargets = new ArrayList<>();
        originTargets.add(new int[]{originPos,originPos}); //the starting points

        if (cardPath(board, intBoard, originTargets, targetPos, true)) { //checks that there is a cardPath
            //next: checks that there is a path of ones.
            ArrayList<int[]> originTargets2 = new ArrayList<>();
            //the starting points
            originTargets2.add(new int[]{originPos*3+1, originPos*3+1});
            originTargets2.add(new int[]{originPos*3+1, originPos*3+2});
            originTargets2.add(new int[]{originPos*3+1, originPos*3});
            originTargets2.add(new int[]{originPos*3, originPos*3+1});
            originTargets2.add(new int[]{originPos*3+2, originPos*3+1});
            //get the target position in 0-1 coordinate
            int[] targetPos2 = {targetPos[0]*3+1, targetPos[1]*3+1};
            if (cardPath(board, intBoard, originTargets2, targetPos2, false)) {
                atLeastOnefound =true;
            }
            else{
                //System.out.println("0-1 path was not found");
            }
        }

        //System.out.println("Checking for successful path.");
        //System.out.println(intBoardToString(intBoard));
        //System.out.println("pathfound: " + atLeastOnefound);

        return atLeastOnefound;
    }
    private static Boolean cardPath(SaboteurTile[][] board, int[][] intBoard, ArrayList<int[]> originTargets, int[] targetPos, Boolean usingCard){
        // the search algorithm, usingCard indicate weither we search a path of cards (true) or a path of ones (aka tunnel)(false).
        ArrayList<int[]> queue = new ArrayList<>(); //will store the current neighboring tile. Composed of position (int[]).
        ArrayList<int[]> visited = new ArrayList<int[]>(); //will store the visited tile with an Hash table where the key is the position the board.
        visited.add(targetPos);
        if(usingCard) addUnvisitedNeighborToQueue(board, intBoard, targetPos,queue,visited,BOARD_SIZE,usingCard);
        else addUnvisitedNeighborToQueue(board, intBoard, targetPos,queue,visited,BOARD_SIZE*3,usingCard);
        while(queue.size()>0){
            int[] visitingPos = queue.remove(0);
            if(containsIntArray(originTargets,visitingPos)){
                return true;
            }
            visited.add(visitingPos);
            if(usingCard) addUnvisitedNeighborToQueue(board, intBoard, visitingPos,queue,visited,BOARD_SIZE,usingCard);
            else addUnvisitedNeighborToQueue(board, intBoard, visitingPos,queue,visited,BOARD_SIZE*3,usingCard);
            //System.out.println(queue.size());
        }
        return false;
    }
    private static void addUnvisitedNeighborToQueue(SaboteurTile[][] board, int[][] intBoard, int[] pos, ArrayList<int[]> queue, ArrayList<int[]> visited, int maxSize, boolean usingCard){
        int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}};
        int i = pos[0];
        int j = pos[1];
        for (int m = 0; m < 4; m++) {
            if (0 <= i+moves[m][0] && i+moves[m][0] < maxSize && 0 <= j+moves[m][1] && j+moves[m][1] < maxSize) { //if the hypothetical neighbor is still inside the board
                int[] neighborPos = new int[]{i+moves[m][0],j+moves[m][1]};
                if(!containsIntArray(visited,neighborPos)){
                    if(usingCard && board[neighborPos[0]][neighborPos[1]]!=null) queue.add(neighborPos);
                    else if(!usingCard && intBoard[neighborPos[0]][neighborPos[1]]==1) queue.add(neighborPos);
                }
            }
        }
    }
    private static boolean containsIntArray(ArrayList<int[]> a, int[] o){ //the .equals used in Arraylist.contains is not working between arrays..
        if (o == null) {
            for (int i = 0; i < a.size(); i++) {
                if (a.get(i) == null)
                    return true;
            }
        } else {
            for (int i = 0; i < a.size(); i++) {
                if (Arrays.equals(o, a.get(i)))
                    return true;
            }
        }
        return false;
    }

    public static String movesToString(ArrayList<SaboteurMove> moves) {
        String result = "";
        for (int i = 0; i < moves.size(); i++) {
            SaboteurMove move = moves.get(i);
            result += move.getCardPlayed().getName() + ", " + move.getPosPlayed()[0] + "," + move.getPosPlayed()[1] + "; ";
        }
        return result;
    }

    public static String handToString(ArrayList<SaboteurCard> hand) {
        String result = "My hand: ";
        for (int i = 0; i < hand.size(); i++) {
            SaboteurCard card = hand.get(i);
            result += card.getName() + "; ";
        }
        return result;
    }

    public static String intBoardToString(int[][] intBoard) {
        StringBuilder boardString = new StringBuilder();
        for (int i = 0; i < BOARD_SIZE*3; i++) {
            for (int j = 0; j < BOARD_SIZE*3; j++) {
                boardString.append(intBoard[i][j]);
                boardString.append(",");
            }
            boardString.append("\n");
        }
        return boardString.toString();
    }
}
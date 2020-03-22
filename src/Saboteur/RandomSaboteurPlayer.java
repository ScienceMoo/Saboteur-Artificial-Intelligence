package Saboteur;

import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurMalus;
import Saboteur.cardClasses.SaboteurMap;
import Saboteur.cardClasses.SaboteurTile;
import boardgame.Move;

import java.util.ArrayList;

/**
 * @author mgrenander
 */
public class RandomSaboteurPlayer extends SaboteurPlayer {
    public RandomSaboteurPlayer() {
        super("RandomPlayer");
    }

    public RandomSaboteurPlayer(String name) {
        super(name);
    }

    @Override
    public Move chooseMove(SaboteurBoardState boardState) {
        // Is random the best you can do?
        SaboteurMove myMove = boardState.getRandomMove();

        int id = boardState.getTurnPlayer();

        System.out.println("RANDOM PLAYER");
        System.out.println("Current player is " + boardState.getTurnPlayer());
        //boardState.printBoard();

        ArrayList<SaboteurCard> myHand = boardState.getCurrentPlayerCards();

        ArrayList<SaboteurMove> possibleMoves = boardState.getAllLegalMoves();

        Object[] myHandList = myHand.toArray();
        Object[] myMoveList = possibleMoves.toArray();

        int hiddenx = 0;
        int hiddeny = 0;
        boolean hiddenCard = false;
        boolean nuggetLocationKnown = false;

        SaboteurTile[][] board = boardState.getHiddenBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if(board[i][j] != null) {
                    if (board[i][j].getName().equals("Tile:nugget")) {
                        System.out.println("Nugget location: " + i + " " + j + " ");
                        nuggetLocationKnown = true;
                    }
                    if (board[i][j].getName().equals("Tile:8")) {
                        hiddenCard = true;
                        System.out.println("Hidden location: " + i + " " + j + " ");
                        hiddenx = i;
                        hiddeny = j;
                    }
                    System.out.println(board[i][j].getName());
                }
            }
        }

        // FIND OUT WHAT CARDS I HAVE
        boolean hasMapCard = false;
        boolean hasMalusCard = false;

        System.out.println("My hand: ");
        for (int i = 0; i < myHand.size(); i++) {
            SaboteurCard c = myHand.get(i);

            System.out.print(c.getName() + ", ");
            if (c.getName().equals("Map")) {
                hasMapCard = true;
            }
            if (c.getName().equals("Malus")) {
                hasMalusCard = true;
            }
        }
        System.out.print("\n");
        System.out.println("Do I have map card? " + hasMapCard);

        System.out.println("My possible moves: ");
        for (int i = 0; i < possibleMoves.size(); i++) {
            SaboteurMove c = possibleMoves.get(i);

            System.out.print("Card: " + c.getCardPlayed().getName() + ", ");
            System.out.print("X_pos: " + c.getPosPlayed()[0] + ", ");
            System.out.print("Y_pos: " + c.getPosPlayed()[1]);
        }
        System.out.print("\n");

        if (hasMalusCard) {
            myMove = new SaboteurMove((new SaboteurMalus()),0,0,id);
            return myMove;
        }

        else if (hasMapCard && hiddenCard && !nuggetLocationKnown) {
            myMove = new SaboteurMove(new SaboteurMap(),hiddenx,hiddeny,id);
            return myMove;
        }


        // OPENING MOVE
        // Follow normal strategy but without using the destroy card (because there's no cards to destroy

        //GENERAL STRATEGY
        // CHECK FOR WINNING MOVE, AND DO IT... otherwise:

        // FIRST USE MALUS
        // Destroy the other player's chances of getting anywhere early on

        // FIRST USE MAP CARD
        // Check if you have a Map card
        // Calculate x position of the objective card I want to see (maybe eliminate the farthest one first)
        // Get the y position
        // Include the player ID

        // IF THERE's A MALUS
        // fix it with a bonus card
        // if no bonus, discard a dead-end path card

        // MOVE TOWARDS OBJECTIVES
        // If all the objectives are hidden, go towards the middle one
        // Prioritize map cards with more options

        // IF All PATHS BRING YOU FARTHER AWAY
        // Use a destroy card to destroy a dead-end that is closest

        // Return your move to be processed by the server.
        return myMove;
    }
}

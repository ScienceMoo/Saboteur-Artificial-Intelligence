package student_player;

import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurMap;
import Saboteur.cardClasses.SaboteurTile;
import boardgame.Move;

import Saboteur.SaboteurPlayer;
import Saboteur.SaboteurBoardState;

import java.util.ArrayList;

/** A player file submitted by a student. */
public class StudentPlayer extends SaboteurPlayer {

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
        MyTools.getSomething();

        // Is random the best you can do?
        SaboteurMove myMove = boardState.getRandomMove();

        int id = boardState.getTurnPlayer();

        System.out.println("Choosing best move");
        System.out.println("Current player is " + boardState.getTurnPlayer());
        //boardState.printBoard();

        ArrayList<SaboteurCard> myHand = boardState.getCurrentPlayerCards();

        ArrayList<SaboteurMove> possibleMoves = boardState.getAllLegalMoves();

        Object[] myHandList = myHand.toArray();
        Object[] myMoveList = possibleMoves.toArray();

        int hiddenx = 0;
        int hiddeny = 0;
        boolean hiddenCard = false;

        SaboteurTile[][] board = boardState.getHiddenBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if(board[i][j] != null) {
                    if (board[i][j].getName().equals("Tile:nugget")) {
                        System.out.println("Nugget location: " + i + " " + j + " ");
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


        boolean hasMapCard = false;
        for (int i = 0; i < myHand.size(); i++) {
            SaboteurCard c = myHand.get(i);
            if (c.getName().equals("Map")) {
                hasMapCard = true;
            }
        }
        System.out.println("Do I have map card? " + hasMapCard);

        if (hasMapCard && hiddenCard) {
            myMove = new SaboteurMove(new SaboteurMap(),hiddenx,hiddeny,id);
            return myMove;
        }

        for (int j = 0; j < myMoveList.length; j++) {
            System.out.println(myMoveList[j].toString());
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
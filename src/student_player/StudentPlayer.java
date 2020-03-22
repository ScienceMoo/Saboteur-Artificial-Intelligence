package student_player;

import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurMap;
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
        boardState.printBoard();

        ArrayList<SaboteurCard> myHand = boardState.getCurrentPlayerCards();

        ArrayList<SaboteurMove> possibleMoves = boardState.getAllLegalMoves();

        Object[] myHandList = myHand.toArray();
        Object[] myMoveList = possibleMoves.toArray();

        boolean hasMapCard = false;
        for (int i = 0; i < myHand.size(); i++) {

        }

        for (int j = 0; j < myMoveList.length; j++) {
            System.out.println(myMoveList[j].toString());
        }

        // Check if you have a Map card
        // Calculate x position of the objective card I want to see (maybe eliminate the farthest one first)
        // Get the y position
        // Include the player ID

        // Return your move to be processed by the server.
        return myMove;
    }
}
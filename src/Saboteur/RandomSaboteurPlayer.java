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

        return myMove;
    }
}

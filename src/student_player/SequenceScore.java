package student_player;

import java.util.ArrayList;

import Saboteur.SaboteurMove;

public class SequenceScore {
    public int minCardsToReachEnd = 100;
    public ArrayList<SaboteurMove> moves;

    public SequenceScore(int minCards) {
        this.minCardsToReachEnd = minCards;
        this.moves = new ArrayList<>();
    }
}


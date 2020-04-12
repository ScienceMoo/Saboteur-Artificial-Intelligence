package student_player;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurTile;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static Saboteur.SaboteurBoardState.*;

public class MyTools {
    private static long startTimeMs;

    public static ArrayList<SaboteurMove> lookForWinningSequence(
        SaboteurBoardState boardState,
        ArrayList<SaboteurCard> cards,
        int[] targetPos,
        boolean[] hiddenRevealed
    ) {
        startTimeMs = getCurTimeMs();
        SequenceScore score = DepthFirstSearch(hiddenRevealed, cards, boardState.getHiddenBoard(), boardState.getTurnPlayer(), targetPos, 0);

        if (score.minCardsToReachEnd < 10 && !score.moves.isEmpty()) {
            return score.moves;
        } else {
            return null;
        }
    }

    private static long getCurTimeMs() {
        return new Timestamp((new Date()).getTime()).getTime();
    }

    private static SequenceScore DepthFirstSearch(
        boolean[] hiddenRevealed,
        ArrayList<SaboteurCard> remainingCards,
        SaboteurTile[][] board,
        int id,
        int[] targetPos,
        int depth
    ) {
        if (depth > 0 && pathToHidden(board, targetPos)) {
            return new SequenceScore(0);
        }

        if (depth >= 3 || (getCurTimeMs() - startTimeMs > 1900)) {
            return new SequenceScore(100);
        }

        SequenceScore bestSequenceScore = new SequenceScore(100);
        for (SaboteurCard card : remainingCards) {
            ArrayList<SaboteurMove> possibleMoves = getPossibleMoves(hiddenRevealed, board, card, id);

            for (SaboteurMove move : possibleMoves) {
                SaboteurTile[][] newBoard = addCardToBoard(board, move);

                ArrayList<SaboteurCard> newRemainingCards = new ArrayList<>(remainingCards);
                newRemainingCards.remove(card);

                SequenceScore sequenceScore = DepthFirstSearch(hiddenRevealed, newRemainingCards, newBoard, id, targetPos, depth + 1);
                sequenceScore.minCardsToReachEnd += 1;

                if (sequenceScore.minCardsToReachEnd < bestSequenceScore.minCardsToReachEnd) {
                    sequenceScore.moves.add(0, move);
                    bestSequenceScore = sequenceScore;
                }
            }
        }

        return bestSequenceScore;
    }

    private static SaboteurTile[][] addCardToBoard(SaboteurTile[][] board, SaboteurMove move){
        SaboteurTile[][] newBoard = new SaboteurTile[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(board[i], 0, newBoard[i], 0, BOARD_SIZE);
        }
        SaboteurCard card = move.getCardPlayed();
        int[] pos = move.getPosPlayed();
        if (card instanceof SaboteurDestroy) {
            newBoard[pos[0]][pos[1]] = null;
        }
        else if (card instanceof SaboteurTile) {
            newBoard[pos[0]][pos[1]] = new SaboteurTile(((SaboteurTile) card).getIdx());
        }
        return newBoard;
    }

    private static ArrayList<SaboteurMove> getPossibleMoves(boolean[] hiddenRevealed, SaboteurTile[][] board, SaboteurCard card, int id) {
        ArrayList<SaboteurMove> legalMoves = new ArrayList<>();

        if (card instanceof SaboteurTile) {
            ArrayList<int[]> allowedPositions = positionsForTile(hiddenRevealed, board, (SaboteurTile) card);
            for (int[] pos : allowedPositions) {
                legalMoves.add(new SaboteurMove(card, pos[0], pos[1], id));
            }
            //if the card can be flipped, we also had legal moves where the card is flipped;
            if (SaboteurTile.canBeFlipped(((SaboteurTile) card).getIdx())) {
                SaboteurTile flippedCard = ((SaboteurTile) card).getFlipped();
                ArrayList<int[]> allowedPositionsflipped = positionsForTile(hiddenRevealed, board, flippedCard);
                for (int[] pos : allowedPositionsflipped) {
                    legalMoves.add(new SaboteurMove(flippedCard, pos[0], pos[1], id));
                }
            }
        } else if (card instanceof SaboteurDestroy) {
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) { //we can't destroy an empty tile, the starting, or final tiles.
                    if (board[i][j] != null && (i != originPos || j != originPos) && (i != hiddenPos[0][0] || j != hiddenPos[0][1])
                            && (i != hiddenPos[1][0] || j != hiddenPos[1][1]) && (i != hiddenPos[2][0] || j != hiddenPos[2][1])) {
                        legalMoves.add(new SaboteurMove(card, i, j, id));
                    }
                }
            }
        }
        return legalMoves;
    }

    private static ArrayList<int[]> positionsForTile(boolean[] hiddenRevealed, SaboteurTile[][] board, SaboteurTile card) {
        ArrayList<int[]> positions = new ArrayList<>();
        int[][] moves = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}}; //to make the test faster, we simply verify around all already placed tiles.
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null) {
                    for (int m = 0; m < 4; m++) {
                        if (0 <= i + moves[m][0] && i + moves[m][0] < BOARD_SIZE && 0 <= j + moves[m][1] && j + moves[m][1] < BOARD_SIZE) {
                            if (verifyLegit(hiddenRevealed, board, card.getPath(), new int[]{i + moves[m][0], j + moves[m][1]})) {
                                positions.add(new int[]{i + moves[m][0], j + moves[m][1]});
                            }
                        }
                    }
                }
            }
        }
        return positions;
    }

    private static boolean verifyLegit(boolean[] hiddenRevealed, SaboteurTile[][] board, int[][] path, int[] pos) {
        if (!(0 <= pos[0] && pos[0] < BOARD_SIZE && 0 <= pos[1] && pos[1] < BOARD_SIZE)) {
            return false;
        }
        if (board[pos[0]][pos[1]] != null) return false;

        //the following integer are used to make sure that at least one path exists between the possible new tile to be added and existing tiles.
        // There are 2 cases:  a tile can't be placed near an hidden objective and a tile can't be connected only by a wall to another tile.
        int requiredEmptyAround = 4;
        int numberOfEmptyAround = 0;

        ArrayList<SaboteurTile> objHiddenList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (!hiddenRevealed[i]) {
                objHiddenList.add(board[hiddenPos[i][0]][hiddenPos[i][1]]);
            }
        }
        //verify left side:
        if (pos[1] > 0) {
            SaboteurTile neighborCard = board[pos[0]][pos[1] - 1];
            if (neighborCard == null)
                numberOfEmptyAround += 1;
            else if (objHiddenList.contains(neighborCard))
                requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                if (path[0][0] != neighborPath[2][0] || path[0][1] != neighborPath[2][1] || path[0][2] != neighborPath[2][2])
                    return false;
                else if (path[0][0] == 0 && path[0][1] == 0 && path[0][2] == 0)
                    numberOfEmptyAround += 1;
            }
        } else
            numberOfEmptyAround += 1;

        //verify right side
        if (pos[1] < BOARD_SIZE - 1) {
            SaboteurTile neighborCard = board[pos[0]][pos[1] + 1];
            if (neighborCard == null)
                numberOfEmptyAround += 1;
            else if (objHiddenList.contains(neighborCard))
                requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                //System.out.println("neighborPath: " + neighborPath);
                //System.out.println("path: " + path);
                if (path[2][0] != neighborPath[0][0] || path[2][1] != neighborPath[0][1] || path[2][2] != neighborPath[0][2])
                    return false;
                else if (path[2][0] == 0 && path[2][1] == 0 && path[2][2] == 0)
                    numberOfEmptyAround += 1;
            }
        } else
            numberOfEmptyAround += 1;

        //verify upper side
        if (pos[0] > 0) {
            SaboteurTile neighborCard = board[pos[0] - 1][pos[1]];
            if (neighborCard == null)
                numberOfEmptyAround += 1;
            else if (objHiddenList.contains(neighborCard))
                requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                int[] p = {path[0][2], path[1][2], path[2][2]};
                int[] np = {neighborPath[0][0], neighborPath[1][0], neighborPath[2][0]};
                if (p[0] != np[0] || p[1] != np[1] || p[2] != np[2]) return false;
                else if (p[0] == 0 && p[1] == 0 && p[2] == 0) numberOfEmptyAround += 1;
            }
        } else
            numberOfEmptyAround += 1;

        //verify bottom side:
        if (pos[0] < BOARD_SIZE - 1) {
            SaboteurTile neighborCard = board[pos[0] + 1][pos[1]];
            if (neighborCard == null)
                numberOfEmptyAround += 1;
            else if (objHiddenList.contains(neighborCard))
                requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                int[] p = {path[0][0], path[1][0], path[2][0]};
                int[] np = {neighborPath[0][2], neighborPath[1][2], neighborPath[2][2]};
                if (p[0] != np[0] || p[1] != np[1] || p[2] != np[2]) return false;
                else if (p[0] == 0 && p[1] == 0 && p[2] == 0) numberOfEmptyAround += 1; //we are touching by a wall
            }
        } else
            numberOfEmptyAround += 1;

        return numberOfEmptyAround != requiredEmptyAround;
    }

    public static boolean pathToHidden(SaboteurTile[][] board, int[] targetPos){
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
            originTargets2.add(new int[]{originPos * 3 + 1, originPos * 3 + 1});
            originTargets2.add(new int[]{originPos * 3 + 1, originPos * 3 + 2});
            originTargets2.add(new int[]{originPos * 3 + 1, originPos * 3    });
            originTargets2.add(new int[]{originPos * 3    , originPos * 3 + 1});
            originTargets2.add(new int[]{originPos * 3 + 2, originPos * 3 + 1});
            //get the target position in 0-1 coordinate
            int[] targetPos2 = {targetPos[0] * 3 + 1, targetPos[1] * 3 + 1};
            if (cardPath(board, intBoard, originTargets2, targetPos2, false)) {
                atLeastOnefound =true;
            }
        }

        return atLeastOnefound;
    }

    private static Boolean cardPath(SaboteurTile[][] board, int[][] intBoard, ArrayList<int[]> originTargets, int[] targetPos, Boolean usingCard) {
        // the search algorithm, usingCard indicate weither we search a path of cards (true) or a path of ones (aka tunnel)(false).
        ArrayList<int[]> queue = new ArrayList<>(); //will store the current neighboring tile. Composed of position (int[]).
        ArrayList<int[]> visited = new ArrayList<>(); //will store the visited tile with an Hash table where the key is the position the board.
        visited.add(targetPos);

        if (usingCard)
            addUnvisitedNeighborToQueue(board, intBoard, targetPos, queue, visited, BOARD_SIZE, usingCard);
        else
            addUnvisitedNeighborToQueue(board, intBoard, targetPos, queue, visited, BOARD_SIZE * 3, usingCard);

        while (queue.size() > 0) {
            int[] visitingPos = queue.remove(0);
            if (containsIntArray(originTargets, visitingPos))
                return true;

            visited.add(visitingPos);
            if (usingCard)
                addUnvisitedNeighborToQueue(board, intBoard, visitingPos, queue, visited, BOARD_SIZE, usingCard);
            else
                addUnvisitedNeighborToQueue(board, intBoard, visitingPos, queue, visited, BOARD_SIZE * 3, usingCard);
        }
        return false;
    }

    private static void addUnvisitedNeighborToQueue(SaboteurTile[][] board, int[][] intBoard, int[] pos, ArrayList<int[]> queue, ArrayList<int[]> visited, int maxSize, boolean usingCard) {
        int[][] moves = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}};
        int i = pos[0];
        int j = pos[1];
        for (int m = 0; m < 4; m++) {
            if (0 <= i + moves[m][0] && i + moves[m][0] < maxSize && 0 <= j + moves[m][1] && j + moves[m][1] < maxSize) { //if the hypothetical neighbor is still inside the board
                int[] neighborPos = new int[]{i + moves[m][0], j + moves[m][1]};
                if (!containsIntArray(visited, neighborPos)) {
                    if (usingCard && board[neighborPos[0]][neighborPos[1]] != null)
                        queue.add(neighborPos);
                    else if (!usingCard && intBoard[neighborPos[0]][neighborPos[1]] == 1)
                        queue.add(neighborPos);
                }
            }
        }
    }

    private static boolean containsIntArray(ArrayList<int[]> a, int[] o) { //the .equals used in Arraylist.contains is not working between arrays..
        if (o == null) {
            for (int[] ints : a) {
                if (ints == null)
                    return true;
            }
        } else {
            for (int[] ints : a) {
                if (Arrays.equals(o, ints))
                    return true;
            }
        }
        return false;
    }

    public static String movesToString(ArrayList<SaboteurMove> moves) {
        StringBuilder result = new StringBuilder();
        for (SaboteurMove move : moves) {
            result.append(move.getCardPlayed().getName()).append(", ").append(move.getPosPlayed()[0]).append(",").append(move.getPosPlayed()[1]).append("; ");
        }
        return result.toString();
    }
    public static String handToString(ArrayList<SaboteurCard> hand) {
        StringBuilder result = new StringBuilder();
        for (SaboteurCard card : hand) {
            result.append(card.getName()).append("; ");
        }
        return result.toString();
    }
}
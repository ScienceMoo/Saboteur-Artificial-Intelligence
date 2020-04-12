package student_player;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurTile;

import java.util.ArrayList;
import java.util.Arrays;

public class MyTools {
    public static final int BOARD_SIZE = 14;
    public static final int originPos = 5;
    public static final int[][] hiddenPos = {{originPos+7,originPos-2},{originPos+7,originPos},{originPos+7,originPos+2}};

    private static SequenceScore DepthFirstSearch(boolean[] hiddenRevealed, ArrayList<SaboteurCard> remainingCards, SaboteurTile[][] board, int id, int[] targetPos) {
        if (pathToHidden(board, targetPos)){
            return new SequenceScore(0);
        }

        if (remainingCards.size() <= 5) {
            return new SequenceScore(100);
        }

        SequenceScore bestSequenceScore = new SequenceScore(100);
        for (SaboteurCard card : remainingCards) {

            ArrayList<SaboteurMove> possibleMoves = getPossibleMoves(hiddenRevealed, board, card, id);
            for (SaboteurMove move : possibleMoves) {
                SaboteurTile[][] newBoard = addCardToBoard(board, move);

                ArrayList<SaboteurCard> newRemainingCards = new ArrayList<>(remainingCards);
                newRemainingCards.remove(card);

                SequenceScore sequenceScore = DepthFirstSearch(hiddenRevealed, newRemainingCards, newBoard, id, targetPos);
                sequenceScore.minCardsToReachEnd += 1;

                if (sequenceScore.minCardsToReachEnd < bestSequenceScore.minCardsToReachEnd) {
                    sequenceScore.moves.add(0, move);
                    bestSequenceScore = sequenceScore;
                }
            }
        }

        return bestSequenceScore;
    }

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
//        System.out.print("Adding card to the board: ");
//        System.out.println(card.getName() + "," + pos[0] + "," + pos[1] + ".");
        if (card instanceof SaboteurDestroy) {
            newBoard[pos[0]][pos[1]] = null;
        }
        else if (card instanceof SaboteurTile) {
            newBoard[pos[0]][pos[1]] = new SaboteurTile(((SaboteurTile) card).getIdx());
        }
        return newBoard;
    }

    public static SaboteurMove lookForWinningMove(ArrayList<SaboteurCard> myHand, ArrayList<SaboteurMove> moves, SaboteurTile[][] board, ArrayList<int[]> targets) {
        ArrayList<SaboteurCard> remainingCards = new ArrayList<>();
        for (int c = 0; c < myHand.size(); c++) {
            remainingCards.add(myHand.get(c));
        }

        int bestScore = 10;

        SaboteurMove bestMove = moves.get(0);

        System.out.println("lookForWinningMove Function");
        for (int t = 0; t < targets.size(); t++) {
            System.out.println("target x: " + targets.get(t)[0]);
            System.out.println("target y: " + targets.get(t)[1]);
        }

        for (int i = 0; i < moves.size(); i++) {
            SaboteurCard firstCard = moves.get(i).getCardPlayed();
            int firstMoveX = moves.get(i).getPosPlayed()[0];
            int firstMoveY = moves.get(i).getPosPlayed()[1];
            System.out.println("card Played: " + firstCard.getName());
            System.out.println("position x Played: " + moves.get(i).getPosPlayed()[0]);
            System.out.println("position y Played: " + moves.get(i).getPosPlayed()[1]);

            for (int c = 0; c < remainingCards.size(); c++) {
                SaboteurCard card = remainingCards.get(c);
                System.out.println("other card: " + card.getName());
                if (firstCard.getName().startsWith(card.getName())){
                    remainingCards.remove(card);
                }
            }

            SaboteurTile[][] newBoard = addCardToBoard(board, moves.get(i));
            for (int j = 0; j < targets.size(); j++) {
                if (pathToHidden(newBoard, targets.get(j))) {
                    System.out.println("Win in 1 move!");
                    return moves.get(i);
                }
            }
            for (int x = 0; ((x < moves.size()) && (x != i)); x++) {
                SaboteurCard secondCard = moves.get(x).getCardPlayed();
                int secondMoveX = moves.get(x).getPosPlayed()[0];
                int secondMoveY = moves.get(x).getPosPlayed()[1];
                boolean cardStillAvailable = false;

                for (int c = 0; c < remainingCards.size(); c++) {
                    SaboteurCard card = remainingCards.get(c);
                    if (secondCard.getName().startsWith(card.getName())){
                        cardStillAvailable = true;
                        remainingCards.remove(card);
                    }
                }

                if (!cardStillAvailable) {
                    continue;
                }
                else if ((secondMoveX == firstMoveX) && (secondMoveY == firstMoveY)) {
                    continue;
                }
                SaboteurTile[][] secondNewBoard = addCardToBoard(newBoard, moves.get(x));
                for (int j = 0; j < targets.size(); j++) {
                    if ((bestScore > 2) && pathToHidden(secondNewBoard, targets.get(j))) {
                        bestScore = 2;
                        bestMove = moves.get(i);
                        System.out.println("Win in 2 moves!");
                    }
                }
                for (int w = 0; ((w < moves.size()) && (w != x)); w++) {
                    SaboteurCard thirdCard = moves.get(w).getCardPlayed();
                    int thirdMoveX = moves.get(w).getPosPlayed()[0];
                    int thirdMoveY = moves.get(w).getPosPlayed()[1];
                    cardStillAvailable = false;

                    for (int c = 0; c < remainingCards.size(); c++) {
                        SaboteurCard card = remainingCards.get(c);
                        if (thirdCard.getName().startsWith(card.getName())){
                            cardStillAvailable = true;
                            remainingCards.remove(card);
                        }
                    }

                    if (!cardStillAvailable) {
                        continue;
                    }

                    else if ((thirdMoveX == firstMoveX) && (thirdMoveY == firstMoveY)) {
                        continue;
                    }
                    else if ((thirdMoveX == secondMoveX) && (thirdMoveY == secondMoveY)) {
                        continue;
                    }
                    SaboteurTile[][] thirdNewBoard = addCardToBoard(newBoard, moves.get(w));
                    for (int j = 0; j < targets.size(); j++) {
                        if ((bestScore > 3) && pathToHidden(thirdNewBoard, targets.get(j))) {
                            bestScore = 3;
                            bestMove = moves.get(i);
                            System.out.println("Win in 3 moves!");
                        }
                    }
                }
            }
        }
        if (bestScore < 10) {
            return bestMove;
        }
        return null;
    }

    public static ArrayList<SaboteurMove> lookForWinningSequence(boolean[] hiddenRevealed, SaboteurBoardState boardState, ArrayList<SaboteurCard> myHand, int[] targetPos) {
        SaboteurTile[][] board = boardState.getHiddenBoard();
        int id = boardState.getTurnPlayer();

        SequenceScore score = DepthFirstSearch(hiddenRevealed, myHand, board, id, targetPos);

        if (score.minCardsToReachEnd < 10 && !score.moves.isEmpty()) {
            return score.moves;
        }
        else {
            return null;
        }

    }

    public static ArrayList<SaboteurMove> getPossibleMoves(boolean[] hiddenRevealed, SaboteurTile[][] board, SaboteurCard card, int id) {
        ArrayList<SaboteurMove> legalMoves = new ArrayList<>();

        if (card instanceof SaboteurTile) {
            ArrayList<int[]> allowedPositions = positionsForTile(hiddenRevealed, board, (SaboteurTile) card);
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

        /*ArrayList<Coord> queue = new ArrayList<>();
        Coord origin = new Coord(targetPos[0], targetPos[1]);
        queue.add(origin);

        while (queue.size() > 0) {

        }*/


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
                System.out.println("0-1 path was not found");
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
        String result = "";
        for (int i = 0; i < hand.size(); i++) {
            SaboteurCard card = hand.get(i);
            result += card.getName() + "; ";
        }
        return result;
    }
}
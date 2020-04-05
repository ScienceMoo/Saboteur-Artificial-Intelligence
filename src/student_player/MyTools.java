package student_player;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurBonus;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurTile;

import java.util.ArrayList;

public class MyTools {
    public static SaboteurCard lookForWinningSequence(SaboteurBoardState boardState, ArrayList<SaboteurCard> myHand) {
        int minimumNumberOfMoves = 8;


        int[] currentSequence = new int[7];
        int[] bestSequence = new int[7];
        bestSequence[0] = 0;
        int currentNumberOfMoves = 0;

        A: for (int i = 0; i < 7; i++) {
            int[][] board = boardState.getHiddenIntBoard(); //intialize the board with no moves added to it
            currentSequence[0] = i;
            board = addMove(board, myHand.get(i));
            boolean isWinningMove = checkForPath(board);
            if (isWinningMove && minimumNumberOfMoves > 1) {
                minimumNumberOfMoves = 1;
                for (int index = 0; index < 7; index++) {
                    bestSequence[index] = currentSequence[index];
                }
                break A;
            }

            B: for (int j = 0; (j < 7 && j != i); j++) {
                currentSequence[1] = j;
                board = addMove(board, myHand.get(j));
                isWinningMove = checkForPath(board);
                if (isWinningMove && minimumNumberOfMoves > 2) {
                    minimumNumberOfMoves = 2;
                    for (int index = 0; index < 7; index++) {
                        bestSequence[index] = currentSequence[index];
                    }
                    break B;
                }

                C: for (int k = 0; (j < 7 && k != j) ; k++) {
                    currentSequence[2] = k;
                    board = addMove(board, myHand.get(k));
                    isWinningMove = checkForPath(board);
                    if (isWinningMove && minimumNumberOfMoves > 3) {
                        minimumNumberOfMoves = 3;
                        for (int index = 0; index < 7; index++) {
                            bestSequence[index] = currentSequence[index];
                        }
                        break C;
                    }

                    D: for (int x = 0; (x < 7 && x != k) ; x++) {
                        currentSequence[3] = x;
                        board = addMove(board, myHand.get(x));
                        isWinningMove = checkForPath(board);
                        if (isWinningMove && minimumNumberOfMoves > 4) {
                            minimumNumberOfMoves = 4;
                            for (int index = 0; index < 7; index++) {
                                bestSequence[index] = currentSequence[index];
                            }
                            break D;
                        }


                        E: for (int w = 0; (w < 7 && w != x) ; w++) {
                            currentSequence[4] = w;
                            board = addMove(board, myHand.get(w));
                            isWinningMove = checkForPath(board);
                            if (isWinningMove && minimumNumberOfMoves > 5) {
                                minimumNumberOfMoves = 5;
                                for (int index = 0; index < 7; index++) {
                                    bestSequence[index] = currentSequence[index];
                                }
                                break E;
                            }

                            F: for (int h = 0; (h < 7 && h != w) ; h++) {
                                currentSequence[5] = h;
                                board = addMove(board, myHand.get(h));
                                isWinningMove = checkForPath(board);
                                if (isWinningMove && minimumNumberOfMoves > 6) {
                                    minimumNumberOfMoves = 6;
                                    for (int index = 0; index < 7; index++) {
                                        bestSequence[index] = currentSequence[index];
                                    }
                                    break F;
                                }

                                G: for (int g = 0; (g < 7 && g != h) ; g++) {
                                    currentSequence[5] = g;
                                    board = addMove(board, myHand.get(g));
                                    isWinningMove = checkForPath(board);
                                    if (isWinningMove && minimumNumberOfMoves > 7) {
                                        minimumNumberOfMoves = 7;
                                        for (int index = 0; index < 7; index++) {
                                            bestSequence[index] = currentSequence[index];
                                        }
                                        break F;
                                    }


                                }
                            }
                        }
                    }
                }
            }
        }
        if (minimumNumberOfMoves == 8) {
            return null;
        }
        else {
            return myHand.get(bestSequence[0]);
        }
    }

    public static boolean checkIfEnemyCanWin(SaboteurBoardState boardState) {
        boolean result = false;

        for (int i = 0; i < 50; i++) {
            ArrayList<SaboteurCard> randomHand = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                int randomNum = (int)(Math.random()*((4-1)+1))+1;
                System.out.println("randomNum: " + randomNum);
                int randomTile = (int)(Math.random()*((10-0)+0))+0;
                System.out.println("otherRandomNum: " + randomTile);
                int randomFlip = (int)(Math.random()*((2-1)+1))+1;
                System.out.println("randomFlip: " + randomFlip);


                if (randomNum < 4) {
                    if (randomTile == 0 || randomTile == 1 || randomTile == 10) {
                        System.out.println("creating non-flippable tile");
                        SaboteurCard randomCard = new SaboteurTile("" + randomTile);
                        randomHand.add(randomCard);
                    }
                    else {
                        System.out.println("creating flippable tile");
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

            System.out.println(randomHand);
            SaboteurCard card = lookForWinningSequence(boardState, randomHand);
            if (card != null) {
                result = true;
            }
        }
        System.out.println(result);
        return result;
    }

    public static boolean checkForPath(int[][] intBoard) {
        boolean result = false;

        int x_index = 5;
        int y_index = 5;

        boolean[][] explorationMap = new boolean[14][14];
        for (int x = 0; x < 14; x++) {
            for (int y = 0; y < 14; y++) {
                explorationMap[x][y] = false;
            }
        }
        explorationMap[5][5] = false;

        int numWalks = 0;

        while ( numWalks < 100) {
            A: for (int i = 0; i < 2; i++) {
                B: for (int j = 0; j < 2; j++) {
                    if (i == 0) {
                        if (j == 0) {
                            if (!explorationMap[x_index][y_index]) {
                                x_index--;
                                y_index--;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static boolean lookForPath(int[][] intBoard, int startingX, int startingY ) {
        //recursive function or something...
        return false;
    }

    public static int[][] addMove(int[][] board, SaboteurCard card) {
        return board;
    }
}
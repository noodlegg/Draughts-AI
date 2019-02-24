package nl.tue.s2id90.group105;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import java.util.Collections;
import java.util.List;
import nl.tue.s2id90.draughts.DraughtsState;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import org10x10.dam.game.Move;

/**
 * Implementation of the DraughtsPlayer interface.
 *
 * @author Daniël Barenholz
 * @author Keon
 */
public class Player55 extends DraughtsPlayer {

    // bestValue denotes the value of the best move
    private int bestValue = 0;
    // maxSearchDepth denotes the maximum recursion depth to search on
    int maxSearchDepth;

    /**
     * boolean that indicates that the GUI asked the player to stop thinking.
     */
    private boolean stopped;

    // A constructor that sets the maxSearchDepth, and adds a picture.
    public Player55(int maxSearchDepth) {
        super("reimu.jpg");
        this.maxSearchDepth = maxSearchDepth;
    }

    /**
     * getMove will try to return the best possible move for a given state,
     * using iterative deepening and alpha-beta pruning.
     *
     * @param originalState denotes the state in which one gets a move.
     * @return bestMove
     */
    @Override
    public Move getMove(DraughtsState originalState) {
        // Clone the original state for the exception
        DraughtsState clonedState = originalState.clone();
        // set bestMove to null
        Move bestMove = null;
        // set bestValue to 0
        bestValue = 0;
        // create a new node
        DraughtsNode node = new DraughtsNode(originalState);
        try {
            // We keep searching as long as the time limit is not reached.
            while (!stopped) {
                // compute bestMove and bestValue in a call to alphabeta (initial call)
                bestValue = alphaBeta(node, MIN_VALUE, MAX_VALUE, maxSearchDepth, maxSearchDepth);
                // store the bestMove found uptill now (if no exception)
                bestMove = node.getBestMove();

                // print the results for debugging reasons
                System.err.format(
                        "%s: depth= %2d, best move = %5s, value=%d\n",
                        this.getClass().getSimpleName(), maxSearchDepth, bestMove, bestValue
                );
            }
        } catch (AIStoppedException ex) {
            System.out.print("Exception! Too much time used by player: " + (clonedState.isWhiteToMove() ? " who plays white." : " who plays black."));
        }

        // We should do a move, it cannot be null
        if (bestMove == null) {
            System.out.println("We could not find a move. Proceeding with a random move.");
            return getRandomMove(clonedState);
        } else {
            // In case everything goes well, we return the best move here
            return bestMove;
        }
    }

    /**
     * This method's return value is displayed in the AICompetition GUI.
     *
     * @return the value for the draughts state s as it is computed in a call to
     * getMove(s).
     */
    @Override
    public Integer getValue() {
        return bestValue;
    }

    /**
     * Tries to make alphabeta search stop. Search should be implemented such
     * that it throws an AIStoppedException when boolean stopped is set to true;
     *
     */
    @Override
    public void stop() {
        stopped = true;
    }

    /**
     * Finds a random (valid) move for a given state. The state should be the
     * same state as the parameter of getMove.
     *
     * @param s - The same draughtsstate as was the parameter of getMove
     */
    Move getRandomMove(DraughtsState s) {
        // Needs to be set to false here, otherwise after the first random move it would continue to do random moves forever.
        stopped = false;
        // A list consisting of (valid) moves
        List<Move> moves = s.getMoves();
        // Shuffle the list for a random move
        Collections.shuffle(moves);
        // if there are no moves, return null; else, return first move
        if (moves.isEmpty()) {
            return null;
        } else {
            /**
             * in case of a random move, we should set the bestValue (shown in
             * GUI) to the evaluation of that move. So we should first do said
             * move, then evaluate it and return it
             */

            // do the move we return
            s.doMove(moves.get(0));
            System.out.println("We will do this move: \n" + s.toString());
            // evaluate the state after we did that move
            bestValue = evaluate(s);
            System.out.println("The evaluation of that move is: " + evaluate(s));
            // return the move we calculated the value for.
            return moves.get(0);
        }
    }

    /**
     * Implementation of alphabeta that automatically chooses the white player
     * as maximizing player and the black player as minimizing player.
     *
     * @param node contains DraughtsState and has field to which the best move
     * can be assigned.
     * @param alpha is the alpha value, initially MIN.VALUE
     * @param beta is the beta value, initially MAX.VALUE
     * @param currentDepth the current depth in the recursion
     * @param maxDepth is the maximum recursion depth
     *
     * @return the compute value of this node
     * @throws AIStoppedException thrown whenever the boolean stopped has been
     * set to true.
     */
    int alphaBeta(DraughtsNode node, int alpha, int beta, int currentDepth, int maxDepth)
            throws AIStoppedException {
        if (node.getState().isWhiteToMove()) {
            // We're the white player, thus maximizing.
            return alphaBetaMax(node, alpha, beta, currentDepth, maxDepth);
        } else {
            // We're the black player, thus minimizing.
            return alphaBetaMin(node, alpha, beta, currentDepth, maxDepth);
        }
    }

    /**
     * Does an alphabeta computation with the given alpha and beta where the
     * player that is to move in node is the minimizing player.
     *
     * @param node contains DraughtsState and has field to which the best move
     * can be assigned.
     * @param alpha is the alpha value
     * @param beta is the beta value
     * @param currentDepth the current depth in the recursion
     * @param maxDepth is the maximum recursion depth
     *
     * @return the compute value of this node
     * @throws AIStoppedException thrown whenever the boolean stopped has been
     * set to true.
     */
    int alphaBetaMin(DraughtsNode node, int alpha, int beta, int currentDepth, int maxDepth)
            throws AIStoppedException {
        // Check if we need to stop
        if (stopped) {
            stopped = false;
            throw new AIStoppedException();
        }

        // get current state
        DraughtsState state = node.getState();
        // set bestMove to null
        Move bestMove = null;
        // We need to evaluate all endStates and when currentDepth is 0
        if (currentDepth == 0 || state.isEndState()) {
            return evaluate(state);
        } else {
            // create a list containing all possible moves
            List<Move> moves = state.getMoves();
            // value := eventually the best evalution of any state
            int currentValue = Integer.MAX_VALUE;
            // for all possible moves do:
            for (Move move : moves) {
                // go down one depth in the tree (do a move to reach a new state)
                state.doMove(move);
                // Get the nextValue by a recursive call
                int nextValue = alphaBetaMax(node, alpha, beta, currentDepth - 1, maxDepth);
                // check if the updated value is smaller than it was previously
                if (nextValue < currentValue) {
                    // if so set the best move and update the value.
                    bestMove = move;
                    currentValue = nextValue;
                }
                // set beta to min(beta,v)
                beta = Math.min(beta, currentValue);
                // go back up in the tree after calculations
                state.undoMove(move);
                // Alpha cutoff!
                if (beta <= alpha) {
                    break;
                }
            }
            // sets the best move if the AI is playing as black
            if (currentDepth == maxDepth) {
                node.setBestMove(bestMove);
            }
            // return currentValue, which is the best one
            return currentValue;
        }
    }

    /**
     * Does an alphabeta computation with the given alpha and beta where the
     * player that is to move in node is the maximizing player.
     *
     * @param node contains DraughtsState and has field to which the best move
     * can be assigned.
     * @param alpha is the alpha value
     * @param beta is the beta value
     * @param currentDepth the current depth in the recursion
     * @param maxDepth is the maximum recursion depth
     *
     * @return the compute value of this node
     * @throws AIStoppedException thrown whenever the boolean stopped has been
     * set to true.
     */
    int alphaBetaMax(DraughtsNode node, int alpha, int beta, int currentDepth, int maxDepth)
            throws AIStoppedException {
        // Check if we need to stop
        if (stopped) {
            stopped = false;
            throw new AIStoppedException();
        }

        // get current state
        DraughtsState state = node.getState();
        // set bestMove to null
        Move bestMove = null;
        // We need to evaluate all endStates and when currentDepth is 0
        if (currentDepth == 0 || state.isEndState()) {
            return evaluate(state);
        } else {
            // create a list containing all possible moves
            List<Move> moves = state.getMoves();
            // value := eventually the best evalution of any state
            int currentValue = Integer.MIN_VALUE;
            // for all possible moves do:
            for (Move move : moves) {
                // go down one depth in the tree (do a move to reach a new state)
                state.doMove(move);
                // Get the nextValue by a recursive call
                int nextValue = alphaBetaMin(node, alpha, beta, currentDepth - 1, maxDepth);
                // check if the updated value is larger than it was previously
                if (nextValue > currentValue) {
                    // if so set the best move and update the value.
                    bestMove = move;
                    currentValue = nextValue;
                }
                // set alpha to max(alpha,v)
                alpha = Math.max(alpha, currentValue);
                // go back up in the tree after calculations
                state.undoMove(move);
                // Beta cutoff!
                if (beta <= alpha) {
                    break;
                }
            }
            // sets the best move if the AI is playing as white
            if (currentDepth == maxDepth) {
                node.setBestMove(bestMove);
            }
            // return currentValue, which is the best one
            return currentValue;
        }
    }
    /**
     * The score if you win as white
     */
    final static private int SCORE_WHITE_WIN = Integer.MAX_VALUE - 1;
    /**
     * The score if you win as black
     */
    final static private int SCORE_BLACK_WIN = Integer.MIN_VALUE + 1;

    /**
     * A function that evaluates the current draughtsstate.
     *
     * @param s the state to be evaluated
     * @return the evaluation of a state
     */
    public int evaluate(DraughtsState s) {
//        System.err.println("Evaluating state: " + s.toString()) ;
        // Check if someone won
        if (s.isEndState()) {
            if (s.isWhiteToMove()) {
                return SCORE_BLACK_WIN;
            } else {
                return SCORE_WHITE_WIN;
            }
        }
        // An integer to hold the result of the evaluation
        int evaluationResult = 0;
        /**
         * A piece counter that counts all pieces with a value of 1. Kings are
         * counted as a value of 3. Multiplied by 10 because this is the most
         * important factor. Note: pieceCounting does not influence the begin
         * state.
         */
        evaluationResult += pieceCounting(s) * 10;
//        System.out.println("Evaluation after counting: " + evaluationResult);
        /**
         * A function that calculates the tempi of the state according to the
         * site linked to on canvas. Note: tempi does not influence the begin
         * state.
         */
        evaluationResult += tempiCount(s);
//        System.out.println("Evaluation after tempi: " + evaluationResult);
        /**
         * A function that returns the number of moves that are capture moves.
         * The more capture moves you have, the better, since you can capture
         * more pieces. Note: does not influence the begin state.
         */
        evaluationResult += pieceAttemptAttacking(s);
//        System.out.println("Evaluation after capture moves: " + evaluationResult);
        /**
         * A function that returns a value based on where your pieces are on the
         * board. In the middle is better than at the edges.
         */
        evaluationResult += pieceInGoodPosition(s);
//        System.out.println("Evaluation after positional eval: " + evaluationResult);
        // return the evaluation
        return evaluationResult;
    }

    /**
     * A function that calculates the tempi of the state. Tempi is calculated as
     * follows: You count the number of pieces per row, and multiply that by the
     * rownumber as looked at from the currentplayer.
     *
     * @return white tempi value minus black tempi value
     */
    private int tempiCount(DraughtsState state) {
        // the tempi value of black
        int blackVal = 0;
        // the tempi value of white
        int whiteVal = 0;
        // get the array of pieces
        int[] pieces = state.getPieces();
        // loop through the board
        for (int i = 0; i < pieces.length; i++) {
            // top row
            if (i < 6) {
                // if black pieces
                if (pieces[i] == 2 | pieces[i] == 4) {
                    blackVal = blackVal + 1;
                }
                // white pieces
                if (pieces[i] == 1 | pieces[i] == 3) {
                    whiteVal = whiteVal + 10;
                }
            }
            // second top row
            if (6 <= i && i < 11) {
                // if black pieces
                if (pieces[i] == 2 | pieces[i] == 4) {
                    blackVal = blackVal + 2;
                }
                // white pieces
                if (pieces[i] == 1 | pieces[i] == 3) {
                    whiteVal = whiteVal + 9;
                }
            }
            // third top row
            if (11 <= i && i < 16) {
                // if black pieces
                if (pieces[i] == 2 | pieces[i] == 4) {
                    blackVal = blackVal + 3;
                }
                // white pieces
                if (pieces[i] == 1 | pieces[i] == 3) {
                    whiteVal = whiteVal + 8;
                }
            }
            // fourth top row
            if (16 <= i && i < 21) {
                // if black pieces
                if (pieces[i] == 2 | pieces[i] == 4) {
                    blackVal = blackVal + 4;
                }
                // white pieces
                if (pieces[i] == 1 | pieces[i] == 3) {
                    whiteVal = whiteVal + 7;
                }
            }
            // fifth top row
            if (21 <= i && i < 26) {
                // if black pieces
                if (pieces[i] == 2 | pieces[i] == 4) {
                    blackVal = blackVal + 5;
                }
                // white pieces
                if (pieces[i] == 1 | pieces[i] == 3) {
                    whiteVal = whiteVal + 6;
                }
            }
            // sixth top row
            if (26 <= i && i < 31) {
                // if black pieces
                if (pieces[i] == 2 | pieces[i] == 4) {
                    blackVal = blackVal + 6;
                }
                // white pieces
                if (pieces[i] == 1 | pieces[i] == 3) {
                    whiteVal = whiteVal + 5;
                }
            }
            // seventh top row
            if (31 <= i && i < 36) {
                // if black pieces
                if (pieces[i] == 2 | pieces[i] == 4) {
                    blackVal = blackVal + 7;
                }
                // white pieces
                if (pieces[i] == 1 | pieces[i] == 3) {
                    whiteVal = whiteVal + 4;
                }
            }
            // eight top row
            if (26 <= i && i < 41) {
                // if black pieces
                if (pieces[i] == 2 | pieces[i] == 4) {
                    blackVal = blackVal + 8;
                }
                // white pieces
                if (pieces[i] == 1 | pieces[i] == 3) {
                    whiteVal = whiteVal + 3;
                }
            }
            // ninth top row
            if (41 <= i && i < 46) {
                // if black pieces
                if (pieces[i] == 2 | pieces[i] == 4) {
                    blackVal = blackVal + 9;
                }
                // white pieces
                if (pieces[i] == 1 | pieces[i] == 3) {
                    whiteVal = whiteVal + 2;
                }
            }
            if (46 <= i && i < pieces.length) {
                // if black pieces
                if (pieces[i] == 2 | pieces[i] == 4) {
                    blackVal = blackVal + 10;
                }
                // white pieces
                if (pieces[i] == 1 | pieces[i] == 3) {
                    whiteVal = whiteVal + 1;
                }
            }
        }
        // set the result to the white minus the black one
        return whiteVal - blackVal;
    }

    /**
     * @return The number of Pieces (white - black), kings are counted as 3
     * pieces each
     */
    private int pieceCounting(DraughtsState state) {
        // get the array of pieces
        int[] pieces = state.getPieces();
        // count the number of white and black pieces.
        int white = 0;
        int black = 0;
        int whiteKing = 0;
        int blackKing = 0;

        for (int i = 0; i < pieces.length; i++) {
            if (pieces[i] == 1) {
                white++;
            }
            if (pieces[i] == 2) {
                black++;
            }
            if (pieces[i] == 3) {
                whiteKing++;
            }
            if (pieces[i] == 4) {
                blackKing++;
            }
        }
        // return the white pieces minus the black pieces
        return (white + 3 * whiteKing) - (black + 3 * blackKing);
    }

    /**
     * @return The number of moves that are capture moves. The more the better.
     */
    private int pieceAttemptAttacking(DraughtsState state) {
        // an integer to hold the value
        int numOfCaptureMoves = 0;
        // create a list with all moves
        List<Move> moves = state.getMoves();
        // loop through all moves
        for (Move move : moves) {
            // if a move is a capture move
            if (move.isCapture()) {
                // add one to the number of capture moves
                numOfCaptureMoves++;
            }
        }
        //return the number of capture moves
        return numOfCaptureMoves;
    }

    /**
     * @return A value based on where the pieces are in the state. A higher
     * value means that pieces are in the middle.
     */
    private int pieceInGoodPosition(DraughtsState state) {
        // an integer to hold the value
        int result = 0;
        // get the array of pieces
        int[] pieces = state.getPieces();
        // loop through all the pieces
        for (int i = 0; i < pieces.length; i++) {
            // if the piece is in the middle
            if ("middle".equals(getPosition(i))) {
                // if it's a white piece
                if (pieces[i] == 1 || pieces[i] == 3) {
                    // Add one to the result
                    result++;
                } else {
                    // if it's black, subtract one
                    result--;
                }
            }
        }
        // return the result
        return result;
    }

    /**
     * @param i the position on the board
     * @return evaluate the position
     */
    private String getPosition(int i) {
        // Some checks to see where the piece is. If it's not on the edges, return "middle"
        if (i < 6) {
            return "top";
        } else if (i > 45) {
            return "bottom";
        } else if (i % 10 == 5) {
            return "right";
        } else if (i % 10 == 6) {
            return "left";
        } else {
            return "middle";
        }
    }

}

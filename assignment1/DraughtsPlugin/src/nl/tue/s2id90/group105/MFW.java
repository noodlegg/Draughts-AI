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
 * @author huub
 */
// ToDo: rename this class (and hence this file) to have a distinct name
//       for your player during the tournament
public class MFW extends DraughtsPlayer {

    private int bestValue = 0;
    int maxSearchDepth;

    /**
     * boolean that indicates that the GUI asked the player to stop thinking.
     */
    private boolean stopped;

    public MFW(int maxSearchDepth) {
        super("brightsmile.jpg"); // ToDo: replace with your own icon
        this.maxSearchDepth = maxSearchDepth;
    }

    @Override
    public Move getMove(DraughtsState s) {
        Move bestMove = null;
        bestValue = 0;
        DraughtsNode node = new DraughtsNode(s);    // the root of the search tree
        try {
            // compute bestMove and bestValue in a call to alphabeta
            bestValue = alphaBeta(node, MIN_VALUE, MAX_VALUE, maxSearchDepth);

            // store the bestMove found uptill now
            // NB this is not done in case of an AIStoppedException in alphaBeat()
            bestMove = node.getBestMove();

            // print the results for debugging reasons
            System.err.format(
                    "%s: depth= %2d, best move = %5s, value=%d\n",
                    this.getClass().getSimpleName(), maxSearchDepth, bestMove, bestValue
            );
        } catch (AIStoppedException ex) {  /* nothing to do */        }

        if (bestMove == null) {
            System.err.println("no valid move found!");
            return getRandomValidMove(s);
        } else {
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
     * returns random valid move in state s, or null if no moves exist.
     */
    Move getRandomValidMove(DraughtsState s) {
        List<Move> moves = s.getMoves();
        Collections.shuffle(moves);
        return moves.isEmpty() ? null : moves.get(0);
    }

    /**
     * Implementation of alphabeta that automatically chooses the white player
     * as maximizing player and the black player as minimizing player.
     *
     * @param node contains DraughtsState and has field to which the best move
     * can be assigned.
     * @param alpha
     * @param beta
     * @param depth maximum recursion Depth
     * @return the computed value of this node
     * @throws AIStoppedException
     *
     */
    int alphaBeta(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException {
        if (node.getState().isWhiteToMove()) {
            return alphaBetaMax(node, alpha, beta, depth);
        } else {
            return alphaBetaMin(node, alpha, beta, depth);
        }
    }

    /**
     * Does an alphabeta computation with the given alpha and beta where the
     * player that is to move in node is the minimizing player.
     *
     * <p>
     * Typical pieces of code used in this method are:
     * <ul> <li><code>DraughtsState state = node.getState()</code>.</li>
     * <li><code> state.doMove(move); .... ; state.undoMove(move);</code></li>
     * <li><code>node.setBestMove(bestMove);</code></li>
     * <li><code>if(stopped) { stopped=false; throw new AIStoppedException(); }</code></li>
     * </ul>
     * </p>
     *
     * @param node contains DraughtsState and has field to which the best move
     * can be assigned.
     * @param alpha
     * @param beta
     * @param depth maximum recursion Depth
     * @return the compute value of this node
     * @throws AIStoppedException thrown whenever the boolean stopped has been
     * set to true.
     */
    int alphaBetaMin(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException {
        if (stopped) {
            stopped = false;
            throw new AIStoppedException();
        }
        DraughtsState state = node.getState();
        DraughtsNode cloneNode = node;
        // ToDo: write an alphabeta search to compute bestMove and value
        Move bestMove = null;
        // Evaluate all endStates or when depth is 0
        if (state.isEndState() || depth == 0) {
            return evaluate(state);
        } else {
            // List all possible moves
            List<Move> moves = state.getMoves();
            int currentValue = Integer.MAX_VALUE;

            for (Move move : moves) {
                // Do a move to reach next state
                state.doMove(move);
                DraughtsNode newNode = new DraughtsNode(state);
                // Get nextValue with recursive call
                int nextValue = alphaBetaMax(newNode, alpha, beta, depth - 1);
                // If next value is smaller than the current
                if (nextValue < currentValue) {
                    bestMove = move;
                    currentValue = nextValue;
                }
                beta = Math.min(beta, currentValue);
                state.undoMove(move);
                // Alpha cutoff
                if (beta <= alpha) {
                    break;
                }
            }
            node.setBestMove(bestMove);
            return currentValue;
        }
    }

    int alphaBetaMax(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException {
        if (stopped) {
            stopped = false;
            throw new AIStoppedException();
        }
        DraughtsState state = node.getState();
        // ToDo: write an alphabeta search to compute bestMove and value
        Move bestMove = null;
        // Evaluate all endStates or when currentDepth is 0
        if (state.isEndState() || depth == 0) {
            return evaluate(state);
        } else {
            // List all possible moves
            List<Move> moves = state.getMoves();
            int currentValue = Integer.MIN_VALUE;

            for (Move move : moves) {
                // Do a move to reach next state
                state.doMove(move);
                DraughtsNode newNode = new DraughtsNode(state);

                // Get nextValue with recursive call
                int nextValue = alphaBetaMin(newNode, alpha, beta, depth - 1);
                // If next value is larger than the current
                if (nextValue > currentValue) {
                    bestMove = move;
                    currentValue = nextValue;
                }
                alpha = Math.max(alpha, currentValue);
                state.undoMove(move);
                // Beta cutoff
                if (beta <= alpha) {
                    break;
                }
            }
            node.setBestMove(bestMove);
            return currentValue;
        }
    }

    /**
     * A method that evaluates the given state.
     */
    // ToDo: write an appropriate evaluation function
    int evaluate(DraughtsState state) {
        // Check if someone won
        if (state.isEndState()) {
            if (state.isWhiteToMove()) {
                // If black won
                return Integer.MIN_VALUE + 1;
            } else { // If white won
                return Integer.MAX_VALUE - 1;
            }
        }

        // Calculate all pieces on the board
        int whiteScore = 0;
        int blackScore = 0;
        int[] pieces = state.getPieces();
        for (int i = 0; i < pieces.length; i++) {
            int piece = pieces[i];
            // See on which row the piece is, close to other end is preferable
            switch (piece) {
                case DraughtsState.WHITEKING:
                    whiteScore += 3;
                    break;
                case DraughtsState.WHITEPIECE:
                    whiteScore += 1;
                    break;
                case DraughtsState.BLACKKING:
                    blackScore += 3;
                    break;
                case DraughtsState.BLACKPIECE:
                    blackScore += 1;
                    break;
            }
        }
        return whiteScore - blackScore;
    }
}

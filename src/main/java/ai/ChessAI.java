package ai;

import engine.Board;
import model.Move;
import model.PieceColor;

import java.util.Optional;

public interface ChessAI {
    Optional<Move> chooseMove(Board board, PieceColor color);
}

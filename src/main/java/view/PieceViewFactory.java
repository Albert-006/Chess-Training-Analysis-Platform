package view;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import model.Bishop;
import model.King;
import model.Knight;
import model.Pawn;
import model.Piece;
import model.PieceColor;
import model.Queen;
import model.Rook;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and caches high-definition transparent piece graphics extracted directly
 * from the reference Staunton piece set.
 */
public final class PieceViewFactory {

    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();

    private PieceViewFactory() {}

    public static Node createPieceNode(Piece piece, double size) {
        if (piece == null) {
            return new Pane();
        }

        String assetName = getAssetFileName(piece);
        Image image = getImage(assetName);

        if (image != null && !image.isError()) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(size * 0.85);
            imageView.setFitHeight(size * 0.85);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            StackPane container = new StackPane(imageView);
            container.setPrefSize(size, size);
            container.setMaxSize(size, size);
            container.setMinSize(size, size);
            return container;
        }

        // Fallback to unicode if image resource somehow fails
        javafx.scene.control.Label fallback = new javafx.scene.control.Label(piece.getDisplaySymbol());
        fallback.setStyle("-fx-font-size: " + (size * 0.6) + "px;");
        return fallback;
    }

    private static Image getImage(String fileName) {
        if (IMAGE_CACHE.containsKey(fileName)) {
            return IMAGE_CACHE.get(fileName);
        }
        String path = "/pieces/" + fileName;
        InputStream is = PieceViewFactory.class.getResourceAsStream(path);
        if (is != null) {
            Image img = new Image(is);
            IMAGE_CACHE.put(fileName, img);
            return img;
        }
        return null;
    }

    private static String getAssetFileName(Piece piece) {
        String color = piece.getColor() == PieceColor.WHITE ? "white" : "black";
        String type;
        if (piece instanceof Pawn) {
            type = "pawn";
        } else if (piece instanceof Knight) {
            type = "knight";
        } else if (piece instanceof Bishop) {
            type = "bishop";
        } else if (piece instanceof Rook) {
            type = "rook";
        } else if (piece instanceof Queen) {
            type = "queen";
        } else if (piece instanceof King) {
            type = "king";
        } else {
            type = "pawn";
        }
        return color + "_" + type + ".png";
    }
}

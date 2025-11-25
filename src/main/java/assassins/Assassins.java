package assassins;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class Assassins extends Application {

    private Game game;
    private TextArea outputArea;
    private Button killButton;
    private Button storeButton;

    @Override
    public void start(Stage primaryStage) {
        TextField gameNameField = new TextField("game-name");

        TextArea namesArea = new TextArea();
        namesArea.setPromptText("Enter one player name per line");

        TextField killField = new TextField();
        killField.setPromptText("Name to kill");

        TextField roundField = new TextField();
        roundField.setPromptText("Round number to load");

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(10);

        Button startButton = new Button("Start New Game");
        killButton = new Button("Kill");
        storeButton = new Button("Store Round");
        Button loadButton = new Button("Load Round");

        killButton.setDisable(true);
        storeButton.setDisable(true);

        startButton.setOnAction(e -> {
            String gameName = gameNameField.getText().trim();
            if (gameName.isEmpty()) {
                appendOutput("Please enter a game name.");
                return;
            }

            List<String> names = namesArea.getText().lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (names.isEmpty()) {
                appendOutput("Please enter at least one player name.");
                return;
            }

            // Start game by using written text file of names
            String dirPath = "src/main/resources/" + gameName;
            String filePath = dirPath + "/gameStart.txt";

            try {
                Files.createDirectories(Paths.get(dirPath));
                String content = String.join(System.lineSeparator(), names) + System.lineSeparator();
                Files.writeString(
                        Paths.get(filePath),
                        content,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            } catch (IOException ex) {
                appendOutput("Error writing start file: " + ex.getMessage());
                return;
            }

            game = new Game(gameName, filePath);
            appendOutput("Started new game '" + gameName + "' with " + names.size() + " players from " + filePath + ".");
            killButton.setDisable(false);
            storeButton.setDisable(false);
            refreshRoundDisplay();
        });

        killButton.setOnAction(e -> {
            if (game == null) {
                appendOutput("Start or load a game first.");
                return;
            }
            String name = killField.getText().trim();
            if (name.isEmpty()) {
                appendOutput("Enter a name to kill.");
                return;
            }
            game.kill(name);
            appendOutput("Attempted to kill: " + name);
            refreshRoundDisplay();
        });

        storeButton.setOnAction(e -> {
            if (game == null) {
                appendOutput("Start or load a game first.");
                return;
            }
            game.endRound(); // sets roundNum, saves JSON, increments roundNum

            // Put the top 3 players in the output box
            String top3 = game.listTop3Players();
            outputArea.setText(top3);

            appendOutput("Stored round.");
            // refreshRoundDisplay();
        });

        loadButton.setOnAction(e -> {
            String gameName = gameNameField.getText().trim();
            if (gameName.isEmpty()) {
                appendOutput("Please enter a game name.");
                return;
            }
            String roundText = roundField.getText().trim();
            if (roundText.isEmpty()) {
                appendOutput("Enter a round number to load.");
                return;
            }
            int roundNumber;
            try {
                roundNumber = Integer.parseInt(roundText);
            } catch (NumberFormatException ex) {
                appendOutput("Round number must be an integer.");
                return;
            }

            if (game == null) {
                game = new Game(gameName);
            } else {
                game.setName(gameName);
            }

            game.loadRound(roundNumber);
            appendOutput("Loaded round " + roundNumber + " for game '" + gameName + "'.");
            killButton.setDisable(false);
            storeButton.setDisable(false);
            refreshRoundDisplay();
        });

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        HBox nameRow = new HBox(5, new Label("Game Name:"), gameNameField);
        VBox newGameBox = new VBox(5,
                new Label("Player names (one per line):"),
                namesArea,
                startButton
        );
        HBox killRow = new HBox(5,
                new Label("Kill player:"), killField, killButton
        );
        HBox roundRow = new HBox(5,
                new Label("Round #:"), roundField, loadButton, storeButton
        );

        root.getChildren().addAll(
                nameRow,
                newGameBox,
                killRow,
                roundRow,
                new Label("Current round:"),
                outputArea
        );

        Scene scene = new Scene(root, 600, 500);
        primaryStage.setTitle("Assassins Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void refreshRoundDisplay() {
        if (game != null && game.getCurrRound() != null) {
            outputArea.setText(game.getCurrRound().toString());
        } else {
            outputArea.setText("(no round loaded)");
        }
    }

    private void appendOutput(String message) {
        if (outputArea.getText().isEmpty()) {
            outputArea.setText(message);
        } else {
            outputArea.appendText("\n" + message);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package assassins;

public class Game {

    private String name;
    private Round currRound;
    private int roundNum;

    public Game(String name, String inputFile) {
        this.name = name;
        this.currRound = new Round(name, inputFile, true, 1);
        this.roundNum = 1;
    }

    // Simple constructor used when we only know the game name (e.g., before loading a round)
    public Game(String name) {
        this.name = name;
        this.currRound = null;
        this.roundNum = 1;
    }

    public Round getCurrRound() {
        return currRound;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void kill(String playerName) {
        if (currRound != null && playerName != null && !playerName.isBlank()) {
            currRound.kill(playerName.strip());
        }
    }

    public void newRound() {
        currRound = new Round(name, null, false, roundNum - 1);
    }

    public void endRound() {
        currRound.setRoundNum(roundNum);
        roundNum++;
        currRound.storeRound();
    }

    public void loadRound(int roundNumber) {
        // Load a specific saved round and set up so the next stored round
        // will be the one after the loaded round.
        this.currRound = new Round(name, null, false, roundNumber);
        this.roundNum = roundNumber + 1;
    }

    public String listTop3Players() {
        Player[] topPlayers = currRound.getTopPlayers(3);
        StringBuilder sb = new StringBuilder();

        sb.append("Top 3 Players: \n");

        int rank = 1;
        for (int i = topPlayers.length - 1; i >= 0; i--) {
            Player player = topPlayers[i];
            if (player == null) continue;
            sb.append((rank) + ". " + player.getName() + ": " + player.getNumKills() + " kills\n");
            rank++;
        }

        System.out.println(sb.toString());
        return sb.toString();
    }

    // Testing
    public static void main(String[] args) {
        Game game = new Game("test-from-game", "src/main/resources/gameStart.txt");
        game.currRound.kill("Player 1");
        game.currRound.kill("Player 2");
        game.currRound.kill("Nobody");
        game.endRound();
        game.newRound();
        game.currRound.kill("Player 3");
        game.currRound.kill("Player 4");
        game.currRound.kill("Player 5");
        game.currRound.kill("Player 6");
        game.listTop3Players();
        game.endRound();
        game.newRound();
        game.currRound.kill("Player 7");
    }
}

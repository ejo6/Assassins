package assassins;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.*;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Round {
    private GameNode<Player> head;
    private int gameSize = 0; // Track for list iteration
    private String gameName;
    private int roundNum;
    
    public Round (String gameName, String inputFile,  boolean newGame, int roundNum) {
        this.gameName = gameName;
        this.roundNum = roundNum;

        if (newGame) this.head = startGame(inputFile);
        else this.head = loadRound(roundNum);
    }

    // Start new game from txt file
    public GameNode<Player> startGame(String inputFile) {
        File file = new File(inputFile);
        Player nobody = new Player("Nobody");

        GameNode<Player> dummyHead = new GameNode<>(nobody);
        GameNode<Player> tail = dummyHead; // pointer for appending

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String name = scanner.nextLine().trim();
                if (name.isEmpty()) continue;

                Player newPlayer = new Player(name);
                GameNode<Player> newNode = new GameNode<>(newPlayer);

                tail.setNext(newNode);
                newNode.setPrev(tail);

                tail = newNode;   
                gameSize++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found." + e);
    }

        // Create circular list
        GameNode<Player> head = dummyHead.getNext();
        tail.setNext(head);
        head.setPrev(tail);

        head = randomizePlayers(head); // Turn off for specific tesitng

        return head;
    }

    public static Round fromNames(String gameName, List<String> playerNames, int roundNum) {
        Round round = new Round(gameName, null, false, roundNum);

        Player nobody = new Player("Nobody");
        GameNode<Player> dummyHead = new GameNode<>(nobody);
        GameNode<Player> tail = dummyHead;

        for (String name : playerNames) {
            if (name == null) {
                continue;
            }
            
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            
            Player player = new Player(trimmed);
            GameNode<Player> node = new GameNode<>(player);
            tail.setNext(node);
            node.setPrev(tail);
            tail = node;
            round.gameSize++;
        }

        if (round.gameSize == 0) {
            round.head = null;
            return round;
        }
        
        // Turn into a literal cycle graph (shoutout 2214)
        GameNode<Player> head = dummyHead.getNext();
        tail.setNext(head);
        head.setPrev(tail);

        head = round.randomizePlayers(head);
        round.head = head;

        return round;
    }

    public void setRoundNum(int roundNum) {
        this.roundNum = roundNum;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        GameNode<Player> curr = head;

        for (int i = 0; i < gameSize; i++) {
            sb.append("↓").append(curr.getData().getName()).append("↓\n");
            curr = curr.getNext();
        }
        sb.append("Back to Top");
        return sb.toString();
    }

    public void kill(String killed) {
        if (head == null || gameSize == 0) {
            System.out.println("No players in game.");
            return;
        }

        GameNode<Player> curr = head;

        for (int i = 0; i < gameSize; i++) {
            if (curr.getData().getName().strip().equals(killed)) {
                Player killedPlayer = curr.getData();
                Player killer = curr.getPrev().getData();

                // Update killer's stats
                try {
                    killer.addKill(killedPlayer); // append player object to kills
                    killer.setSafety(true); 
                    killedPlayer.setDead(true);
                } catch (GameException e) {
                    System.out.println(e);
                }

                // Unlink node from circular DLL (curr is the person who just got killed)
                curr.getPrev().setNext(curr.getNext());
                curr.getNext().setPrev(curr.getPrev());

                // If we killed the head, move head forward
                if (curr == head) {
                    head = curr.getNext();
                }

                gameSize--;

                // One player left means game over
                if (gameSize == 1) {
                    System.out.println("GAME OVER: " + head.getData().getName() + " wins!");
                }
                return;
            }
            curr = curr.getNext(); // Iterate
        }
        System.out.println(killed + " not found in game.");
    }

    public GameNode<Player> randomizePlayers(GameNode<Player> head) {
        if (head == null) return null;

        GameNode<Player> curr = head;

        ArrayList<Player> players = head.linkedToArrayList(gameSize);
        ArrayList<Player> newPlayers = new ArrayList<>(); 

        // Sample without replacement randomly and copy over to new list
        while (!players.isEmpty()) {
            int index = (int)(Math.random() * players.size());
            newPlayers.add(players.remove(index));
        }

        // Write new array list to same linked list
        curr = head;
        for (int i = 0; i < newPlayers.size(); i++) {
            curr.setData(newPlayers.get(i));
            curr = curr.getNext();
        }

        return head; 
    }

    public Player getPlayer(String player) {
        if (player == null) return null;

        GameNode<Player> curr = head;        
        for (int i = 0; i < gameSize; i++) {
            if (curr.getData().getName().equals(player)) { 
                return curr.getData();
            } curr = curr.getNext();
        }
        System.out.println("Player not found.");
        return null;
    }

    // <WORKSITE>: GETTING TOP PLAYERS

    public Player[] getTopPlayers(int topN) {
        Player[] topPlayers = new Player[topN];
        ArrayList<Player> players = head.linkedToArrayList(gameSize);
        
        // Initialize priority queue with objective of comparing kills of each player
        PriorityQueue<Player> minHeap = new PriorityQueue<>(Comparator.comparingInt(Player::getNumKills));
        

        for (Player player : players) {
            int numKills = player.getNumKills();
            if (minHeap.size() < topN) {
                minHeap.add(player); // always add to correct place if heap isnt full
            } else if (numKills > minHeap.peek().getNumKills()) {
                // Case of num kills being bigger than the smallest number in the heap after filling
                minHeap.poll();
                minHeap.add(player);
            }
        }

        // Copy into array (ascending)
        int i = 0;
        for (Player player : minHeap) {
            topPlayers[i] = player;
            i++;
        }

        return topPlayers;
    }


    // </WORKSITE>

    public void storeRound() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        ArrayList<Player> players = head.linkedToArrayList(gameSize);

        String json = gson.toJson(players);
        
       // Build correct path
        String dirPath = "src/main/resources/" + gameName;
        String filePath = dirPath + "/round" + roundNum + ".json";

        try {
            // Ensure directory exists
            Files.createDirectories(Paths.get(dirPath));

            // Write JSON file
            Files.writeString(Paths.get(filePath), json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Saved: " + filePath);

        } catch (IOException e) {
            System.out.println("ERROR saving round: " + e.getMessage());
        }
    }

    public GameNode<Player> loadRound(int round) {
        Gson gson = new Gson();

        String jsonFile = "src/main/resources/" + gameName + "/round" + round + ".json";  

        try {
            String json = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(jsonFile)));
            Player[] loadedPlayers = gson.fromJson(json, Player[].class);

            GameNode<Player> head = rebuildList(loadedPlayers);
            return head;
        } catch (IOException e) {
            System.out.println(e);
        } return null;
    }

    // Helper to rebuild list
    private GameNode<Player> rebuildList(Player[] players) {
        Player nobody = new Player("Nobody");
        GameNode<Player> dummyHead = new GameNode<>(nobody);
        GameNode<Player> tail = dummyHead; // pointer for appending

        for (Player player : players) {
            GameNode<Player> node = new GameNode<Player>(player);
            tail.setNext(node);
            node.setPrev(tail);
            tail = tail.getNext();
            gameSize++;
        }
        GameNode<Player> head = dummyHead.getNext();
        tail.setNext(head);
        head.setPrev(tail);

        return head;
    }

    // Testing
    public static void main (String[] args) {
        
        // Test making game
        System.out.println("--Initial Game Test--");
        Round round = new Round("test-from-round", "src/main/resources/gameStart.txt", true, 1);
        System.out.println(round.toString());
        System.out.println("\n");

        // Test kill functionality        
        System.out.println("--Kill Test--");
        round.kill("Player 2");
        round.kill("Player 3");
        System.out.println(round.toString());
        System.out.println("\n");

        // Test top players
        System.out.println("--Top Player Test--");
        int topN = 2;
        Player[] topPlayers = round.getTopPlayers(topN);
        System.out.println("Top Players: ");
        
        for (Player player : topPlayers) {
            System.out.println(topN + ": " + player.getName());
            topN--;
        }
        System.out.println("\n");


        round.storeRound();
    }
}

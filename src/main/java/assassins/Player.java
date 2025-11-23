package assassins;


import java.util.ArrayList;

public class Player {
    // Field variables
    private String name;
    private int kills;
    private ArrayList<String> killList;
    private boolean roundSafe;
    private boolean isDead;
    
    // Loaded in and restored constructor
    public Player(String name, int kills, ArrayList<String> killList, boolean roundSafe, boolean dead) {
        this.name = name;
        this.kills = kills;
        this.killList = killList;
        this.roundSafe = roundSafe;
        this.isDead = dead;
    }

    // Initialized contructor
    public Player(String name) {
        this.name = name;
        this.kills = 0;
        this.killList = new ArrayList<String>();
        this.roundSafe = false;
        this.isDead = false;
    }

    // Getters and setter (basic)
    public String getName() {
        return name;
    }

    public int getNumKills() {
        return kills;
    }
    
    public ArrayList<String> getKills() {
        return killList;
    }

    public boolean isSafe() {
        return roundSafe;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setKills(int newKills) {
        this.kills = newKills;
    }

    public void setSafety(boolean saftety) {
        this.roundSafe = saftety;
    }
    
    public void setDead(boolean lifeStatus) {
        this.isDead = lifeStatus;
    }

    // Getters and setters (complex)
    public void addKill(Player player) throws GameException{
        if (isDead()) throw new GameException("Cannot add a dead player as a kill.");
        this.killList.add(player.getName());
        this.kills++;
    }

    public void removeKill(Player player) throws GameException {
        if (!killList.contains(player.getName())) throw new GameException("Player not currently listed as a kill.");
        this.killList.remove(player.getName());
        this.kills--;
        player.setDead(false);
    }
}

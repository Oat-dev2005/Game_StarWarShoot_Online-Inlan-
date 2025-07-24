import java.io.Serializable;

public class GameData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String playerName;
    private int aimX, aimY;
    private boolean isShooting;
    private boolean targetHit;

    public GameData(String playerName, int aimX, int aimY, boolean isShooting, boolean targetHit) {
        this.playerName = playerName;
        this.aimX = aimX;
        this.aimY = aimY;
        this.isShooting = isShooting;
        this.targetHit = targetHit;
    }

    // Getters และ Setters
    public String getPlayerName() { return playerName; }
    public int getAimX() { return aimX; }
    public int getAimY() { return aimY; }
    public boolean isShooting() { return isShooting; }
    public boolean isTargetHit() { return targetHit; }
}

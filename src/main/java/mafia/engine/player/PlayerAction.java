package mafia.engine.player;

public enum PlayerAction {
    HEAL,
    KILL,
    TAKEDOWN,
    INVESTIGATE;

    public static PlayerAction parse(String s) {
        s = s.toLowerCase();

        if (s.contains("kill")) {
            return KILL;
        }
        if (s.contains("heal")) {
            return HEAL;
        }
        if (s.contains("investigate")) {
            return INVESTIGATE;
        }
        if (s.contains("takedown")) {
            return TAKEDOWN;
        }
        throw new IllegalStateException("Unexpected action: " + s);
    }
}

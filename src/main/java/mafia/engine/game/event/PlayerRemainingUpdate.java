package mafia.engine.game.event;

import java.util.List;

import lombok.Getter;
import lombok.experimental.Accessors;
import mafia.engine.player.Player;

@Accessors(fluent = true)
public class PlayerRemainingUpdate extends GameUpdate {

    @Getter
    private final List<Player> remainingPlayers;

    public PlayerRemainingUpdate(List<Player> remainingPlayers) {
        this.remainingPlayers = remainingPlayers;
    }    
}

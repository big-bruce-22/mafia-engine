package mafia.engine.game.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import mafia.engine.player.Player;
import mafia.engine.player.PlayerState;

@RequiredArgsConstructor
public class NightEvent {

    @NonNull @Getter 
    
    private final PlayerState state;
    @NonNull @Getter 
    
    private final Player player;

    @Override
    public String toString() {
        return switch (state) {
            case ALIVE -> player.name() + " is alive";
            case DEAD -> player.name() + " is dead";
            case KILLED -> player.name() + " has been killed";
            case SAVED -> player.name() + " has been saved";
            default -> throw new IllegalStateException("Unexpected state: " + state);
        };
    }
}

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
            case ALIVE -> player.getName() + " is alive";
            case DEAD -> player.getName() + " is dead";
            case KILLED -> player.getName() + " has been killed";
            case SAVED -> player.getName() + " has been saved";
            default -> throw new IllegalStateException("Unexpected state: " + state);
        };
    }
}

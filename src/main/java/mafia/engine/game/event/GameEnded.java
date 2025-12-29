package mafia.engine.game.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class GameEnded extends GameUpdate {

    @Getter
    private final String winner;
    
}

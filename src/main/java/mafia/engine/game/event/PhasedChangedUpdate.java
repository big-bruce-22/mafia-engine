package mafia.engine.game.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import mafia.engine.core.GamePhase;

@RequiredArgsConstructor
@Accessors(fluent=true)
public class PhasedChangedUpdate extends GameUpdate {

    @Getter
    private final GamePhase previousPhase, newPhase;    
}

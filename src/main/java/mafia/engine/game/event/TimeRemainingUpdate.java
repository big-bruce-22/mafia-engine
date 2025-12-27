package mafia.engine.game.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import mafia.engine.core.GamePhase;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class TimeRemainingUpdate extends GameUpdate {

    @Getter
    private final GamePhase phase;

    @Getter
    private final int secondsRemaining;

    public TimeRemainingUpdate(GamePhase phase, long secondsRemaining) {
        this.phase = phase;
        this.secondsRemaining = (int) secondsRemaining;
    }
    
}

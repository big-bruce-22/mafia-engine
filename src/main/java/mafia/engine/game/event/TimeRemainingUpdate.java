package mafia.engine.game.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class TimeRemainingUpdate extends GameUpdate {

    @Getter
    private final String context;

    @Getter
    private final int secondsRemaining;

    @Getter
    private final String message;

    public TimeRemainingUpdate(String context, int secondsRemaining) {
        this.context = context;
        this.secondsRemaining = secondsRemaining;
        this.message = "";
    }

    public TimeRemainingUpdate(String context, long secondsRemaining) {
        this.context = context;
        this.secondsRemaining = (int) secondsRemaining;
        this.message = "";
    }

    public TimeRemainingUpdate(String context, long secondsRemaining, String message) {
        this.context = context;
        this.secondsRemaining = (int) secondsRemaining;
        this.message = message;
    }
    
}

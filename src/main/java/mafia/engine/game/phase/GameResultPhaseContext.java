package mafia.engine.game.phase;

import lombok.RequiredArgsConstructor;
import mafia.engine.game.GameResult;

@RequiredArgsConstructor
public class GameResultPhaseContext implements PhaseContext<GameResult> {

    private final GameResult gameResult;

    @Override
    public GameResult getResult() {
        return gameResult;
    }
    
}

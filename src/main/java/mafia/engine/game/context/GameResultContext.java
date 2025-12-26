package mafia.engine.game.context;

import lombok.RequiredArgsConstructor;
import mafia.engine.core.GameResult;

@RequiredArgsConstructor
public class GameResultContext implements ChannelContext<GameResult> {

    private final GameResult gameResult;

    @Override
    public GameResult getContext() {
        return gameResult;
    }
    
}

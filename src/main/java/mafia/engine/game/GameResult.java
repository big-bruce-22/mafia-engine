package mafia.engine.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GameResult {

    @Getter
    private final String result;

    @Override
    public String toString() {
        return result;
    }
    
}

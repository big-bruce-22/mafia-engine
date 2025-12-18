package mafia.engine.game.vote;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import mafia.engine.player.Player;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class PlayerVote {

    @NonNull @Getter
    private final Player player, target;    
}

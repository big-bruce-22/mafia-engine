package mafia.engine.game.vote;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import mafia.engine.player.Player;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class VoteResult {

    @Getter
    private final Player target;

    @Getter
    private final int votes;

    @Getter
    private final String message;

    public String toString() {
        if (target == null) {
            return "No one was voted for.";
        } else {
            return target.name() + " received " + votes + " votes. " + (message == null ? "" : message);
        }
    }    
}

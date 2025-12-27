package mafia.engine.game.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import mafia.engine.vote.VoteResult;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class VotingResultUpdate extends GameUpdate {

    @Getter
    private final VoteResult voteResult;
}

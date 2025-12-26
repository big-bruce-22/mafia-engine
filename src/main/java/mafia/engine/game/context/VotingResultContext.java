package mafia.engine.game.context;

import lombok.RequiredArgsConstructor;
import mafia.engine.vote.VoteResult;

@RequiredArgsConstructor
public class VotingResultContext implements ChannelContext<VoteResult> {

    private final VoteResult result;

    @Override
    public VoteResult getContext() {
        return result;
    }
}

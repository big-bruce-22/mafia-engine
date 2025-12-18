package mafia.engine.game.phase;

import lombok.RequiredArgsConstructor;
import mafia.engine.game.vote.VoteResult;

@RequiredArgsConstructor
public class VotingResultPhaseContext implements PhaseContext<VoteResult> {

    private final VoteResult result;

    @Override
    public VoteResult getResult() {
        return result;
    }
}

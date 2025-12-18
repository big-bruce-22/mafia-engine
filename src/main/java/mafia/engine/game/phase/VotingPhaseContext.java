package mafia.engine.game.phase;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mafia.engine.game.vote.PlayerVote;

public class VotingPhaseContext implements PhaseContext<List<PlayerVote>> {
    
    @NonNull @Setter @Getter
    private List<PlayerVote> votes = new ArrayList<>();

    public void addVote(PlayerVote vote) {
        votes.add(vote);
    }

    @Override
    public List<PlayerVote> getResult() {
        return votes;   
    }
    
}

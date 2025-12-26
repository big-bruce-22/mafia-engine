package mafia.engine.game.context;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mafia.engine.vote.PlayerVote;

public class VotingContext implements ChannelContext<List<PlayerVote>> {
    
    @NonNull @Setter @Getter
    private List<PlayerVote> votes = new ArrayList<>();

    public void addVote(PlayerVote vote) {
        votes.add(vote);
    }

    @Override
    public List<PlayerVote> getContext() {
        return votes;   
    }    
}

package mafia.engine.core.dispatcher;

import java.util.ArrayList;
import java.util.List;

import mafia.engine.core.GameChannels;
import mafia.engine.game.channel.message.prompt.VotePromptResponse;
import mafia.engine.property.Properties;
import mafia.engine.vote.PlayerVote;

public final class VotingPhaseDispatcher extends PhaseDispatcher {

    private final List<PlayerVote> votes = new ArrayList<>();

    public VotingPhaseDispatcher(Properties gameProperties, GameChannels gameChannels) {
        super(gameProperties, gameChannels);
    }

    @Override
    public void start() { }

    @Override
    public void stop() {
        while (gameChannels.promptResponseChannel().hasSent()) {
            var response = gameChannels.promptResponseChannel().receive();
            if (response instanceof VotePromptResponse res) {
                votes.add(new PlayerVote(res.source(), res.voteOption().player()));
            }
        }
    }

    public List<PlayerVote> votes() {
        return votes;
    }
}

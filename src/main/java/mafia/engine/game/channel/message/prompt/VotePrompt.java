package mafia.engine.game.channel.message.prompt;

import java.util.List;

import lombok.Getter;
import lombok.experimental.Accessors;
import mafia.engine.player.Player;

@Accessors(fluent = true)
public class VotePrompt extends Prompt {

    @Getter
    private final List<VotePromptOption> voteOptions;

    public VotePrompt(Player target, List<VotePromptOption> voteOptions) {
        this.target = target;
        this.prompt = "Select a player to vote out";
        this.voteOptions = voteOptions;
    }
}

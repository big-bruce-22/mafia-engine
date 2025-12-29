package mafia.engine.game.channel.prompt;

import lombok.Getter;
import lombok.experimental.Accessors;
import mafia.engine.player.Player;

@Accessors(fluent = true)
public class VotePromptResponse extends PromptResponse {

    @Getter
    private final VotePromptOption voteOption;

    public VotePromptResponse(Player source, VotePromptOption option) {
        super(source, option);
        this.option = option;
        this.voteOption = option;
    }
    
}

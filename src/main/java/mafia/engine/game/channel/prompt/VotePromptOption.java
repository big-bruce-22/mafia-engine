package mafia.engine.game.channel.prompt;

import lombok.Getter;
import lombok.experimental.Accessors;
import mafia.engine.player.Player;

@Accessors(fluent = true)
public class VotePromptOption extends PromptOption {

    @Getter
    private final Player player;

    public VotePromptOption(Player player) {
        super(player.name());
        this.player = player;
    }
    
}

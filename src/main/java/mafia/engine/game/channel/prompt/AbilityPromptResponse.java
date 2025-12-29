package mafia.engine.game.channel.prompt;

import lombok.Getter;
import lombok.experimental.Accessors;
import mafia.engine.player.Player;

@Accessors(fluent = true)
public class AbilityPromptResponse extends PromptResponse {

    @Getter
    private final Player target;

    public AbilityPromptResponse(Player source, PromptOption option, Player target) {
        super(source, option);
        this.target = target;
    }
}

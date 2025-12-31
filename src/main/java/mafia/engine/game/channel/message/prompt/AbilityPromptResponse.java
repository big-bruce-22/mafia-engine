package mafia.engine.game.channel.message.prompt;

import lombok.Getter;
import lombok.experimental.Accessors;
import mafia.engine.player.Player;

@Accessors(fluent = true)
public class AbilityPromptResponse extends PromptResponse {

    @Getter
    private final Player target;

    @Getter
    private final AbilityPromptOption abilityOption;

    public AbilityPromptResponse(Player source, AbilityPromptOption abilityOption, Player target) {
        super(source, abilityOption);
        this.target = target;
        this.abilityOption = abilityOption;
    }
}

package mafia.engine.game.channel.message.prompt;

import lombok.Getter;
import lombok.experimental.Accessors;
import mafia.engine.ability.Ability;

@Accessors(fluent = true)
public class AbilityPromptOption extends PromptOption {

    @Getter
    private final Ability ability;

    public AbilityPromptOption(Ability ability ) {
        super(ability.name());
        this.ability = ability;
    }
}

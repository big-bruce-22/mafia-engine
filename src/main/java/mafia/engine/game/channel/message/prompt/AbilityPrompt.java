package mafia.engine.game.channel.message.prompt;

import static mafia.engine.util.StreamUtils.mapToList;

import java.util.List;

import lombok.Getter;
import lombok.experimental.Accessors;
import mafia.engine.player.Player;

@Accessors(fluent=true)
public class AbilityPrompt extends Prompt {

    @Getter
    private final List<Player> availableTargets;

    @Getter
    private final List<AbilityPromptOption> abilityOptions;

    public AbilityPrompt(Player target, List<AbilityPromptOption> abilityOptions, List<Player> availableTargets) {
        this.target = target;
        this.prompt = "Select your ability to use this night";
        this.abilityOptions = abilityOptions;
        this.options = mapToList(abilityOptions, PromptOption.class::cast);
        this.availableTargets = availableTargets;
    }
}

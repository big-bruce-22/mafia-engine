package mafia.engine.game.channel.prompt;

import java.util.List;

import lombok.Getter;
import lombok.experimental.Accessors;
import mafia.engine.player.Player;

@Accessors(fluent=true)
public class AbilityPrompt extends Prompt {

    @Getter
    private final List<Player> availableTargets;

    public AbilityPrompt(Player target, List<PromptOption> options, List<Player> availableTargets) {
        this.target = target;
        this.prompt = "Select your ability to use this night";
        this.options = options;
        this.availableTargets = availableTargets;
    }
}

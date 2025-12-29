package mafia.engine.game.channel.prompt;

import java.util.List;

import lombok.Getter;
import lombok.experimental.Accessors;
import mafia.engine.player.Player;

@Accessors(fluent = true)
public abstract class Prompt {

    @Getter
    protected Player target;

    @Getter
    protected String prompt;

    @Getter
    protected List<PromptOption> options;
}
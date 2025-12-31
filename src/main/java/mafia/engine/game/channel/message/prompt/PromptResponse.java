package mafia.engine.game.channel.message.prompt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import mafia.engine.player.Player;

@AllArgsConstructor
@Accessors(fluent = true)
public class PromptResponse {

    @Getter
    protected Player source;

    @Getter
    protected PromptOption option;    
}

package mafia.engine.game.channel.message.prompt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class PromptOption {

    @Getter
    private final String option;
    
}

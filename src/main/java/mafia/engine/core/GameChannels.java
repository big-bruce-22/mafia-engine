package mafia.engine.core;

import lombok.Getter;
import lombok.experimental.Accessors;
import mafia.engine.game.channel.BlockingChannel;
import mafia.engine.game.channel.message.Information;
import mafia.engine.game.channel.message.prompt.Prompt;
import mafia.engine.game.channel.message.prompt.PromptResponse;
import mafia.engine.game.event.GameUpdate;

@Accessors(fluent = true)
public class GameChannels {

    @Getter 
    private final BlockingChannel<Prompt> promptChannel = new BlockingChannel<>();
    
    @Getter 
    private final BlockingChannel<PromptResponse> promptResponseChannel= new BlockingChannel<>();
    
    @Getter 
    private final BlockingChannel<GameUpdate> gameUpdateChannel = new BlockingChannel<>();

    @Getter
    private final BlockingChannel<Information> informationChannel = new BlockingChannel<>();
    
}

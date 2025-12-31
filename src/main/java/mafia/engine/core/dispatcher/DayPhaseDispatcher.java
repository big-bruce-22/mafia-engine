package mafia.engine.core.dispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import mafia.engine.core.GameChannels;
import mafia.engine.game.channel.message.prompt.AbilityPromptResponse;
import mafia.engine.player.action.PlayerActionContext;
import mafia.engine.property.Properties;

public final class DayPhaseDispatcher extends PhaseDispatcher {

    private final List<AbilityPromptResponse> deferredResponses;

    public DayPhaseDispatcher(
        List<AbilityPromptResponse> deferredResponses,
        Properties gameProperties,
        GameChannels gameChannels

    ) {
        super(gameProperties, gameChannels);
        this.deferredResponses = deferredResponses;
    }

    @Override
    public void start() { }

    @Override
    public void stop() { }

    public List<PlayerActionContext> resolve(Function<AbilityPromptResponse, PlayerActionContext> resolveAbilityResponse) {
        var contexts = new ArrayList<PlayerActionContext>();
        for (var res : deferredResponses) {
            contexts.add(resolveAbilityResponse.apply(res));
        }
        return contexts;
    }
}

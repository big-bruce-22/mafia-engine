package mafia.engine.core.dispatcher;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import lombok.Setter;
import lombok.experimental.Accessors;

import mafia.engine.core.GameChannels;
import mafia.engine.game.channel.message.Information;
import mafia.engine.game.channel.message.Message;
import mafia.engine.game.channel.message.prompt.AbilityPromptResponse;
import mafia.engine.game.channel.message.prompt.PromptResponse;
import mafia.engine.game.event.GameUpdate;
import mafia.engine.player.Player;
import mafia.engine.property.Properties;

@Accessors(fluent = true)
public final class NightPhaseDispatcher extends PhaseDispatcher {


    private final Set<Player> responded = ConcurrentHashMap.newKeySet();
    private final Queue<AbilityPromptResponse> deferred = new ConcurrentLinkedQueue<>();

    private volatile boolean running = true;

    @Setter
    private Function<AbilityPromptResponse, Message> immediateAbilityResponseResolver;

    public NightPhaseDispatcher(Properties gameProperties, GameChannels gameChannels) {
        super(gameProperties, gameChannels);
    }

    @Override
    public void start() {
        Thread.ofVirtual().start(this::listen);
    }

    @Override
    public void stop() {
        running = false;
    }

    private void listen() {
        while (running) {
            if (!gameChannels.promptResponseChannel().hasSent()) {
                Thread.onSpinWait();
                continue;
            }

            var response = gameChannels.promptResponseChannel().receive();
            handle(response);
        }
    }

    private void handle(PromptResponse response) {
        if (!(response instanceof AbilityPromptResponse res)) {
            return;
        }

        if (!responded.add(res.source())) {
            return;
        }

        if (res.abilityOption().ability().immediateResult()) {
            var result = immediateAbilityResponseResolver.apply(res);
            sendPrivateAbilityResult(res.source(), result);
        } else {
            deferred.add(res);
        }
    }

    public List<AbilityPromptResponse> drainDeferred() {
        return List.copyOf(deferred);
    }

    private void sendPrivateAbilityResult(Player player, Message message) {
        switch (message) {
            case Information info -> {
                gameChannels.informationChannel().send(info);
            }
            case GameUpdate gameUpdate -> {
                gameChannels.gameUpdateChannel().send(gameUpdate);
            }
            default -> throw new IllegalStateException("Unexpected message type: " + message.getClass());
        }
    }
}

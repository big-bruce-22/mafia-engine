package client;

import java.util.ArrayList;
import java.util.Collections;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import mafia.engine.game.channel.SimpleChannel;
import mafia.engine.game.channel.prompt.AbilityPrompt;
import mafia.engine.game.channel.prompt.AbilityPromptResponse;
import mafia.engine.game.channel.prompt.Prompt;
import mafia.engine.game.channel.prompt.PromptResponse;
import mafia.engine.game.channel.prompt.VotePrompt;
import mafia.engine.game.channel.prompt.VotePromptResponse;
import mafia.engine.game.event.GameUpdate;
import mafia.engine.player.Player;

@Accessors(fluent = true)
public class Client {

    @Getter 
    private final SimpleChannel<Prompt> promptChannel = new SimpleChannel<>();

    @Getter 
    private final SimpleChannel<PromptResponse> promptResponseChannel = new SimpleChannel<>();

    @Getter 
    private final SimpleChannel<GameUpdate> gameUpdateChannel = new SimpleChannel<>();

    @Setter @Getter
    private Player player;

    public Client() {
        promptChannel.subscribe(this::handlePrompt);
    }

    public Client(Player player) {
        this.player = player;
        promptChannel.subscribe(this::handlePrompt);
    }

    private void handlePrompt(Prompt prompt) {
        if (prompt instanceof AbilityPrompt abilityPrompt) {
            // System.out.println("Client " + player.name() + " received AbilityPrompt: " + abilityPrompt.prompt());
            // System.out.println("Available options: ");
            // abilityPrompt.options().forEach(option -> System.out.println(option.option()));

            var randomTarget = randomFromList(new ArrayList<>(abilityPrompt.availableTargets()));
            var randomOption = randomFromList(new ArrayList<>(abilityPrompt.options()));
            var response = new AbilityPromptResponse(
                player,
                randomOption,
                randomTarget
            );
            // System.out.println("Client " + player.name() + " responding with option: " + randomOption.option() + " targeting " + randomTarget.name());

            promptResponseChannel.publish(response);
        }

        if (prompt instanceof VotePrompt votePrompt) {
            // System.out.println("Client " + player.name() + " received VotePrompt: " + votePrompt.prompt());
            var randomOption = randomFromList(new ArrayList<>(votePrompt.voteOptions()));
            var response = new VotePromptResponse(
                player,
                randomOption
            );

            // System.out.println("Client " + player.name() + " responding with vote: " + randomOption.option());

            promptResponseChannel.publish(response);
        }
    }

    private <T> T randomFromList(ArrayList<T> list) {
        Collections.shuffle(list);
        return list.getFirst();
    }
}

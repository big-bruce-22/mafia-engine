package client;

import java.util.ArrayList;
import java.util.Collections;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import mafia.engine.game.channel.SimpleChannel;
import mafia.engine.game.channel.message.Information;
import mafia.engine.game.channel.message.prompt.AbilityPrompt;
import mafia.engine.game.channel.message.prompt.AbilityPromptResponse;
import mafia.engine.game.channel.message.prompt.Prompt;
import mafia.engine.game.channel.message.prompt.PromptResponse;
import mafia.engine.game.channel.message.prompt.VotePrompt;
import mafia.engine.game.channel.message.prompt.VotePromptResponse;
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

    @Getter 
    private final SimpleChannel<Information> informationChannel = new SimpleChannel<>();

    @Setter @Getter
    private Player player;

    public Client() {
        promptChannel.subscribe(this::handlePrompt);
    }

    public Client(Player player) {
        this.player = player;
        promptChannel.subscribe(this::handlePrompt);
        informationChannel.subscribe(this::handleInformation);
    }

    private void handleInformation(Information information) {
        // For now, just print the information
        System.out.println("client: Client " + player.name() + " received Information: " + information.info());
    }

    private void handlePrompt(Prompt prompt) {
        if (prompt instanceof AbilityPrompt abilityPrompt) {
            // var shouldUseAbility = Math.random() < 0.5;
            // if (!shouldUseAbility && !player.alignment().equalsIgnoreCase("evil")) {
            //     return;
            // }

            var randomTarget = randomFromList(new ArrayList<>(abilityPrompt.availableTargets()));
            var randomOption = randomFromList(new ArrayList<>(abilityPrompt.abilityOptions()));
            var response = new AbilityPromptResponse(
                player,
                randomOption,
                randomTarget
            );
            // System.out.println("client: Client " + player.name() + " received AbilityPrompt: " + abilityPrompt.prompt() + " and responded with option: " + randomOption.option() + " targeting " + randomTarget.name());

            promptResponseChannel.publish(response);
            // System.out.println("Available options: ");
            // abilityPrompt.options().forEach(option -> System.out.println(option.option()));

            // var options = filter(abilityPrompt.options(), o -> !o.option().equals(player.name()));
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

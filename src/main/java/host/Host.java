package host;

import java.util.ArrayList;
import java.util.List;

import client.Client;
import client.SpectatorClient;

import mafia.engine.core.GameConfiguration;
import mafia.engine.core.GameEngine;
import mafia.engine.core.GameRules;
import mafia.engine.game.channel.BlockingToSimpleAdapter;
import mafia.engine.game.channel.SimpleChannel;
import mafia.engine.game.channel.SimpleToBlockingAdapter;
import mafia.engine.game.channel.message.Information;
import mafia.engine.game.channel.message.prompt.Prompt;
import mafia.engine.game.event.GameUpdate;
import mafia.engine.player.Player;
import mafia.engine.player.PlayerState;
import mafia.engine.presets.Preset;
import mafia.engine.property.Properties;
import mafia.engine.role.Role;
import mafia.engine.util.StreamUtils;

public class Host {

    private GameEngine engine;

    private List<Client> clients = new ArrayList<>();
    private List<SpectatorClient> spectators = new ArrayList<>();

    private GameConfiguration gameConfig;
    private GameRules gameRules;

    private List<Role> primaryRoles, secondaryRoles;

    private Preset preset;

    public Properties getGameProperties() {
        return engine.gameProperties();
    }

    public void configure(GameConfiguration gameConfig,GameRules gameRules) {
        this.gameConfig = gameConfig;
        this.gameRules = gameRules;
    }

    public void preset(Preset preset) {
        this.preset = preset;
    }

    public void connectClient(Client client) {
        // TODO actually connect it
        clients.add(client);
    }

    public void connectSpectator(SpectatorClient spectator) {
        // TODO actually connect it
        spectators.add(spectator);
    }
    
    public void loadRoles(List<Role> primaryRoles, List<Role> secondaryRoles) {
        this.primaryRoles = primaryRoles;
        this.secondaryRoles = secondaryRoles;
    }

    public void startGame() {
        var players = new ArrayList<>(StreamUtils.mapToList(clients, Client::player));
        players.forEach(p -> p.state(PlayerState.ALIVE));
        engine = new GameEngine(players, primaryRoles, secondaryRoles, preset, gameRules);
        engine.configure(gameConfig);

        establishConnections();

        new Thread(engine::start).start();
    }

    private void establishConnections() {
        // ONE broadcast channel
        SimpleChannel<GameUpdate> broadcastChannel = new SimpleChannel<>();

        // ONE adapter
        new BlockingToSimpleAdapter<>(
            engine.gameChannels().gameUpdateChannel(),
            broadcastChannel
        );

        // clients subscribe
        clients.forEach(c ->
            broadcastChannel.subscribe(c.gameUpdateChannel()::publish)
        );

        // spectators subscribe
        spectators.forEach(s ->
            broadcastChannel.subscribe(s.gameUpdateChannel()::publish)
        );

        // all prompt responses go to engine
        clients.forEach(c ->
            new SimpleToBlockingAdapter<>(
                c.promptResponseChannel(),
                engine.gameChannels().promptResponseChannel()
            )
        );

        // redirect prompts from engine to clients
        new Thread(() -> {
            var promptChannel = engine.gameChannels().promptChannel();
            while (true) {
                Prompt prompt = promptChannel.receive();
                Client targetClient = findClientByPlayer(prompt.target());
                targetClient.promptChannel().publish(prompt);
            }
        }, "PromptForwarder").start();

        // redirect prompts from engine to clients
        new Thread(() -> {
            var informationChannel = engine.gameChannels().informationChannel();
            while (true) {
                Information information = informationChannel.receive();
                Client targetClient = findClientByPlayer(information.target());
                targetClient.informationChannel().publish(information);
            }
        }, "InformationForwarder").start();
    }

    private Client findClientByPlayer(Player player) {
        return clients.stream()
            .filter(c -> c.player().equals(player))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No client found for player: " + player.name()));
    }
}

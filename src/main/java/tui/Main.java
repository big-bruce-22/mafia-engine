package tui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import client.Client;
import client.SpectatorClient;
import host.Host;
import mafia.engine.config.PresetsConfig;
import mafia.engine.config.RoleConfig;
import mafia.engine.config.loader.Loader;
import mafia.engine.core.GameConfiguration;
import mafia.engine.core.GameRules;
import mafia.engine.player.Player;
import mafia.engine.util.StreamUtils;

public class Main {
    
    public static void main(String[] args) throws Exception {
        RoleConfig primaryRoleConfig = Loader.load("mafia-engine/PrimaryRoles.yaml", RoleConfig.class);
        RoleConfig secondaryRoleConfig = Loader.load("mafia-engine/SecondaryRoles.yaml", RoleConfig.class);
        PresetsConfig presetsConfig = Loader.load("mafia-engine/Presets.yaml", PresetsConfig.class);
        GameConfiguration gameConfig = Loader.load("mafia-engine/GameConfiguration.yaml", GameConfiguration.class);
        GameRules gameRules  = Loader.load("mafia-engine/GameRules.yaml", GameRules.class);

        Host host = new Host();
        host.configure(gameConfig, gameRules);
        host.preset(presetsConfig.getPresets().get(1));
        host.loadRoles(primaryRoleConfig.getRoles(), secondaryRoleConfig.getRoles());
        
        generateClients().forEach(host::connectClient);

        SpectatorClient spectator = new SpectatorClient();
        host.connectSpectator(spectator);

        host.startGame();
    }

    private static List<Client> generateClients() {
        return StreamUtils.mapToList(generatePlayers(), Client::new);
    }

    private static List<Player> generatePlayers() {
        String[] names = {
            "Wayne",
            "Mark",
            "Aez",
            "Xav",
            "Pat",
            "Ricia",
            "Ruth",
            "Pj",
            "Claire",
            "Wilson",
            "Ralph"
        };

        return new ArrayList<>(Arrays.asList(classNames)
            .stream()
            .map(n -> new Player().name(n))
            .toList());
    }

    private static final String[] classNames = {
        "Ruth",
        "Chasten",
        "Wayne",
        "Jayjay",
        "Jahred",
        "Arnold",
        "Tristan",
        "Patrick",
        "Xavier",
        "Phoebe",
        "Mary",
        "Mark",
        "Jewel",
        "Adelaine",
        "Princess",
        "Juan",
        "Ralph",
        "Ricia",
        "Archangel",
        "Warren",
        "Michael",
        "Marie",
        "Aezekiel",
        "Jhayden",
        "John",
        "Criztian",
        "Carlos",
        "Naomi",
        "Jimwell",
        "Wilson",
        "Tristan",
        "Jeremy"
    };
}

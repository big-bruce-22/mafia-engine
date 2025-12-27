package client;

import lombok.Getter;
import lombok.experimental.Accessors;
import mafia.engine.game.channel.SimpleChannel;
import mafia.engine.game.event.GameEnded;
import mafia.engine.game.event.GameUpdate;
import mafia.engine.game.event.NightActionResolutionUpdate;
import mafia.engine.game.event.PhasedChangedUpdate;
import mafia.engine.game.event.RoleRevealUpdate;
import mafia.engine.game.event.TimeRemainingUpdate;
import mafia.engine.game.event.VotingResultUpdate;

@Accessors(fluent = true)
public class SpectatorClient {

    @Getter
    private final SimpleChannel<GameUpdate> gameUpdateChannel = new SimpleChannel<>();

    public SpectatorClient() {
        gameUpdateChannel.subscribe(this::handleUpdate);
    }

    private void handleUpdate(GameUpdate update) {
        if (update instanceof PhasedChangedUpdate phaseChanged) {
            System.out.println();
            System.out.println();
            System.out.println(phaseChanged.newPhase() + " time!");
        }

        if (update instanceof RoleRevealUpdate roleReveal) {
            for (var reveal : roleReveal.reveals()) {
                System.out.print(reveal.player().name() + " is a " + reveal.role().getRoleName());
                if (reveal.secondaryRole() != null) {
                    System.out.print(" and " + reveal.secondaryRole().getRoleName() + "\n");
                }
            }
        }

        if (update instanceof TimeRemainingUpdate timeRemaining) {
            System.out.printf("\rTime remaining in phase: " + timeRemaining.secondsRemaining() + " seconds");
        }

        if (update instanceof VotingResultUpdate votingResult) {
            System.out.println();
            System.out.println(votingResult.voteResult());
        }

        if (update instanceof GameEnded gameEnded) {
            System.out.println();
            System.out.println(gameEnded.winner());
            System.exit(0);
        }

        if (update instanceof NightActionResolutionUpdate nightActionResolution) {
            nightActionResolution.resolvedEvents().forEach(System.out::println);
            System.out.println();
        }
    }
}

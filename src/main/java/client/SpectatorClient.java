package client;

import lombok.Getter;
import lombok.experimental.Accessors;

import mafia.engine.game.channel.SimpleChannel;
import mafia.engine.game.event.GameEnded;
import mafia.engine.game.event.GameUpdate;
import mafia.engine.game.event.NightActionResolutionUpdate;
import mafia.engine.game.event.PhasedChangedUpdate;
import mafia.engine.game.event.PlayerRemainingUpdate;
import mafia.engine.game.event.RoleRevealUpdate;
import mafia.engine.game.event.TimeRemainingUpdate;
import mafia.engine.game.event.VotingResultUpdate;

import tui.SplitPrinter;

@Accessors(fluent = true)
public class SpectatorClient {

    @Getter
    private final SimpleChannel<GameUpdate> gameUpdateChannel = new SimpleChannel<>();

    public SpectatorClient() {
        gameUpdateChannel.subscribe(this::handleUpdate);
    }

    private void handleUpdate(GameUpdate update) {
        if (update instanceof PhasedChangedUpdate phaseChanged) {
            SplitPrinter.println("spectator", phaseChanged.newPhase() + " time!");
        }

        if (update instanceof RoleRevealUpdate roleReveal) {
            if (roleReveal.reveals().isEmpty()) {
                return;
            }
            
            for (var reveal : roleReveal.reveals()) {
                SplitPrinter.print("spectator", reveal.player().name() + " is a " + reveal.role().getRoleName());
                if (reveal.secondaryRole() != null) {
                    SplitPrinter.print("spectator", " and " + reveal.secondaryRole().getRoleName());
                }
                SplitPrinter.println("spectator");
            }
            SplitPrinter.println("spectator");
        }

        if (update instanceof TimeRemainingUpdate timeRemaining) {
            SplitPrinter.printf("spectator", "\rTime left: " + timeRemaining.secondsRemaining() + "s");
            if (timeRemaining.message() != null && !timeRemaining.message().isEmpty()) {
                SplitPrinter.print("spectator", " (" + timeRemaining.message() + ")");
            }
            if (timeRemaining.secondsRemaining() == 0) {
                SplitPrinter.println("spectator");
                SplitPrinter.println("spectator");
            }
        }

        if (update instanceof VotingResultUpdate votingResult) {
            SplitPrinter.println("spectator", votingResult.voteResult());
            SplitPrinter.println("spectator");
        }

        if (update instanceof GameEnded gameEnded) {
            SplitPrinter.println("spectator", gameEnded.winner());
            // SplitPrinter.printFlush("spectator");
            // SplitPrinter.printAll();
            System.exit(0);
        }

        if (update instanceof NightActionResolutionUpdate nightActionResolution) {
            for (var event : nightActionResolution.resolvedEvents()) {
                SplitPrinter.println("spectator", event);
            }
            SplitPrinter.println("spectator");
        }

        if (update instanceof PlayerRemainingUpdate playerRemainingUpdate) {
            SplitPrinter.println("spectator", "Players remaining: " + playerRemainingUpdate.remainingPlayers().size());
            SplitPrinter.println("spectator");
            // for (var player : playerRemainingUpdate.remainingPlayers()) {
            //     SplitPrinter.println("spectator", "- " + player.name());
            // }
        }
    }
}

package mafia.engine.ability;

import mafia.engine.player.Player;

public record InvestigationResult(
    Player target,
    String result
) implements AbilityResult {}

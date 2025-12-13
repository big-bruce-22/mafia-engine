package mafia.engine.player;

import java.util.List;

import mafia.engine.ability.AbilityEngine;
import mafia.engine.context.Context;
import mafia.engine.game.GameEvent;
import mafia.engine.rule.RuleEngine;

// classic gameplay for now
public class PlayerEngine {

    private AbilityEngine abilityEngine = new AbilityEngine();
    private RuleEngine ruleEngine = new RuleEngine();

    public void updatePlayersState(List<Context> contexts, List<Player> players) {
        for (var ctx : contexts) {
            ruleEngine.process(GameEvent.BEFORE_ABILITY, ctx);

            if (ctx.cancelled()) {
                continue;
            }

            abilityEngine.registerAction(ctx);
            var result = new ActionResult(ctx);

            ctx.actor().getActionResults().add(result);
            ctx.actionResult(result);

            ruleEngine.process(GameEvent.AFTER_ABILITY, ctx);
        }

        for (var player : players) {
            var attemptedKills = player.getAttemptedActions().get(PlayerAction.KILL);
            var attemptedHeals = player.getAttemptedActions().get(PlayerAction.HEAL);

            attemptedKills = attemptedKills == null ? 0 : attemptedKills;
            attemptedHeals = attemptedHeals == null ? 0 : attemptedHeals;

            // System.out.println(player.getName() + " attemptedKills: " + attemptedKills + ", attemptedHeals: " + attemptedHeals);

            // only in classic
            if (attemptedKills >= 1 && attemptedHeals == 0) {
                player.setState(PlayerState.DEAD);
            } else {
                player.setState(PlayerState.ALIVE);
            }
        }

        for (var ctx : contexts) {
            ActionResult result = ctx.actionResult();

            if (ctx.cancelled()) {
                result.resultType(ResultType.FAILED);
                result.data().put("reason", "Action was cancelled");
                continue;
            }

            Player target = ctx.target();

            switch (ctx.ability().getAction()) {
                case KILL -> {
                    if (target.getState() == PlayerState.DEAD) {
                        result.resultType(ResultType.SUCCESS);
                    } else {
                        result.resultType(ResultType.FAILED);
                        result.data().put("reason", "Target survived");
                    }
                }

                case HEAL -> {
                    if (target.getState() == PlayerState.ALIVE) {
                        result.resultType(ResultType.SUCCESS);
                    } else {
                        result.resultType(ResultType.FAILED);
                    }
                }

                case INVESTIGATE -> {
                    // data already filled by RuleEngine
                    result.resultType(ResultType.INFO);
                }
            }
        }

    }
}

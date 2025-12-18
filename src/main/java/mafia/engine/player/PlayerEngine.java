package mafia.engine.player;

import java.util.List;

import mafia.engine.ability.AbilityEngine;
import mafia.engine.game.event.GameEvent;
import mafia.engine.player.action.PlayerActionContext;
import mafia.engine.player.action.PlayerActionResult;
import mafia.engine.rule.RuleEngine;

// classic gameplay for now
public class PlayerEngine {

    private AbilityEngine abilityEngine = new AbilityEngine();
    private RuleEngine ruleEngine = new RuleEngine();

    public void updatePlayersState(List<PlayerActionContext> contexts, List<Player> players) {
        for (var ctx : contexts) {
            ruleEngine.process(GameEvent.BEFORE_ABILITY, ctx);

            if (ctx.cancelled()) {
                continue;
            }

            abilityEngine.registerAction(ctx);
            var result = new PlayerActionResult(ctx);

            ctx.actor().playerActionResults().add(result);
            ctx.playerActionResult(result);

            ruleEngine.process(GameEvent.AFTER_ABILITY, ctx);
        }

        for (var player : players) {
            var attemptedKills = player.attemptedActions().get(PlayerAction.KILL);
            var attemptedHeals = player.attemptedActions().get(PlayerAction.HEAL);

            attemptedKills = attemptedKills == null ? 0 : attemptedKills;
            attemptedHeals = attemptedHeals == null ? 0 : attemptedHeals;

            // System.out.println(player.getName() + " attemptedKills: " + attemptedKills + ", attemptedHeals: " + attemptedHeals);
            if (player.state() != PlayerState.ALIVE) {
                continue;
            }
            
            // only in classic
            if (attemptedKills >= 1 && attemptedHeals == 0) {
                player.state(PlayerState.KILLED);
            } else if (attemptedKills >= 1 && attemptedHeals >= 1 && attemptedKills <= attemptedHeals) {
                player.state(PlayerState.SAVED);
            } else {
                player.state(PlayerState.ALIVE);
            }
            player.attemptedActions().clear();
        }

        for (var ctx : contexts) {
            PlayerActionResult result = ctx.playerActionResult();

            if (ctx.cancelled()) {
                result.resultType(ResultType.FAILED);
                result.data().put("reason", "Action was cancelled");
                continue;
            }

            Player target = ctx.target();

            switch (ctx.ability().getAction()) {
                case KILL -> {
                    if (target.state() == PlayerState.KILLED) {
                        result.resultType(ResultType.SUCCESS);
                    } else {
                        result.resultType(ResultType.FAILED);
                        result.data().put("reason", "Target survived");
                    }
                }

                case HEAL -> {
                    result.resultType(switch (target.state()) {
                        case SAVED -> ResultType.SUCCESS;
                        case ALIVE, DEAD -> ResultType.NONE;
                        case KILLED -> ResultType.FAILED;
                        default -> throw new IllegalArgumentException("Unexpected value: " + target.state());
                    });
                }

                case INVESTIGATE -> {
                    // data already filled by RuleEngine
                    result.resultType(ResultType.INFO);
                }
            }
        }

    }
}

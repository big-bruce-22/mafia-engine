package mafia.engine.player;

import java.util.ArrayList;
import java.util.List;

import mafia.engine.ability.AbilityEngine;
import mafia.engine.core.GameConfiguration;
import mafia.engine.player.action.PlayerActionContext;
import mafia.engine.player.action.PlayerActionResult;
import mafia.engine.player.action.PlayerActionResultType;
import mafia.engine.rule.RuleEngine;

// classic gameplay for now
public class PlayerEngine {

    private AbilityEngine abilityEngine = new AbilityEngine();
    private RuleEngine ruleEngine = new RuleEngine();

    @SuppressWarnings("unchecked")
    public void updatePlayersState(List<PlayerActionContext> contexts, List<Player> players, GameConfiguration configuration) {
        for (var ctx : contexts) {
            ruleEngine.process("before", ctx);

            if (ctx.cancelled()) {
                continue;
            }

            var abilityName = ctx.ability().name();
            switch (abilityName.toLowerCase()) {
                case "nightkill", "daykill" -> {
                    var targetProperties = ctx.target().properties();
                    var actorProperties = ctx.actor().properties();
                    if (targetProperties.getProperty("killer") == null) {
                        targetProperties.addProperty("killer", new ArrayList<Player>());
                    }
                    ((List<Player>) targetProperties.getProperty("killer")).add(ctx.actor());
        
                    actorProperties.addProperty("killed", ctx.target());
                }
                case "takedown" -> 
                    ctx.target().properties().addProperty("takendown", true);
                default -> {}
            }            

            abilityEngine.registerAction(ctx);
            var result = new PlayerActionResult(ctx);

            ctx.actor().playerActionResults().add(result);
            ctx.playerActionResult(result);

            ruleEngine.process("after", ctx);
        }

        for (var player : players) {
            var attemptedKills = player.attemptedActions().get(PlayerAction.KILL);
            var attemptedTakeDowns = player.attemptedActions().get(PlayerAction.TAKEDOWN);
            var attemptedHeals = player.attemptedActions().get(PlayerAction.HEAL);

            attemptedKills = attemptedKills == null ? 0 : attemptedKills;
            attemptedHeals = attemptedHeals == null ? 0 : attemptedHeals;
            attemptedTakeDowns = attemptedTakeDowns == null ? 0 : attemptedTakeDowns;

            if (player.state() != PlayerState.ALIVE) {
                continue;
            }

            var isOverKill = configuration.getBooleanConfiguration(
                                "general", 
                                "overkillRule"
                            );

            var isSaved = isOverKill 
                            ? attemptedHeals == attemptedKills 
                            : attemptedHeals >= 1 && attemptedKills >= 1;
            
            var isKilled = isOverKill 
                            ? attemptedHeals != attemptedKills 
                            : attemptedHeals < 1 && attemptedKills >= 1;

            var isTakenDown = attemptedTakeDowns >= 1;
            
            if (isSaved) {
                player.state(PlayerState.SAVED);
            } else if (isKilled || isTakenDown) {
                player.state(PlayerState.KILLED);
                player.properties().addProperty("killed", true);
            } else {
                player.state(PlayerState.ALIVE);
            }
            
            player.attemptedActions().clear();
        }

        for (var ctx : contexts) {
            PlayerActionResult result = ctx.playerActionResult();

            if (ctx.cancelled()) {
                result.resultType(PlayerActionResultType.FAILED);
                result.data().put("reason", "Action was cancelled");
                continue;
            }

            Player target = ctx.target();

            switch (ctx.ability().getAction()) {
                case KILL -> {
                    if (target.state() == PlayerState.KILLED) {
                        result.resultType(PlayerActionResultType.SUCCESS);
                    } else {
                        result.resultType(PlayerActionResultType.FAILED);
                        result.data().put("reason", "Target survived");
                    }
                }

                case HEAL -> {
                    result.resultType(switch (target.state()) {
                        case SAVED -> PlayerActionResultType.SUCCESS;
                        case ALIVE, DEAD -> PlayerActionResultType.NONE;
                        case KILLED -> PlayerActionResultType.FAILED;
                        default -> throw new IllegalArgumentException("Unexpected value: " + target.state());
                    });
                }

                case INVESTIGATE -> {
                    // data already filled by RuleEngine
                    result.resultType(PlayerActionResultType.INFO);
                }
                case TAKEDOWN -> {
                    result.resultType(PlayerActionResultType.SUCCESS);
                }
                default -> throw new IllegalArgumentException("Unexpected value: " + ctx.ability().getAction());
            }
        }

    }
}

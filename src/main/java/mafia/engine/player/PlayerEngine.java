package mafia.engine.player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public void updatePlayerState(
        PlayerActionContext context,
        List<Player> players, 
        GameConfiguration configuration
    ) {
        var affectedPlayers = resolveAffectedPlayers(List.of(context));
        for (var player : affectedPlayers) {
            resolvePlayerState(player, configuration);
        }

        setContextResults(context);
    }

    public void updatePlayersState(
        List<PlayerActionContext> contexts,
        List<Player> players, 
        GameConfiguration configuration
    ) {
        var affectedPlayers = resolveAffectedPlayers(contexts);
        for (var player : affectedPlayers) {
            resolvePlayerState(player, configuration);
        }

        for (var ctx : contexts) {
            setContextResults(ctx);
        }
    }

    private void setContextResults(PlayerActionContext ctx) {
        PlayerActionResult result = ctx.playerActionResult();

        if (ctx.cancelled()) {
            result.resultType(PlayerActionResultType.FAILED);
            result.data().put("reason", "Action was cancelled");
            return;
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

    @SuppressWarnings("unchecked")
    private Set<Player> resolveAffectedPlayers(List<PlayerActionContext> contexts) {
        Set<Player> affectedPlayers = new HashSet<>();
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

            affectedPlayers.add(ctx.target());
        }
        
        return affectedPlayers;
    }

    private void resolvePlayerState(Player player, GameConfiguration config) {
        if (player.state() != PlayerState.ALIVE) return;

        int kills = player.attemptedActions().getOrDefault(PlayerAction.KILL, 0);
        int heals = player.attemptedActions().getOrDefault(PlayerAction.HEAL, 0);
        int takedowns = player.attemptedActions().getOrDefault(PlayerAction.TAKEDOWN, 0);

        boolean isOverKill = config.getBooleanConfiguration("general", "overkillRule");

        boolean isSaved = isOverKill ? kills == heals : kills >= 1 && heals >= 1;
        boolean isKilled = isOverKill ? kills != heals : kills >= 1 && heals < 1;
        boolean isTakenDown = takedowns >= 1;

        if (isSaved) {
            player.state(PlayerState.SAVED);
        } else if (isKilled || isTakenDown) {
            player.state(PlayerState.KILLED);
            player.properties().addProperty("killed", true);
        }

        player.attemptedActions().clear();
    }
}

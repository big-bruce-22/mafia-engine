package mafia.engine.vote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.experimental.Accessors;
import mafia.engine.core.GameConfiguration;
import mafia.engine.player.Player;
import mafia.engine.player.PlayerState;
import mafia.engine.util.StreamUtils;

@Accessors(fluent = true)
public class VoteResult {

    private final Map<Player, Integer> voteCount = new HashMap<>();
    private final Map<Player, List<Player>> playerVotes = new HashMap<>();

    @Getter
    private final Player target;

    @Getter
    private final List<Player> affectedByTarget = new ArrayList<>();

    @Getter
    private final String message;

    public VoteResult(List<PlayerVote> votes, GameConfiguration configuration) {
        for (var vote : votes) {
            playerVotes.putIfAbsent(vote.target(), new ArrayList<>());
            playerVotes.get(vote.target()).add(vote.player());
        }

        playerVotes.entrySet().forEach(e -> voteCount.put(e.getKey(), e.getValue().size()));
        
        var result = voteCount.entrySet().stream()
                .collect(Collectors.groupingBy(
                    Entry::getValue,
                    Collectors.mapping(Entry::getKey, Collectors.toList())
                ))
                .entrySet().stream()
                .max(Entry.comparingByKey())
                .filter(e -> e.getValue().size() == 1)
                .map(e -> Map.entry(e.getValue().get(0), e.getKey()));

        target = result.map(Entry::getKey).orElse(null);
        
        if (target != null) {
            target.state(PlayerState.DEAD);

            if (target.secondaryRole() != null) {
                if (target.secondaryRole().getRoleName().equalsIgnoreCase("Soulmate")) {
                    var soulmate = ((Player) target.getProperties().getProperty("soulmate"));
                    soulmate.state(PlayerState.DEAD);
                    affectedByTarget.add(soulmate);
                }
            }
        }

        var isAnonymousVoting = configuration.getConfiguration(
                                    "general", 
                                    "anonymousVoting"
                                ).getBooleanValue();

        if (!isAnonymousVoting) {
            if (playerVotes.get(target) == null) {
                message = "";
                return;
            }

            var players = StreamUtils.mapAndCollect(
                            playerVotes.get(target), 
                            Player::name, 
                            Collectors.joining(", ")
                        );
            message = "(%s)".formatted(players);
        } else {
            message = null;
        }
    }    

    @Override
    public String toString() {
        return target == null ? "Tied. No one is excluded." : 
            "%s received %d vote/s. %s"
                .formatted(target.name(), voteCount.get(target), message == null ? "" : message);
    }    
}

package mafia.engine.player.action;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import mafia.engine.player.ResultType;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class PlayerActionResult {

    @NonNull @Getter
    private final PlayerActionContext context;

    @NonNull @Setter @Getter
    private ResultType resultType = ResultType.SUCCESS;

    @NonNull @Setter @Getter
    private Map<String, Object> data = new HashMap<>();

    public String toString() {
        return "%s did %s on %s with result %s and data %s".formatted(
            context.actor().name(),
            context.ability().getAction().name(),
            context.target().name(),
            resultType,
            data.toString()
        );
    }
}

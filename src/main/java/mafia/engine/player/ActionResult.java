package mafia.engine.player;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import mafia.engine.context.Context;

@Accessors(fluent = true)
public class ActionResult {

    @NonNull @Getter
    private final Context context;

    @NonNull @Setter @Getter
    private ResultType resultType;

    @NonNull @Setter @Getter
    private Map<String, Object> data;

    public ActionResult(Context context) {
        this.context = context;
        this.resultType = ResultType.SUCCESS;
        this.data = new HashMap<>();
    }

    public String toString() {
        return "%s did %s on %s with result %s and data %s".formatted(
            context.actor().getName(),
            context.ability().getAction().name(),
            context.target().getName(),
            resultType,
            data.toString()
        );
    }
}

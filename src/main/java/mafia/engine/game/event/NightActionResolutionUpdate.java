package mafia.engine.game.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class NightActionResolutionUpdate extends GameUpdate {

    @Getter
    private final List<NightEvent> resolvedEvents;
}

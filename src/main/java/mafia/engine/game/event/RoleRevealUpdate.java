package mafia.engine.game.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import mafia.engine.role.RoleReveal;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class RoleRevealUpdate extends GameUpdate {
    
    @Getter
    private final List<RoleReveal> reveals;
}

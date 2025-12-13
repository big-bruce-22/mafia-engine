package mafia.engine.role;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import mafia.engine.ability.Ability;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Role {

    @Getter
    @JsonProperty("role")
    private String roleName;

    @Getter
    private String alignment;

    @Getter
    private String roleDescription;

    @Getter
    private List<Ability> abilities;

    @Getter
    private String cardImagePath;

    @Getter
    private String tokenImagePath;

    public String toString() {
        return """
            role: %S
            alignment: %s
            description: %s
            abilities: [
                %s
            ]
            cardImagePath: %s
            tokenImagePath: %s
            """.formatted(
                roleName, alignment, roleDescription,
                abilities == null ? "" : abilities.stream().map(Ability::toString).collect(Collectors.joining("\n")),
                cardImagePath, tokenImagePath
            );
    }
}

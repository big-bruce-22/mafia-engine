package mafia.engine.role;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import mafia.engine.ability.Ability;
import mafia.engine.property.Properties;
import mafia.engine.property.PropertyHolder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Role implements PropertyHolder {

    @Getter
    @JsonProperty("role")
    protected String roleName;

    @Getter
    protected String alignment;

    @Getter
    protected String roleDescription;

    @Getter
    protected List<Ability> abilities;

    @Getter
    protected String cardImagePath;

    @Getter
    protected String tokenImagePath;

    protected Properties properties = new Properties("role");

    public void setRoleName(String roleName) {
        this.roleName = roleName;
        properties.addProperty("roleName", roleName);
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
        properties.addProperty("alignment", alignment);
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
        properties.addProperty("roleDescription", roleDescription);
    }

    public void setAbilities(List<Ability> abilities) {
        this.abilities = abilities;
        properties.addProperty("abilities", abilities);
    }

    public void setCardImagePath(String cardImagePath) {
        this.cardImagePath = cardImagePath;
        properties.addProperty("cardImagePath", cardImagePath);
    }

    public void setTokenImagePath(String tokenImagePath) {
        this.tokenImagePath = tokenImagePath;
        properties.addProperty("tokenImagePath", tokenImagePath);
    }

    @Override
    public String toString() {
        return """
            role name: %S
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

    @Override
    public Properties getProperties() {
        return properties;
    }
}

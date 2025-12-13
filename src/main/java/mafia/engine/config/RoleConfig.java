package mafia.engine.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mafia.engine.role.Role;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleConfig {

    @Getter @Setter
    private String version;

    @Getter @Setter
    private List<Role> roles;
}

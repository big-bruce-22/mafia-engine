package mafia.engine.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mafia.engine.presets.Preset;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresetsConfig {

    @Getter @Setter
    private String version;

    @Getter @Setter
    private List<Preset> presets;
}

package ragna.wf.orc.engine.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties("orc.feature")
@ConstructorBinding
@RequiredArgsConstructor
public class FeatureTogglesConfigProperties {
    private final Map<String, Boolean> toggles = new HashMap<>();

    Map<String, Boolean> getToggles() {
        return toggles;
    }

    public boolean isReplayEngineEnabled() {
        return toggles.getOrDefault("replay-enabled", true);
    }
}

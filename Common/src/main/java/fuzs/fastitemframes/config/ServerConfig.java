package fuzs.fastitemframes.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ServerConfig implements ConfigCore {
    @Config(description = {
            "Transform all vanilla item frame entities to blocks when loading chunks. Otherwise only newly placed ite frames will be blocks.",
            "When disabled; individual item frame entities can be converted manually by breaking and replacing them."
    })
    public boolean convertAllExistingItemFrames = false;
}

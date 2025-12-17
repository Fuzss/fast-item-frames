package fuzs.fastitemframes.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ServerConfig implements ConfigCore {
    @Config(description = {
            "Transform all vanilla item frame entities to blocks when loading chunks. Otherwise only newly placed ite frames will be blocks.",
            "When disabled; individual item frame entities can be converted manually by breaking and replacing them."
    })
    public boolean convertAllExistingItemFrames = false;
    @Config(description = {
            "Attempt interaction with the supporting block when clicking on an item frame before falling back to the item frame itself.",
            "Allows for opening attached containers such as chests and crafting tables without clicking on them directly."
    })
    public boolean passClicksToAttachedBlock = true;
}

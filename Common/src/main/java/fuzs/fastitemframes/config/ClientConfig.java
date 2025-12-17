package fuzs.fastitemframes.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ClientConfig implements ConfigCore {
    @Config(description = {
            "Prevents items from rendering slightly closer to the attached block in invisible item frames.",
            "Allows for custom item textures (CIT) to look much better."
    })
    public boolean disableItemOffsetWhenInvisible = false;
    @Config(description = "Hides the text label shown for names items.")
    public boolean disableNameTagRendering = false;
    @Config(description = "Increases the scale of rendered items in invisible item frames.")
    public boolean largeItemsWhenInvisible = true;
}

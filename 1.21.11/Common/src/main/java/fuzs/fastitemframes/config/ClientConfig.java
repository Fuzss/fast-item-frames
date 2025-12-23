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
    @Config(description = {
            "Pick the scale of rendered items in invisible item frames.",
            "A scale value of 0.5 is equal to the default rendering of items in normal item frames."
    })
    @Config.DoubleRange(min = 0.0, max = 1.0)
    public double itemScaleWhenInvisible = 0.75;
}

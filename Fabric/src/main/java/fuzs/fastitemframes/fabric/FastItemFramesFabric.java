package fuzs.fastitemframes.fabric;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;

public class FastItemFramesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(FastItemFrames.MOD_ID, FastItemFrames::new);
    }
}

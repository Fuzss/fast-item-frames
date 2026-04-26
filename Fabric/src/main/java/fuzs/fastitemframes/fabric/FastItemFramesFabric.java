package fuzs.fastitemframes.fabric;

import fuzs.fastitemframes.common.FastItemFrames;
import fuzs.puzzleslib.common.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;

public class FastItemFramesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(FastItemFrames.MOD_ID, FastItemFrames::new);
    }
}

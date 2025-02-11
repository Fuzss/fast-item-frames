package fuzs.fastitemframes.neoforge;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.data.ModBlockLootProvider;
import fuzs.fastitemframes.data.tags.ModBlockTagProvider;
import fuzs.fastitemframes.data.tags.ModItemTagProvider;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.fml.common.Mod;

@Mod(FastItemFrames.MOD_ID)
public class FastItemFramesNeoForge {

    public FastItemFramesNeoForge() {
        ModConstructor.construct(FastItemFrames.MOD_ID, FastItemFrames::new);
        DataProviderHelper.registerDataProviders(FastItemFrames.MOD_ID,
                ModBlockLootProvider::new,
                ModBlockTagProvider::new,
                ModItemTagProvider::new);
    }
}

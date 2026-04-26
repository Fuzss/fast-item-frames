package fuzs.fastitemframes.neoforge;

import fuzs.fastitemframes.common.FastItemFrames;
import fuzs.fastitemframes.common.data.ModRecipeProvider;
import fuzs.fastitemframes.common.data.loot.ModBlockLootProvider;
import fuzs.fastitemframes.common.data.tags.ModBlockTagProvider;
import fuzs.fastitemframes.common.data.tags.ModEntityTypeTagProvider;
import fuzs.fastitemframes.common.data.tags.ModItemTagProvider;
import fuzs.puzzleslib.common.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.fml.common.Mod;

@Mod(FastItemFrames.MOD_ID)
public class FastItemFramesNeoForge {

    public FastItemFramesNeoForge() {
        ModConstructor.construct(FastItemFrames.MOD_ID, FastItemFrames::new);
        DataProviderHelper.registerDataProviders(FastItemFrames.MOD_ID,
                ModBlockLootProvider::new,
                ModBlockTagProvider::new,
                ModItemTagProvider::new,
                ModEntityTypeTagProvider::new,
                ModRecipeProvider::new);
    }
}

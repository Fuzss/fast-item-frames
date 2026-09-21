package fuzs.fastitemframes.neoforge;

import fuzs.fastitemframes.common.FastItemFrames;
import fuzs.fastitemframes.common.data.ModRecipeProvider;
import fuzs.fastitemframes.common.data.loot.ModBlockLootProvider;
import fuzs.fastitemframes.common.data.tags.ModBlockTagsProvider;
import fuzs.fastitemframes.common.data.tags.ModEntityTypeTagsProvider;
import fuzs.fastitemframes.common.data.tags.ModItemTagsProvider;
import fuzs.puzzleslib.common.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v3.core.DataProviderBuilder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.fml.common.Mod;

@Mod(FastItemFrames.MOD_ID)
public class FastItemFramesNeoForge {

    public FastItemFramesNeoForge() {
        ModConstructor.construct(FastItemFrames.MOD_ID, FastItemFrames::new);
        DataProviderBuilder.of(FastItemFrames.MOD_ID)
                .addLootProvider(ModBlockLootProvider::new, LootContextParamSets.BLOCK)
                .addProvider(ModBlockTagsProvider::new, ModItemTagsProvider::new, ModEntityTypeTagsProvider::new)
                .addRecipeProvider(ModRecipeProvider::new);
    }
}

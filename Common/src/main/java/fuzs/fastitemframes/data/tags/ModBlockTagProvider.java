package fuzs.fastitemframes.data.tags;

import fuzs.fastitemframes.init.ModRegistry;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;

public class ModBlockTagProvider extends AbstractTagProvider<Block> {

    public ModBlockTagProvider(DataProviderContext context) {
        super(Registries.BLOCK, context);
    }

    @Override
    public void addTags(HolderLookup.Provider registries) {
        this.add(BlockTags.MINEABLE_WITH_AXE)
                .add(ModRegistry.ITEM_FRAME_BLOCK.value(), ModRegistry.GLOW_ITEM_FRAME_BLOCK.value());
        this.add(ModRegistry.ITEM_FRAMES_BLOCK_TAG)
                .add(ModRegistry.ITEM_FRAME_BLOCK.value(), ModRegistry.GLOW_ITEM_FRAME_BLOCK.value());
    }
}

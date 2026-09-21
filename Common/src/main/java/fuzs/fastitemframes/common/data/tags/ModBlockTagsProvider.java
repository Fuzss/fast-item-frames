package fuzs.fastitemframes.common.data.tags;

import fuzs.fastitemframes.common.init.ModRegistry;
import fuzs.puzzleslib.common.api.data.v3.core.DataProviderContext;
import fuzs.puzzleslib.common.api.data.v3.tags.AbstractTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;

public class ModBlockTagsProvider extends AbstractTagsProvider<Block> {

    public ModBlockTagsProvider(DataProviderContext context) {
        super(Registries.BLOCK, context);
    }

    @Override
    public void addTags(HolderLookup.Provider registries) {
        this.tag(BlockTags.MINEABLE_WITH_AXE).add(ModRegistry.ITEM_FRAME_BLOCK, ModRegistry.GLOW_ITEM_FRAME_BLOCK);
        this.tag(ModRegistry.ITEM_FRAMES_BLOCK_TAG)
                .add(ModRegistry.ITEM_FRAME_BLOCK, ModRegistry.GLOW_ITEM_FRAME_BLOCK);
    }
}

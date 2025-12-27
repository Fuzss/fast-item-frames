package fuzs.fastitemframes.config;

import fuzs.fastitemframes.init.ModRegistry;
import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import fuzs.puzzleslib.api.config.v3.serialization.ConfigDataSet;
import fuzs.puzzleslib.api.config.v3.serialization.KeyedValueProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

import java.util.List;

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
    // TODO move these to a separate mod project
    @Config(description = {
            "Attempt interaction with the supporting block when clicking on an attached block before falling back to the originally clicked block itself.",
            "Allows for opening attached containers such as chests and crafting tables without clicking on them directly."
    })
    public boolean passClicksToAttachedBlock = true;
    @Config(description = {
            "Support passing clicks through attached blocks only when interacting with an empty hand.",
            "This generally allows the attached block to still be used, like applying dyes and ink to signs."
    })
    public boolean requiresEmptyHand = true;
    @Config(name = "passes_block_clicks_through",
            description = "The blocks that are permitted to pass clicks to the block they attach to.")
    List<String> passesBlockClicksThroughRaw = KeyedValueProvider.tagAppender(Registries.BLOCK)
            .addTag(ModRegistry.PASSES_CLICKS_THROUGH_BLOCK_TAG)
            .addTag(BlockTags.WALL_SIGNS, BlockTags.BANNERS)
            .addTag(ModRegistry.ITEM_FRAMES_BLOCK_TAG)
            .asStringList();
    @Config(name = "passes_entity_clicks_through",
            description = "The entities that are permitted to pass clicks to the block they attach to.")
    List<String> passesEntityClicksThroughRaw = KeyedValueProvider.tagAppender(Registries.ENTITY_TYPE)
            .addTag(ModRegistry.PASSES_CLICKS_THROUGH_ENTITY_TYPE_TAG)
            .addTag(ModRegistry.ITEM_FRAMES_ENTITY_TYPE_TAG)
            .asStringList();

    public ConfigDataSet<Block> passesBlockClicksThrough = ConfigDataSet.from(Registries.BLOCK);
    public ConfigDataSet<EntityType<?>> passesEntityClicksThrough = ConfigDataSet.from(Registries.ENTITY_TYPE);

    @Override
    public void afterConfigReload() {
        this.passesBlockClicksThrough = ConfigDataSet.from(Registries.BLOCK, this.passesBlockClicksThroughRaw);
        this.passesEntityClicksThrough = ConfigDataSet.from(Registries.ENTITY_TYPE, this.passesEntityClicksThroughRaw);
    }
}

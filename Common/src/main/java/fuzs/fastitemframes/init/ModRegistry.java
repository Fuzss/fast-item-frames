package fuzs.fastitemframes.init;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.world.item.ItemFrameItem;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.block.v1.MutableSoundType;
import fuzs.puzzleslib.api.init.v3.registry.RegistryManager;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class ModRegistry {
    public static final SoundType ITEM_FRAME_SOUND_TYPE = MutableSoundType.copyOf(SoundType.WOOD)
            .setBreakSound(SoundEvents.ITEM_FRAME_BREAK)
            .setPlaceSound(SoundEvents.ITEM_FRAME_PLACE);
    public static final SoundType GLOW_ITEM_FRAME_SOUND_TYPE = MutableSoundType.copyOf(SoundType.WOOD)
            .setBreakSound(SoundEvents.GLOW_ITEM_FRAME_BREAK)
            .setPlaceSound(SoundEvents.GLOW_ITEM_FRAME_PLACE);
    static final RegistryManager REGISTRY = RegistryManager.from(FastItemFrames.MOD_ID);
    public static final Holder.Reference<Block> ITEM_FRAME_BLOCK = REGISTRY.registerBlock("item_frame",
            () -> new ItemFrameBlock(EntityType.ITEM_FRAME,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.SAND)
                            .forceSolidOn()
                            .instrument(NoteBlockInstrument.BASS)
                            .noCollission()
                            .strength(1.0F)
                            .ignitedByLava()
                            .instabreak()
                            .pushReaction(PushReaction.DESTROY)
                            .sound(ITEM_FRAME_SOUND_TYPE)
            )
    );
    public static final Holder.Reference<Item> ITEM_FRAME_ITEM = REGISTRY.registerItem("item_frame",
            () -> new ItemFrameItem(ITEM_FRAME_BLOCK.value(), new Item.Properties())
    );
    public static final Holder.Reference<BlockEntityType<ItemFrameBlockEntity>> ITEM_FRAME_BLOCK_ENTITY = REGISTRY.registerBlockEntityType(
            "item_frame",
            () -> BlockEntityType.Builder.of(ItemFrameBlockEntity::new, ITEM_FRAME_BLOCK.value())
    );

    public static void touch() {

    }
}

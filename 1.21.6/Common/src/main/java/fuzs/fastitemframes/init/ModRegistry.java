package fuzs.fastitemframes.init;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.attachment.v4.DataAttachmentRegistry;
import fuzs.puzzleslib.api.attachment.v4.DataAttachmentType;
import fuzs.puzzleslib.api.block.v1.MutableSoundType;
import fuzs.puzzleslib.api.init.v3.registry.RegistryManager;
import fuzs.puzzleslib.api.init.v3.tags.TagFactory;
import fuzs.puzzleslib.api.network.v4.PlayerSet;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.Set;

public class ModRegistry {
    public static final SoundType ITEM_FRAME_SOUND_TYPE = MutableSoundType.copyOf(SoundType.WOOD)
            .setBreakSound(SoundEvents.ITEM_FRAME_BREAK)
            .setPlaceSound(SoundEvents.ITEM_FRAME_PLACE);
    public static final SoundType GLOW_ITEM_FRAME_SOUND_TYPE = MutableSoundType.copyOf(SoundType.WOOD)
            .setBreakSound(SoundEvents.GLOW_ITEM_FRAME_BREAK)
            .setPlaceSound(SoundEvents.GLOW_ITEM_FRAME_PLACE);

    static final RegistryManager REGISTRIES = RegistryManager.from(FastItemFrames.MOD_ID);
    public static final Holder.Reference<Block> ITEM_FRAME_BLOCK = registerItemFrame("item_frame",
            Items.ITEM_FRAME,
            ITEM_FRAME_SOUND_TYPE);
    public static final Holder.Reference<Block> GLOW_ITEM_FRAME_BLOCK = registerItemFrame("glow_item_frame",
            Items.GLOW_ITEM_FRAME,
            GLOW_ITEM_FRAME_SOUND_TYPE);
    public static final Holder.Reference<BlockEntityType<ItemFrameBlockEntity>> ITEM_FRAME_BLOCK_ENTITY = REGISTRIES.registerBlockEntityType(
            "item_frame",
            ItemFrameBlockEntity::new,
            () -> Set.of(ITEM_FRAME_BLOCK.value(), GLOW_ITEM_FRAME_BLOCK.value()));

    static final TagFactory TAGS = TagFactory.make(FastItemFrames.MOD_ID);
    public static final TagKey<Block> ITEM_FRAMES_BLOCK_TAG = TAGS.registerBlockTag("item_frames");
    public static final TagKey<EntityType<?>> ITEM_FRAMES_ENTITY_TYPE_TAG = TAGS.registerEntityTypeTag("item_frames");

    public static final DataAttachmentType<Entity, DyedItemColor> ITEM_FRAME_COLOR_ATTACHMENT_TYPE = DataAttachmentRegistry.<DyedItemColor>entityBuilder()
            .persistent(DyedItemColor.CODEC)
            .networkSynchronized(DyedItemColor.STREAM_CODEC, PlayerSet::nearEntity)
            .build(FastItemFrames.id("item_frame_color"));

    public static void bootstrap() {
        // NO-OP
    }

    private static Holder.Reference<Block> registerItemFrame(String path, Item item, SoundType soundType) {
        return REGISTRIES.registerBlock(path,
                (BlockBehaviour.Properties properties) -> new ItemFrameBlock(item, properties),
                () -> BlockBehaviour.Properties.of()
                        .mapColor(MapColor.SAND)
                        .forceSolidOn()
                        .instrument(NoteBlockInstrument.BASS)
                        .noCollission()
                        .strength(1.0F)
                        .ignitedByLava()
                        .instabreak()
                        .pushReaction(PushReaction.DESTROY)
                        .sound(soundType)
                        .overrideDescription(item.getDescriptionId()));
    }
}

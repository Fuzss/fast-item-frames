package fuzs.fastitemframes.world.level.block.entity;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public class ItemFrameBlockEntity extends BlockEntity {
    static final String TAG_ITEM_FRAME = FastItemFrames.id("item_frame").toString();
    static final String TAG_ENTITY_TYPE = FastItemFrames.id("entity_type").toString();

    @Nullable
    private ItemFrame itemFrame;
    @Nullable
    private CompoundTag storedTag;
    @Nullable
    private Integer color;

    public ItemFrameBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegistry.ITEM_FRAME_BLOCK_ENTITY.value(), pos, blockState);
    }

    public void load(ItemFrame itemFrame, EntityType<?> entityType) {
        CompoundTag compoundTag = new CompoundTag();
        itemFrame.addAdditionalSaveData(compoundTag);
        this.loadItemFrame(compoundTag, entityType);
        ModRegistry.ITEM_FRAME_COLOR_CAPABILITY.get(itemFrame).getColor().ifPresent(this::setColor);
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains(TAG_ENTITY_TYPE, Tag.TAG_STRING)) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.byNameCodec()
                    .parse(NbtOps.INSTANCE, tag.get(TAG_ENTITY_TYPE))
                    .resultOrPartial(FastItemFrames.LOGGER::error)
                    .orElse(EntityType.ITEM_FRAME);
            this.loadItemFrame(tag.getCompound(TAG_ITEM_FRAME), entityType);
        }
        this.color =
                tag.contains(DyeableLeatherItem.TAG_COLOR, Tag.TAG_INT) ? tag.getInt(DyeableLeatherItem.TAG_COLOR) :
                        null;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        // save the entity type, with ModernFix installed this for some reason cannot be retrieved from the block state during loading
        BuiltInRegistries.ENTITY_TYPE.byNameCodec()
                .encodeStart(NbtOps.INSTANCE, this.getEntityType())
                .resultOrPartial(FastItemFrames.LOGGER::error)
                .ifPresent((Tag tag) -> compoundTag.put(TAG_ENTITY_TYPE, tag));
        CompoundTag itemFrameTag = this.getItemFrameTag();
        if (itemFrameTag != null) {
            compoundTag.put(TAG_ITEM_FRAME, itemFrameTag);
        }
        if (this.color != null) {
            compoundTag.putInt(DyeableLeatherItem.TAG_COLOR, this.color);
        }
    }

    @Nullable
    private CompoundTag getItemFrameTag() {
        ItemFrame itemFrame = this.getEntityRepresentation();
        if (itemFrame != null) {
            CompoundTag compoundTag = new CompoundTag();
            itemFrame.addAdditionalSaveData(compoundTag);
            return compoundTag;
        } else if (this.storedTag != null) {
            return this.storedTag;
        }

        return null;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void setColor(int color) {
        if (this.color == null || this.color != color) {
            this.color = color;
        }
    }

    public OptionalInt getColor() {
        return this.color != null ? OptionalInt.of(this.color) : OptionalInt.empty();
    }

    public ItemStack getItem() {
        ItemFrame itemFrame = this.getEntityRepresentation();
        return itemFrame != null ? itemFrame.getItem() : ItemStack.EMPTY;
    }

    public boolean isInvisible() {
        return this.getBlockState().getValue(ItemFrameBlock.INVISIBLE);
    }

    public void markUpdated() {
        if (this.hasLevel()) {
            this.getLevel().setBlock(this.getBlockPos(), this.getUpdatedBlockState(), 2);
            this.setChanged();
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    private BlockState getUpdatedBlockState() {

        BlockState blockState = this.getBlockState();
        ItemFrame itemFrame = this.getEntityRepresentation();
        if (itemFrame != null) {

            if (itemFrame.getItem().isEmpty() && blockState.getValue(ItemFrameBlock.INVISIBLE)) {
                blockState = blockState.setValue(ItemFrameBlock.INVISIBLE, Boolean.FALSE);
            }

            return blockState.setValue(ItemFrameBlock.HAS_MAP, itemFrame.hasFramedMap())
                    .setValue(ItemFrameBlock.DYED, this.getColor().isPresent());
        }

        return blockState;
    }

    @Nullable
    public ItemFrame getEntityRepresentation() {
        return this.getEntityRepresentation(false, this.getEntityType());
    }

    private EntityType<?> getEntityType() {
        ItemFrame itemFrame = this.itemFrame;
        if (itemFrame != null) {
            return itemFrame.getType();
        } else {
            Item item = this.getBlockState().getBlock().asItem();
            if (item instanceof HangingEntityItem hangingEntityItem) {
                return hangingEntityItem.type;
            } else {
                return EntityType.ITEM_FRAME;
            }
        }
    }

    @Nullable
    public ItemFrame getEntityRepresentation(boolean skipInit, EntityType<?> entityType) {
        if (this.itemFrame == null && this.hasLevel()) {
            ItemFrame itemFrame = (ItemFrame) entityType.create(this.getLevel());
            if (!skipInit) this.initItemFrame(itemFrame, this.storedTag);
            this.storedTag = null;
            return this.itemFrame = itemFrame;
        } else {
            return this.itemFrame;
        }
    }

    private void loadItemFrame(CompoundTag compoundTag, EntityType<?> entityType) {
        ItemFrame itemFrame = this.getEntityRepresentation(true, entityType);
        if (itemFrame != null) {
            this.initItemFrame(itemFrame, compoundTag);
        } else {
            this.storedTag = compoundTag.copy();
        }
    }

    private void initItemFrame(ItemFrame itemFrame, @Nullable CompoundTag compoundTag) {
        itemFrame.setItem(ItemStack.EMPTY, false);
        BlockPos pos = this.getBlockPos();
        // set this first to prevent a warning when reading the wrong position from nbt
        itemFrame.setPos(pos.getX(), pos.getY(), pos.getZ());
        if (compoundTag != null) itemFrame.readAdditionalSaveData(compoundTag);
        // set this again in case the nbt contained a wrong position
        itemFrame.setPos(pos.getX(), pos.getY(), pos.getZ());
        // force block facing e.g. when the item frame has been copied with nbt
        // ArchLoom seems to be unable to remap overridden methods in the access transformer file, so we have to defer to the super class
        ((HangingEntity) itemFrame).setDirection(this.getBlockState().getValue(ItemFrameBlock.FACING));
        // just make those always invisible for client rendering, the actual block invisibility status is tracked via a block state
        itemFrame.setInvisible(true);
    }
}

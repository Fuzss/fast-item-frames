package fuzs.fastitemframes.world.level.block.entity;

import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public class ItemFrameBlockEntity extends BlockEntity {
    @Nullable
    private ItemFrame itemFrame;
    @Nullable
    private CompoundTag storedTag;
    @Nullable
    private Integer color;

    public ItemFrameBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegistry.ITEM_FRAME_BLOCK_ENTITY.value(), pos, blockState);
    }

    @Override
    public void load(CompoundTag tag) {
        ItemFrame itemFrame = this.getEntityRepresentation(true);
        if (itemFrame != null) {
            this.initItemFrame(itemFrame, tag);
        } else {
            this.storedTag = tag.copy();
        }
        this.color = tag.contains(DyeableLeatherItem.TAG_COLOR, Tag.TAG_INT) ?
                tag.getInt(DyeableLeatherItem.TAG_COLOR) :
                null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        ItemFrame itemFrame = this.getEntityRepresentation();
        if (itemFrame != null) {
            itemFrame.addAdditionalSaveData(tag);
        } else if (this.storedTag != null) {
            for (String key : this.storedTag.getAllKeys()) {
                tag.put(key, this.storedTag.get(key));
            }
        }
        if (this.color != null) {
            tag.putInt(DyeableLeatherItem.TAG_COLOR, this.color);
        }
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
            this.setChanged();
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

    @Override
    public void setChanged() {

        if (this.hasLevel()) {

            ItemFrame itemFrame = this.getEntityRepresentation();
            if (itemFrame != null) {
                if (this.getBlockState().getValue(ItemFrameBlock.HAS_MAP) != itemFrame.hasFramedMap()) {
                    BlockState blockState = this.getBlockState()
                            .setValue(ItemFrameBlock.HAS_MAP, itemFrame.hasFramedMap());
                    this.getLevel().setBlock(this.getBlockPos(), blockState, 2);
                }
                if (this.getBlockState().getValue(ItemFrameBlock.DYED) != this.getColor().isPresent()) {
                    BlockState blockState = this.getBlockState()
                            .setValue(ItemFrameBlock.DYED, this.getColor().isPresent());
                    this.getLevel().setBlock(this.getBlockPos(), blockState, 2);
                }
                if (itemFrame.getItem().isEmpty()) {
                    itemFrame.setRotation(0);
                    if (this.getBlockState().getValue(ItemFrameBlock.INVISIBLE)) {
                        BlockState blockState = this.getBlockState().setValue(ItemFrameBlock.INVISIBLE, false);
                        this.getLevel().setBlock(this.getBlockPos(), blockState, 2);
                    }
                }
            }

            super.setChanged();

            BlockState newBlockState = this.getLevel().getBlockState(this.getBlockPos());
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), newBlockState, 3);
        }
    }

    @Nullable
    public ItemFrame getEntityRepresentation() {
        return this.getEntityRepresentation(false);
    }

    @Nullable
    public ItemFrame getEntityRepresentation(boolean skipInit) {
        if (this.itemFrame == null && this.hasLevel()) {

            EntityType<? extends HangingEntity> type = ((HangingEntityItem) this.getBlockState()
                    .getBlock()
                    .asItem()).type;

            ItemFrame itemFrame = (ItemFrame) type.create(this.getLevel());
            if (!skipInit) this.initItemFrame(itemFrame, this.storedTag);
            this.storedTag = null;
            return this.itemFrame = itemFrame;
        } else {
            return this.itemFrame;
        }
    }

    void initItemFrame(ItemFrame itemFrame, @Nullable CompoundTag compoundTag) {
        itemFrame.setItem(ItemStack.EMPTY, false);
        BlockPos pos = this.getBlockPos();
        // set this first to prevent a warning when reading the wrong position from nbt
        itemFrame.setPos(pos.getX(), pos.getY(), pos.getZ());
        if (compoundTag != null) itemFrame.readAdditionalSaveData(compoundTag);
        // set this again in case the nbt contained a wrong position
        itemFrame.setPos(pos.getX(), pos.getY(), pos.getZ());
        // force block facing e.g. when the item frame has been copied with nbt
        itemFrame.setDirection(this.getBlockState().getValue(ItemFrameBlock.FACING));
        // just make those always invisible for client rendering, the actual block invisibility status is tracked via a block state
        itemFrame.setInvisible(true);
    }
}

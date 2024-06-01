package fuzs.fastitemframes.world.level.block.entity;

import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class ItemFrameBlockEntity extends BlockEntity {
    @Nullable
    private ItemFrame itemFrame;
    @Nullable
    private CompoundTag deferredSaveData;
    private int color = 10511680;

    public ItemFrameBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegistry.ITEM_FRAME_BLOCK_ENTITY.value(), pos, blockState);
    }

    @Override
    public void load(CompoundTag tag) {
        ItemFrame itemFrame = this.getEntityRepresentation();
        if (itemFrame != null) {
            itemFrame.setItem(ItemStack.EMPTY, false);
            itemFrame.readAdditionalSaveData(tag);
        } else {
            this.deferredSaveData = tag.copy();
        }
        this.color = tag.contains("color", Tag.TAG_INT) ? tag.getInt("color") : 10511680;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        ItemFrame itemFrame = this.getEntityRepresentation();
        if (itemFrame != null) {
            itemFrame.addAdditionalSaveData(tag);
        } else if (this.deferredSaveData != null) {
            for (String key : this.deferredSaveData.getAllKeys()) {
                tag.put(key, this.deferredSaveData.get(key));
            }
        }
        tag.putInt("color", this.color);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public int getColor() {
        return this.color;
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

        ItemFrame itemFrame = this.getEntityRepresentation();
        if (this.hasLevel() && itemFrame != null) {
            if (this.getBlockState().getValue(ItemFrameBlock.HAS_MAP) != itemFrame.hasFramedMap()) {
                BlockState blockState = this.getBlockState().setValue(ItemFrameBlock.HAS_MAP, itemFrame.hasFramedMap());
                this.getLevel().setBlock(this.getBlockPos(), blockState, 2);
            }

            // TODO probably remove this for parity with the entity
            if (itemFrame.getItem().isEmpty() && itemFrame.getRotation() != 0) {
                itemFrame.setRotation(0);
            }
        }

        super.setChanged();

        BlockState newBlockState = this.getLevel().getBlockState(this.getBlockPos());
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), newBlockState, 3);
    }

    @Nullable
    public ItemFrame getEntityRepresentation() {
        if (this.itemFrame == null && this.hasLevel()) {
            // TODO support glow frames
            ItemFrame itemFrame = new ItemFrame(this.getLevel(),
                    this.getBlockPos(),
                    this.getBlockState().getValue(ItemFrameBlock.FACING)
            );
            if (this.deferredSaveData != null) {
                itemFrame.readAdditionalSaveData(this.deferredSaveData);
                this.deferredSaveData = null;
                BlockPos pos = this.getBlockPos();
                itemFrame.setPos(pos.getX(), pos.getY(), pos.getZ());
                try {
                    // access widener
                    Method setDirection = ItemFrame.class.getDeclaredMethod("setDirection", Direction.class);
                    setDirection.setAccessible(true);
                    MethodHandles.lookup()
                            .unreflect(setDirection)
                            .invoke(itemFrame, this.getBlockState().getValue(ItemFrameBlock.FACING));
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
            // just make those always invisible for client rendering, the actual block invisibility status is tracked via a block state
            itemFrame.setInvisible(true);
            return this.itemFrame = itemFrame;
        } else {
            return this.itemFrame;
        }
    }
}

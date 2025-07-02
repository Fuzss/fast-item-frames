package fuzs.fastitemframes.world.level.block.entity;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import fuzs.puzzleslib.api.block.v1.entity.TickingBlockEntity;
import fuzs.puzzleslib.api.util.v1.ValueSerializationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class ItemFrameBlockEntity extends BlockEntity implements TickingBlockEntity {
    static final String TAG_COLOR = FastItemFrames.id("color").toString();
    static final String TAG_ITEM_FRAME = FastItemFrames.id("item_frame").toString();

    @Nullable
    private ItemFrame itemFrame;
    @Nullable
    private CompoundTag storedTag;
    @Nullable
    private DyedItemColor color;

    public ItemFrameBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegistry.ITEM_FRAME_BLOCK_ENTITY.value(), pos, blockState);
    }

    /**
     * @see ServerEntity#sendChanges()
     */
    @Override
    public void serverTick() {
        if (this.hasLevel() && ((ServerLevel) this.getLevel()).getServer().getTickCount() % 10 == 0) {
            ItemStack itemStack = this.getItem();
            if (itemStack.getItem() instanceof MapItem) {
                MapId mapId = itemStack.get(DataComponents.MAP_ID);
                MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapId, this.getLevel());
                if (mapItemSavedData != null) {
                    for (ServerPlayer serverPlayer : ((ServerLevel) this.getLevel()).players()) {
                        mapItemSavedData.tickCarriedBy(serverPlayer, itemStack);
                        Packet<?> packet = mapItemSavedData.getUpdatePacket(mapId, serverPlayer);
                        if (packet != null) {
                            serverPlayer.connection.send(packet);
                        }
                    }
                }
            }
        }
    }

    public void load(ItemFrame itemFrame) {
        CompoundTag compoundTag = ValueSerializationHelper.save(this.problemPath(), itemFrame::saveWithoutId);
        this.loadItemFrame(compoundTag);
        this.color = ModRegistry.ITEM_FRAME_COLOR_ATTACHMENT_TYPE.get(itemFrame);
    }

    @Override
    public void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        valueInput.read(TAG_ITEM_FRAME, CompoundTag.CODEC).ifPresent(this::loadItemFrame);
        this.color = valueInput.read(TAG_COLOR, DyedItemColor.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.storeNullable(TAG_ITEM_FRAME, CompoundTag.CODEC, this.getItemFrameTag());
        valueOutput.storeNullable(TAG_COLOR, DyedItemColor.CODEC, this.color);
    }

    @Nullable
    private CompoundTag getItemFrameTag() {
        ItemFrame itemFrame = this.getEntityRepresentation();
        if (itemFrame != null) {
            return ValueSerializationHelper.save(this.problemPath(), itemFrame::saveWithoutId);
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
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public void preRemoveSideEffects(BlockPos blockPos, BlockState blockState) {
        super.preRemoveSideEffects(blockPos, blockState);
        this.getEntityRepresentation().dropItem((ServerLevel) this.level, null, false);
        // not sure if this is necessary since the block entity is about to be deleted as well
        this.setChanged();
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        super.applyImplicitComponents(dataComponentGetter);
        this.color = dataComponentGetter.get(DataComponents.DYED_COLOR);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (this.color != null) {
            components.set(DataComponents.DYED_COLOR, this.color);
        }
    }

    @Override
    public void removeComponentsFromTag(ValueOutput valueOutput) {
        valueOutput.discard(TAG_COLOR);
    }

    @Nullable
    public DyedItemColor getColor() {
        return this.color;
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
            blockState = blockState.setValue(ItemFrameBlock.MAP, itemFrame.hasFramedMap())
                    .setValue(ItemFrameBlock.DYED, this.color != null);

            if (itemFrame.getItem().isEmpty() && blockState.getValue(ItemFrameBlock.INVISIBLE)) {
                return blockState.setValue(ItemFrameBlock.INVISIBLE, Boolean.FALSE);
            } else {
                return blockState;
            }
        }

        return blockState;
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
            ItemFrame itemFrame = (ItemFrame) type.create(this.getLevel(), EntitySpawnReason.LOAD);
            if (!skipInit) this.initItemFrame(itemFrame, this.storedTag);
            this.storedTag = null;
            return this.itemFrame = itemFrame;
        } else {
            return this.itemFrame;
        }
    }

    private void loadItemFrame(CompoundTag compoundTag) {
        ItemFrame itemFrame = this.getEntityRepresentation(true);
        if (itemFrame != null) {
            this.initItemFrame(itemFrame, compoundTag);
        } else {
            this.storedTag = compoundTag;
        }
    }

    private void initItemFrame(ItemFrame itemFrame, @Nullable CompoundTag compoundTag) {
        itemFrame.setItem(ItemStack.EMPTY, false);
        BlockPos pos = this.getBlockPos();
        // set this first to prevent a warning when reading the wrong position from nbt
        itemFrame.setPos(pos.getX(), pos.getY(), pos.getZ());
        if (compoundTag != null) {
            ValueSerializationHelper.load(this.problemPath(), itemFrame.registryAccess(), compoundTag, itemFrame::load);
        }
        // set this again in case the nbt contained a wrong position
        itemFrame.setPos(pos.getX(), pos.getY(), pos.getZ());
        // force block facing e.g. when the item frame has been copied with nbt
        itemFrame.setDirection(this.getBlockState().getValue(ItemFrameBlock.FACING));
        // just make those always invisible for client rendering, the actual block invisibility status is tracked via a block state
        itemFrame.setInvisible(true);
    }
}

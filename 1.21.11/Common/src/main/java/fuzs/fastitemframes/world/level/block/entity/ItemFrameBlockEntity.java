package fuzs.fastitemframes.world.level.block.entity;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import fuzs.puzzleslib.api.block.v1.entity.TickingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ListBackedContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ItemFrameBlockEntity extends BlockEntity implements TickingBlockEntity, ListBackedContainer {
    public static final String TAG_COLOR = "color";
    public static final String TAG_ITEM_DROP_CHANCE = "item_drop_chance";
    public static final float DEFAULT_DROP_CHANCE = 1.0F;

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private float dropChance = DEFAULT_DROP_CHANCE;
    @Nullable
    private DyedItemColor color;
    @Nullable
    private ItemFrame itemFrame;
    private ItemStack lastItem = ItemStack.EMPTY;

    public ItemFrameBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegistry.ITEM_FRAME_BLOCK_ENTITY.value(), pos, blockState);
    }

    @Override
    public void serverTick() {
        if (this.getLevel() instanceof ServerLevel serverLevel) {
            ItemStack itemStack = this.getItem();
            if (!ItemStack.isSameItemSameComponents(this.lastItem, itemStack)) {
                this.setUpdatedBlockState(serverLevel, this.lastItem, itemStack);
                this.setItemStackEntityRepresentation(serverLevel, this.lastItem, itemStack);
                this.lastItem = itemStack.copy();
            }

            if (serverLevel.getServer().getTickCount() % 10 == 0) {
                this.tickMapItemSavedData(serverLevel, itemStack);
            }
        }
    }

    private void setUpdatedBlockState(ServerLevel serverLevel, ItemStack oldItemStack, ItemStack newItemStack) {
        BlockState blockState = this.getBlockState();
        if (newItemStack.isEmpty()) {
            blockState = blockState.setValue(ItemFrameBlock.INVISIBLE, Boolean.FALSE);
        }

        if (this.hasFramedMap(oldItemStack) != this.hasFramedMap(newItemStack)) {
            blockState = blockState.setValue(ItemFrameBlock.MAP, this.hasFramedMap(newItemStack));
        }

        if (blockState != this.getBlockState()) {
            serverLevel.setBlock(this.getBlockPos(), blockState, ItemFrameBlock.UPDATE_ALL);
        }
    }

    private void setItemStackEntityRepresentation(ServerLevel serverLevel, ItemStack oldItemStack, ItemStack newItemStack) {
        ItemFrame itemFrame = this.getItemFrameEntity(serverLevel);
        if (itemFrame != null) {
            itemFrame.getEntityData().set(ItemFrame.DATA_ITEM, newItemStack);
            if (!newItemStack.isEmpty()) {
                if (newItemStack.getFrame() != itemFrame) {
                    newItemStack.setEntityRepresentation(itemFrame);
                }
            }
        }

        if (!oldItemStack.isEmpty()) {
            this.removeFramedMap(oldItemStack);
        }
    }

    private @Nullable ItemFrame getItemFrameEntity(ServerLevel serverLevel) {
        if (this.itemFrame == null) {
            return this.itemFrame = this.createItemFrameEntity(serverLevel);
        } else {
            return this.itemFrame;
        }
    }

    private @Nullable ItemFrame createItemFrameEntity(ServerLevel serverLevel) {
        ItemFrame itemFrame = this.getEntityType().create(serverLevel, EntitySpawnReason.LOAD);
        if (itemFrame != null) {
            itemFrame.getEntityData().set(ItemFrame.DATA_ITEM, this.getItem());
            itemFrame.setPos(Vec3.atCenterOf(this.getBlockPos()));
            itemFrame.setDirection(this.getBlockState().getValue(ItemFrameBlock.FACING));
        }

        return itemFrame;
    }

    /**
     * @see ServerEntity#sendChanges()
     */
    private void tickMapItemSavedData(ServerLevel serverLevel, ItemStack itemStack) {
        if (itemStack.getItem() instanceof MapItem) {
            MapId mapId = itemStack.get(DataComponents.MAP_ID);
            MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapId, serverLevel);
            if (mapItemSavedData != null) {
                for (ServerPlayer serverPlayer : serverLevel.players()) {
                    mapItemSavedData.tickCarriedBy(serverPlayer, itemStack);
                    Packet<?> packet = mapItemSavedData.getUpdatePacket(mapId, serverPlayer);
                    if (packet != null) {
                        serverPlayer.connection.send(packet);
                    }
                }
            }
        }
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    public EntityType<? extends ItemFrame> getEntityType() {
        return (EntityType<? extends ItemFrame>) ((HangingEntityItem) this.getBlockState().getBlock().asItem()).type;
    }

    public ItemStack getItem() {
        return this.getItem(0);
    }

    /**
     * @see ItemFrame#removeFramedMap(ItemStack)
     */
    private void removeFramedMap(ItemStack itemStack) {
        MapId mapId = this.getFramedMapId(itemStack);
        if (mapId != null && this.itemFrame != null) {
            MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapId, this.getLevel());
            if (mapItemSavedData != null) {
                mapItemSavedData.removedFromFrame(this.getBlockPos(), this.itemFrame.getId());
            }
        }

        itemStack.setEntityRepresentation(null);
    }

    /**
     * @see ItemFrame#getFramedMapId(ItemStack)
     */
    public @Nullable MapId getFramedMapId(ItemStack itemStack) {
        return itemStack.get(DataComponents.MAP_ID);
    }

    /**
     * @see ItemFrame#hasFramedMap()
     */
    public boolean hasFramedMap(ItemStack itemStack) {
        return itemStack.has(DataComponents.MAP_ID);
    }

    public float getDropChance() {
        return this.dropChance;
    }

    public void setDropChance(float dropChance) {
        this.dropChance = dropChance;
    }

    @Nullable
    public DyedItemColor getColor() {
        return this.color;
    }

    public void setColor(@Nullable DyedItemColor color) {
        this.color = color;
    }

    /**
     * @see ItemFrame#getRemoveItemSound()
     */
    public SoundEvent getRemoveItemSound() {
        return this.isGlowItemFrame() ? SoundEvents.GLOW_ITEM_FRAME_REMOVE_ITEM : SoundEvents.ITEM_FRAME_REMOVE_ITEM;
    }

    /**
     * @see ItemFrame#getAddItemSound()
     */
    public SoundEvent getAddItemSound() {
        return this.isGlowItemFrame() ? SoundEvents.GLOW_ITEM_FRAME_ADD_ITEM : SoundEvents.ITEM_FRAME_ADD_ITEM;
    }

    /**
     * @see ItemFrame#getRotateItemSound()
     */
    public SoundEvent getRotateItemSound() {
        return this.isGlowItemFrame() ? SoundEvents.GLOW_ITEM_FRAME_ROTATE_ITEM : SoundEvents.ITEM_FRAME_ROTATE_ITEM;
    }

    private boolean isGlowItemFrame() {
        return this.getEntityType() == EntityType.GLOW_ITEM_FRAME;
    }

    @Override
    public void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.items.clear();
        ContainerHelper.loadAllItems(valueInput, this.items);
        this.setDropChance(valueInput.getFloatOr(TAG_ITEM_DROP_CHANCE, DEFAULT_DROP_CHANCE));
        valueInput.read(TAG_COLOR, DyedItemColor.CODEC).ifPresent(this::setColor);
        this.loadLegacyAdditional(valueInput);
    }

    private void loadLegacyAdditional(ValueInput valueInput) {
        valueInput.childOrEmpty(FastItemFrames.id("item_frame").toString())
                .read("Item", ItemStack.CODEC)
                .ifPresent((ItemStack itemStack) -> {
                    this.setItem(0, itemStack);
                });
        valueInput.read(FastItemFrames.id("color").toString(), DyedItemColor.CODEC).ifPresent(this::setColor);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        ContainerHelper.saveAllItems(valueOutput, this.items, false);
        if (this.getDropChance() != DEFAULT_DROP_CHANCE) {
            valueOutput.putFloat(TAG_ITEM_DROP_CHANCE, this.getDropChance());
        }

        if (this.getColor() != null) {
            valueOutput.storeNullable(TAG_COLOR, DyedItemColor.CODEC, this.getColor());
        }
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
    public void setChanged() {
        super.setChanged();
        if (!ItemStack.isSameItemSameComponents(this.lastItem, this.getItem())) {
            if (!this.lastItem.isEmpty()) {
                this.removeFramedMap(this.lastItem);
            }
        }
    }

    public void markUpdated() {
        if (this.hasLevel()) {
            this.setChanged();
            this.getLevel()
                    .sendBlockUpdated(this.getBlockPos(),
                            this.getBlockState(),
                            this.getBlockState(),
                            ItemFrameBlock.UPDATE_ALL);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos blockPos, BlockState blockState) {
        if (this.hasLevel()) {
            this.dropItem(this.getLevel(), blockPos, blockState, true);
        }
    }

    /**
     * @see ItemFrame#dropItem(ServerLevel, Entity, boolean)
     * @see net.minecraft.world.entity.Entity#spawnAtLocation(ServerLevel, ItemStack, Vec3)
     */
    public void dropItem(Level level, BlockPos blockPos, BlockState blockState, boolean dropItem) {
        ItemStack itemStack = this.getItem();
        this.setItem(0, ItemStack.EMPTY);
        if (dropItem && level.getRandom().nextFloat() < this.dropChance) {
            Vec3 vec3 = Vec3.atCenterOf(blockPos).relative(blockState.getValue(ItemFrameBlock.FACING), -0.25);
            ItemEntity itemEntity = new ItemEntity(level, vec3.x(), vec3.y(), vec3.z(), itemStack);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
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
}

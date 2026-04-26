package fuzs.fastitemframes.common.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.fastitemframes.common.init.ModRegistry;
import fuzs.fastitemframes.common.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.common.api.block.v1.entity.TickingEntityBlock;
import fuzs.puzzleslib.common.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.common.api.util.v1.CommonHelper;
import fuzs.puzzleslib.common.api.util.v1.ShapesHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemFrameItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ItemFrameBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, TickingEntityBlock<ItemFrameBlockEntity> {
    public static final MapCodec<ItemFrameBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(itemFrame -> itemFrame.item),
            propertiesCodec()).apply(instance, ItemFrameBlock::new));
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, ItemFrame.NUM_ROTATIONS - 1);
    public static final BooleanProperty MAP = BlockStateProperties.MAP;
    public static final BooleanProperty INVISIBLE = BooleanProperty.create("invisible");
    public static final BooleanProperty WAXED = BooleanProperty.create("waxed");
    public static final BooleanProperty DYED = BooleanProperty.create("dyed");
    static final VoxelShape SHAPE = box(2.0, 0.0, 2.0, 14.0, 1.0, 14.0);
    static final VoxelShape MAP_SHAPE = box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    static final Map<Direction, VoxelShape> SHAPES = ShapesHelper.rotate(SHAPE);
    static final Map<Direction, VoxelShape> MAP_SHAPES = ShapesHelper.rotate(MAP_SHAPE);
    public static final Map<Item, Block> BY_ITEM = new HashMap<>();

    private final Item item;

    public ItemFrameBlock(Item item, Properties properties) {
        super(properties);
        this.item = item;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, Boolean.FALSE)
                .setValue(ROTATION, 0)
                .setValue(MAP, Boolean.FALSE)
                .setValue(INVISIBLE, Boolean.FALSE)
                .setValue(WAXED, Boolean.FALSE)
                .setValue(DYED, Boolean.FALSE));
        Item.BY_BLOCK.put(this, item);
        BY_ITEM.put(item, this);
    }

    @Override
    protected MapCodec<? extends ItemFrameBlock> codec() {
        return CODEC;
    }

    @Override
    public Item asItem() {
        return this.item;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemInHand, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult hitResult) {
        if (level.getBlockEntity(blockPos) instanceof ItemFrameBlockEntity blockEntity && !blockState.getValue(WAXED)) {
            if (player.isSecondaryUseActive()) {
                // Support toggling invisibility via shift+right-clicking with an empty hand.
                if (!blockEntity.getItem().isEmpty()) {
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.playSound(null,
                                blockPos,
                                blockEntity.getRotateItemSound(),
                                SoundSource.BLOCKS,
                                1.0F,
                                1.0F);
                        serverLevel.setBlock(blockPos, blockState.cycle(INVISIBLE), Block.UPDATE_ALL);
                        serverLevel.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
                    }

                    return InteractionResult.SUCCESS;
                }
            } else {
                if (!blockEntity.getItem().isEmpty() && itemInHand.is(ModRegistry.APPLIES_WAX_ITEM_TAG)) {
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.levelEvent(null, LevelEvent.PARTICLES_AND_SOUND_WAX_ON, blockPos, 0);
                        serverLevel.setBlock(blockPos, blockState.cycle(WAXED), Block.UPDATE_ALL);
                        serverLevel.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
                        itemInHand.consume(1, player);
                    }

                    return InteractionResult.SUCCESS;
                } else {
                    return this.interact(blockEntity, itemInHand, blockState, level, blockPos, player, interactionHand);
                }
            }
        }

        return super.useItemOn(itemInHand, blockState, level, blockPos, player, interactionHand, hitResult);
    }

    /**
     * @see ItemFrame#interact(Player, InteractionHand)
     */
    public InteractionResult interact(ItemFrameBlockEntity blockEntity, ItemStack itemInHand, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand) {
        if (blockState.getValue(ItemFrameBlock.WAXED)) {
            return InteractionResult.PASS;
        } else if (player.level() instanceof ServerLevel serverLevel) {
            if (blockEntity.getItem().isEmpty()) {
                if (!itemInHand.isEmpty() && !blockEntity.isRemoved()) {
                    MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemInHand, level);
                    if (mapItemSavedData != null && mapItemSavedData.isTrackedCountOverLimit(256)) {
                        return InteractionResult.FAIL;
                    } else {
                        blockEntity.setItem(0, itemInHand.copyWithCount(1));
                        level.playSound(null, blockPos, blockEntity.getAddItemSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                        blockEntity.markUpdated(serverLevel);
                        level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
                        itemInHand.consume(1, player);
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    return InteractionResult.PASS;
                }
            } else {
                level.playSound(null, blockPos, blockEntity.getRotateItemSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlock(blockPos, blockState.cycle(ItemFrameBlock.ROTATION), ItemFrameBlock.UPDATE_ALL);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
                return InteractionResult.SUCCESS;
            }
        } else {
            return blockEntity.getItem().isEmpty() && itemInHand.isEmpty() ? InteractionResult.PASS :
                    InteractionResult.SUCCESS;
        }
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter level, BlockPos blockPos, CollisionContext context) {
        return blockState.getValue(MAP) ? MAP_SHAPES.get(blockState.getValue(FACING)) :
                SHAPES.get(blockState.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext context) {
        // to be able to implement BlockBehavior::onProjectileHit a projectile must be able to collide with this block
        if (context instanceof EntityCollisionContext entityCollisionContext
                && entityCollisionContext.getEntity() instanceof Projectile projectile) {
            if (blockGetter instanceof ServerLevel serverLevel && projectile.mayInteract(serverLevel, blockPos)
                    && projectile.mayBreak(serverLevel)) {
                return this.getShape(blockState, blockGetter, blockPos, context);
            }
        }

        return super.getCollisionShape(blockState, blockGetter, blockPos, context);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader level, BlockPos blockPos) {
        BlockState attachedBlockState = level.getBlockState(blockPos.relative(blockState.getValue(FACING)
                .getOpposite()));
        // some weird behavior from the entity for repeaters / comparators
        return attachedBlockState.isSolid() || DiodeBlock.isDiode(attachedBlockState);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        boolean isWaterlogged = fluidState.getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(WATERLOGGED, isWaterlogged);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos neighborBlockPos, BlockState neighborBlockState, RandomSource random) {
        if (direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(level, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }

        if (blockState.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(blockState,
                level,
                scheduledTickAccess,
                blockPos,
                direction,
                neighborBlockPos,
                neighborBlockState,
                random);
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    @Override
    public BlockEntityType<? extends ItemFrameBlockEntity> getBlockEntityType() {
        return ModRegistry.ITEM_FRAME_BLOCK_ENTITY.value();
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ROTATION, INVISIBLE, WAXED, MAP, WATERLOGGED, DYED);
    }

    @Override
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult hitResult, Projectile projectile) {
        BlockPos blockPos = hitResult.getBlockPos();
        if (level instanceof ServerLevel serverLevel && projectile.mayInteract(serverLevel, blockPos)
                && projectile.mayBreak(serverLevel)) {
            level.destroyBlock(blockPos, true, projectile);
            // update potentially attached comparators
            level.updateNeighborsAt(blockPos, this);
            level.updateNeighborsAt(blockPos.relative(blockState.getValue(FACING).getOpposite()), this);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(blockState, serverLevel, blockPos);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    /**
     * @see ItemFrame#getAnalogOutput()
     */
    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        if (level.getBlockEntity(blockPos) instanceof ItemFrameBlockEntity blockEntity) {
            return blockEntity.getItem().isEmpty() ? 0 : blockState.getValue(ROTATION) % ItemFrame.NUM_ROTATIONS + 1;
        } else {
            return 0;
        }
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos blockPos, BlockState blockState, boolean includeData) {
        if (level.getBlockEntity(blockPos) instanceof ItemFrameBlockEntity blockEntity) {
            ItemStack itemStack = this.getPickResult(level, blockPos, blockState, includeData, blockEntity);
            if (itemStack.getItem() instanceof ItemFrameItem) {
                setItemStackColor(itemStack, blockEntity.getColor());
            }

            return itemStack;
        } else {
            return super.getCloneItemStack(level, blockPos, blockState, includeData);
        }
    }

    private ItemStack getPickResult(LevelReader level, BlockPos blockPos, BlockState blockState, boolean includeData, ItemFrameBlockEntity blockEntity) {
        // it's fine to use proxy value as this is only called client-side
        if (!CommonHelper.hasControlDown()
                || ModLoaderEnvironment.INSTANCE.isClient() && !CommonHelper.getClientPlayer().isCreative()) {
            ItemStack itemStack = blockEntity.getItem();
            if (!itemStack.isEmpty()) {
                return itemStack.copy();
            }
        }

        return super.getCloneItemStack(level, blockPos, blockState, includeData);
    }

    public static ItemStack setItemStackColor(ItemStack itemStack, @Nullable DyedItemColor color) {
        if (color != null) {
            itemStack.set(DataComponents.DYED_COLOR, color);
        } else {
            itemStack.remove(DataComponents.DYED_COLOR);
        }

        return itemStack;
    }
}

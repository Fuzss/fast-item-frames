package fuzs.fastitemframes.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.core.v1.Proxy;
import fuzs.puzzleslib.api.shape.v1.ShapesHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;

@SuppressWarnings("deprecation")
public class ItemFrameBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<ItemFrameBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_type").forGetter(ItemFrameBlock::getEntityType),
            propertiesCodec()
    ).apply(instance, ItemFrameBlock::new));
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty INVISIBLE = BooleanProperty.create("invisible");
    public static final BooleanProperty HAS_MAP = BooleanProperty.create("has_map");
    public static final BooleanProperty DYED = BooleanProperty.create("dyed");
    static final VoxelShape SHAPE = box(2.0, 0.0, 2.0, 14.0, 1.0, 14.0);
    static final VoxelShape MAP_SHAPE = box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    static final Map<Direction, VoxelShape> SHAPES = ShapesHelper.rotate(SHAPE);
    static final Map<Direction, VoxelShape> MAP_SHAPES = ShapesHelper.rotate(MAP_SHAPE);

    private final EntityType<?> entityType;

    public ItemFrameBlock(EntityType<?> entityType, Properties properties) {
        super(properties);
        this.entityType = entityType;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(WATERLOGGED, Boolean.FALSE)
                .setValue(INVISIBLE, Boolean.FALSE)
                .setValue(HAS_MAP, Boolean.FALSE)
                .setValue(DYED, Boolean.FALSE));
    }

    @Override
    protected MapCodec<? extends ItemFrameBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        if (level.getBlockEntity(pos) instanceof ItemFrameBlockEntity blockEntity) {

            ItemFrame itemFrame = blockEntity.getEntityRepresentation();
            if (itemFrame != null) {

                if (player.isSecondaryUseActive() && !this.isFixed(level, pos)) {

                    level.setBlock(pos, state.setValue(INVISIBLE, !state.getValue(INVISIBLE)), 2);
                    itemFrame.playSound(itemFrame.getRotateItemSound(), 1.0F, 1.0F);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                } else {

                    InteractionResult result = itemFrame.interact(player, hand);
                    if (result.consumesAction()) blockEntity.setChanged();
                    return result;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HAS_MAP) ? MAP_SHAPES.get(state.getValue(FACING)) : SHAPES.get(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
        // to be able to implement BlockBehavior::onProjectileHit a projectile must be able to collide with this block
        if (context instanceof EntityCollisionContext entityCollisionContext &&
                entityCollisionContext.getEntity() instanceof Projectile projectile) {
            if (blockGetter instanceof Level level && projectile.mayInteract(level, pos) &&
                    projectile.mayBreak(level)) {
                return this.getShape(state, blockGetter, pos, context);
            }
        }

        return super.getCollisionShape(state, blockGetter, pos, context);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return this.isFixed(level, pos) ||
                level.getBlockState(pos.relative(state.getValue(FACING).getOpposite())).isSolid();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        boolean waterlogged = fluidState.getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(WATERLOGGED, waterlogged);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemFrameBlockEntity(pos, state);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, INVISIBLE, HAS_MAP, WATERLOGGED, DYED);
    }

    public EntityType<?> getEntityType() {
        return this.entityType;
    }

    public boolean isFixed(LevelReader level, BlockPos blockPos) {
        if (level.getBlockEntity(blockPos) instanceof ItemFrameBlockEntity blockEntity) {
            ItemFrame itemFrame = blockEntity.getEntityRepresentation();
            if (itemFrame != null) {
                try {
                    // TODO access widener
                    Field setDirection = ItemFrame.class.getDeclaredField("fixed");
                    setDirection.setAccessible(true);
                    return (boolean) MethodHandles.lookup().unreflectGetter(setDirection).invoke(itemFrame);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return false;
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (!level.isClientSide && !this.isFixed(level, blockPos) && projectile.mayInteract(level, blockPos) &&
                projectile.mayBreak(level)) {
            level.destroyBlock(blockPos, true, projectile);
            // update potentially attached comparators
            level.updateNeighborsAt(blockPos, this);
            level.updateNeighborsAt(blockPos.relative(state.getValue(FACING).getOpposite()), this);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof ItemFrameBlockEntity blockEntity) {
//                Containers.dropContents(level, pos, blockEntity);
                // TODO don't drop frame?
                // loot table instead
                blockEntity.getEntityRepresentation().dropItem(null);
                blockEntity.setChanged();
            }

            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ItemFrameBlockEntity blockEntity) {
            ItemFrame itemFrame = blockEntity.getEntityRepresentation();
            if (itemFrame != null) {
                return itemFrame.getAnalogOutput();
            }
        }

        return 0;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        // it's fine to use proxy value as this is only called client-side
        if (!Proxy.INSTANCE.hasControlDown() && level.getBlockEntity(pos) instanceof ItemFrameBlockEntity blockEntity) {
            ItemFrame itemFrame = blockEntity.getEntityRepresentation();
            if (itemFrame != null) {
                return itemFrame.getPickResult();
            }
        }

        return super.getCloneItemStack(level, pos, state);
    }
}

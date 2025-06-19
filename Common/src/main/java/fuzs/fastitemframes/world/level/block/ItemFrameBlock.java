package fuzs.fastitemframes.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.block.v1.entity.TickingEntityBlock;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.api.core.v1.Proxy;
import fuzs.puzzleslib.api.shape.v1.ShapesHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemFrameItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

@SuppressWarnings("deprecation")
public class ItemFrameBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, TickingEntityBlock<ItemFrameBlockEntity> {
    public static final MapCodec<ItemFrameBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(itemFrame -> itemFrame.item),
            propertiesCodec()).apply(instance, ItemFrameBlock::new));
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty INVISIBLE = BooleanProperty.create("invisible");
    public static final BooleanProperty HAS_MAP = BooleanProperty.create("has_map");
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
                .setValue(FACING, Direction.UP)
                .setValue(WATERLOGGED, Boolean.FALSE)
                .setValue(INVISIBLE, Boolean.FALSE)
                .setValue(HAS_MAP, Boolean.FALSE)
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
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (level.getBlockEntity(pos) instanceof ItemFrameBlockEntity blockEntity) {

            ItemFrame itemFrame = blockEntity.getEntityRepresentation();
            if (itemFrame != null) {

                if (player.isSecondaryUseActive() && !this.isFixed(level, pos) && !itemFrame.getItem().isEmpty()) {

                    // support toggling invisibility via shift+right-clicking + empty hand
                    level.setBlock(pos, state.setValue(INVISIBLE, !state.getValue(INVISIBLE)), 2);
                    itemFrame.playSound(itemFrame.getRotateItemSound(), 1.0F, 1.0F);
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                } else {

                    if (itemFrame.getItem().isEmpty()) itemFrame.setRotation(0);
                    InteractionResult result = itemFrame.interact(player, hand);
                    if (result.consumesAction()) blockEntity.markUpdated();
                    return switch (result) {
                        case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                        case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
                        case FAIL -> ItemInteractionResult.FAIL;
                        case CONSUME -> ItemInteractionResult.CONSUME;
                        case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
                    };
                }
            }
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HAS_MAP) ? MAP_SHAPES.get(state.getValue(FACING)) : SHAPES.get(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
        // to be able to implement BlockBehavior::onProjectileHit a projectile must be able to collide with this block
        if (context instanceof EntityCollisionContext entityCollisionContext
                && entityCollisionContext.getEntity() instanceof Projectile projectile) {
            if (blockGetter instanceof Level level && projectile.mayInteract(level, pos)
                    && projectile.mayBreak(level)) {
                return this.getShape(state, blockGetter, pos, context);
            }
        }

        return super.getCollisionShape(state, blockGetter, pos, context);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (this.isFixed(level, pos)) {
            return true;
        } else {
            BlockState blockState = level.getBlockState(pos.relative(state.getValue(FACING).getOpposite()));
            // some weird behavior from the entity for repeaters / comparators
            return blockState.isSolid() || DiodeBlock.isDiode(blockState);
        }
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

    @Override
    public BlockEntityType<? extends ItemFrameBlockEntity> getBlockEntityType() {
        return ModRegistry.ITEM_FRAME_BLOCK_ENTITY.value();
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

    public boolean isFixed(LevelReader level, BlockPos blockPos) {
        if (level.getBlockEntity(blockPos) instanceof ItemFrameBlockEntity blockEntity) {
            ItemFrame itemFrame = blockEntity.getEntityRepresentation();
            return itemFrame != null && itemFrame.fixed;
        }

        return false;
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (!level.isClientSide && !this.isFixed(level, blockPos) && projectile.mayInteract(level, blockPos)
                && projectile.mayBreak(level)) {
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
                blockEntity.getEntityRepresentation().dropItem(null, false);
                // not sure if this is necessary since the block entity is about to be deleted as well
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

        if (level.getBlockEntity(pos) instanceof ItemFrameBlockEntity blockEntity) {

            ItemStack itemStack = null;
            // it's fine to use proxy value as this is only called client-side
            if (!Proxy.INSTANCE.hasControlDown()
                    || ModLoaderEnvironment.INSTANCE.isClient() && !Proxy.INSTANCE.getClientPlayer().isCreative()) {

                ItemFrame itemFrame = blockEntity.getEntityRepresentation();
                if (itemFrame != null) {

                    itemStack = itemFrame.getPickResult();
                }
            }

            if (itemStack == null) {

                itemStack = super.getCloneItemStack(level, pos, state);
            }

            if (itemStack.getItem() instanceof ItemFrameItem) {

                OptionalInt color = blockEntity.getColor();
                if (color.isPresent()) {

                    setItemFrameColor(itemStack, color.getAsInt());
                }
            }

            return itemStack;
        }

        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public String getDescriptionId() {
        return this.asItem().getDescriptionId();
    }

    public static ItemStack setItemFrameColor(ItemStack itemStack, int color) {
        itemStack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, true));
        return itemStack;
    }
}

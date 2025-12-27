package fuzs.fastitemframes.handler;

import fuzs.fastitemframes.init.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public abstract class AbstractMenuProviderInteraction {

    protected abstract boolean supportsCurrentEnvironment(boolean isClientSide);

    public @Nullable InteractionResult interactWithEntity(Player player, Level level, InteractionHand interactionHand, Entity entity) {
        BlockPos blockPos = entity.blockPosition();
        if (this.isEntityPassingThroughClicks(entity)) {
            Direction neighborDirection = entity.getDirection().getOpposite();
            return this.interactWithAttachedBlock(player,
                    level,
                    interactionHand,
                    blockPos,
                    neighborDirection,
                    (BlockPos neighborBlockPos) -> {
                        // The vector is mostly used for distance from the player checks, which should remain at the original position.
                        return new BlockHitResult(Vec3.atCenterOf(blockPos),
                                entity.getDirection(),
                                neighborBlockPos,
                                false);
                    });
        }

        return null;
    }

    protected abstract boolean isEntityPassingThroughClicks(Entity entity);

    public @Nullable InteractionResult interactWithBlock(Player player, Level level, InteractionHand interactionHand, BlockHitResult hitResult) {
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (this.isBlockPassingThroughClicks(blockState)) {
            Direction neighborDirection = this.getNeighborDirection(blockState, hitResult);
            return this.interactWithAttachedBlock(player,
                    level,
                    interactionHand,
                    blockPos,
                    neighborDirection,
                    hitResult::withPosition);
        }

        return null;
    }

    private Direction getNeighborDirection(BlockState blockState, BlockHitResult hitResult) {
        Direction direction = hitResult.getDirection();
        for (Map.Entry<Property<?>, Comparable<?>> entry : blockState.getValues().entrySet()) {
            if (entry.getKey().getValueClass() == Direction.class) {
                direction = (Direction) entry.getValue();
                break;
            }
        }

        return direction.getOpposite();
    }

    protected abstract boolean isBlockPassingThroughClicks(BlockState blockState);

    private @Nullable InteractionResult interactWithAttachedBlock(Player player, Level level, InteractionHand interactionHand, BlockPos blockPos, Direction neighborDirection, Function<BlockPos, BlockHitResult> hitResultGetter) {
        if (!player.isSecondaryUseActive()) {
            ItemStack itemInHand = player.getItemInHand(interactionHand);
            if (!this.requiresEmptyHand() || itemInHand.isEmpty()) {
                BlockPos neighborBlockPos = blockPos.relative(neighborDirection);
                BlockState neighborBlockState = level.getBlockState(neighborBlockPos);
                if (!neighborBlockState.is(ModRegistry.REQUIRES_DIRECT_CLICKS_BLOCK_TAG)
                        && neighborBlockState.getMenuProvider(level, neighborBlockPos) != null) {
                    return this.useBlock(neighborBlockState,
                            level,
                            neighborBlockPos,
                            player,
                            interactionHand,
                            hitResultGetter.apply(neighborBlockPos));
                }
            }
        }

        return null;
    }

    protected abstract boolean requiresEmptyHand();

    protected abstract @Nullable InteractionResult useBlock(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult hitResult);
}

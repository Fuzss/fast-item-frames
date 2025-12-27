package fuzs.fastitemframes.handler;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.config.ServerConfig;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public class ReachBehindHandler {

    public static EventResultHolder<InteractionResult> onUseEntity(Player player, Level level, InteractionHand interactionHand, Entity entity) {
        if (!FastItemFrames.CONFIG.get(ServerConfig.class).passClicksToAttachedBlock) {
            return EventResultHolder.pass();
        }

        BlockPos blockPos = entity.blockPosition();
        if (entity.getType().is(ModRegistry.PASSES_CLICKS_THROUGH_ENTITY_TYPE_TAG)) {
            Direction neighborDirection = entity.getDirection().getOpposite();
            InteractionResult interactionResult = getInteractionResultEventResultHolder(player,
                    level,
                    interactionHand,
                    neighborDirection,
                    blockPos,
                    (BlockPos neighborBlockPos) -> ItemFrameHandler.getBlockHitResult(entity, neighborBlockPos));
            if (interactionResult != null) {
                return EventResultHolder.interrupt(interactionResult);
            }
        }

        return EventResultHolder.pass();
    }

    public static EventResultHolder<InteractionResult> onUseBlock(Player player, Level level, InteractionHand interactionHand, BlockHitResult hitResult) {
        if (!FastItemFrames.CONFIG.get(ServerConfig.class).passClicksToAttachedBlock) {
            return EventResultHolder.pass();
        }

        BlockPos blockPos = hitResult.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.is(ModRegistry.PASSES_CLICKS_THROUGH_BLOCK_TAG)) {
            Direction neighborDirection = getNeighborDirection(blockState, hitResult);
            InteractionResult interactionResult = getInteractionResultEventResultHolder(player,
                    level,
                    interactionHand,
                    neighborDirection,
                    blockPos,
                    hitResult::withPosition);
            if (interactionResult != null) {
                return EventResultHolder.interrupt(interactionResult);
            }
        }

        return EventResultHolder.pass();
    }

    private static @Nullable InteractionResult getInteractionResultEventResultHolder(Player player, Level level, InteractionHand interactionHand, Direction neighborDirection, BlockPos blockPos, Function<BlockPos, BlockHitResult> hitResultGetter) {
        if (!player.isSecondaryUseActive()) {
            ItemStack itemInHand = player.getItemInHand(interactionHand);
            if (!FastItemFrames.CONFIG.get(ServerConfig.class).requiresEmptyHand || itemInHand.isEmpty()) {
                BlockPos neighborBlockPos = blockPos.relative(neighborDirection);
                BlockState neighborBlockState = level.getBlockState(neighborBlockPos);
                if (!neighborBlockState.is(ModRegistry.REQUIRES_DIRECT_CLICKS_BLOCK_TAG)
                        && neighborBlockState.getMenuProvider(level, blockPos) != null) {
                    return useBlock(neighborBlockState,
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

    private static Direction getNeighborDirection(BlockState blockState, BlockHitResult hitResult) {
        Direction direction;
        if (blockState.hasProperty(BlockStateProperties.FACING)) {
            direction = blockState.getValue(BlockStateProperties.FACING);
        } else {
            direction = hitResult.getDirection();
        }

        return direction.getOpposite();
    }

    private static @Nullable InteractionResult useBlock(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult hitResult) {
        ItemStack itemStack = player.getItemInHand(interactionHand).copy();
        InteractionResult useItemOnResult;
        if (!FastItemFrames.CONFIG.get(ServerConfig.class).requiresEmptyHand) {
            useItemOnResult = blockState.useItemOn(player.getItemInHand(interactionHand),
                    level,
                    player,
                    interactionHand,
                    hitResult);
            if (useItemOnResult.consumesAction()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, itemStack);
                }

                return useItemOnResult;
            }
        } else {
            useItemOnResult = InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (useItemOnResult instanceof InteractionResult.TryEmptyHandInteraction
                && interactionHand == InteractionHand.MAIN_HAND) {
            InteractionResult useWithoutItemResult = blockState.useWithoutItem(level, player, hitResult);
            if (useWithoutItemResult.consumesAction()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.DEFAULT_BLOCK_USE.trigger(serverPlayer, blockPos);
                }

                return useWithoutItemResult;
            }
        }

        return null;
    }

    public static boolean interactWithAttachedBlockWhenClicked(Player player, boolean isFixed, ItemStack itemInHand) {
        return !player.isSecondaryUseActive() && (isFixed || itemInHand.isEmpty());
    }

    public static @Nullable InteractionResult passClicksToAttachedBlock(Level level, Player player, BlockPos blockPos, BlockHitResult hitResult) {
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.is(ModRegistry.REQUIRES_DIRECT_CLICKS_BLOCK_TAG)
                && blockState.getMenuProvider(level, blockPos) != null) {
            InteractionResult interactionResult = blockState.useWithoutItem(level, player, hitResult);
            if (interactionResult.consumesAction()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.DEFAULT_BLOCK_USE.trigger(serverPlayer, blockPos);
                }

                return interactionResult;
            }
        }

        return null;
    }
}

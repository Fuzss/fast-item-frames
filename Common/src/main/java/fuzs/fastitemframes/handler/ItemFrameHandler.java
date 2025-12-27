package fuzs.fastitemframes.handler;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.config.ServerConfig;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

public class ItemFrameHandler {

    public static EventResult onBreakBlock(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, Player player, ItemStack itemInHand) {
        if (blockState.is(ModRegistry.ITEM_FRAMES_BLOCK_TAG)
                && serverLevel.getBlockEntity(blockPos) instanceof ItemFrameBlockEntity blockEntity) {
            if (!blockEntity.getItem().isEmpty()) {
                blockEntity.dropItem(serverLevel, blockPos, blockState, !player.getAbilities().instabuild);
                serverLevel.playSound(null, blockPos, blockEntity.getRemoveItemSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                blockEntity.markUpdated(serverLevel);
                serverLevel.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
                return blockState.getValue(ItemFrameBlock.WAXED) ? EventResult.PASS : EventResult.INTERRUPT;
            }
        }

        return EventResult.PASS;
    }

    public static EventResult onEntityLoad(Entity entity, ServerLevel serverLevel, boolean isNewlySpawned) {
        if (entity.getType().is(ModRegistry.ITEM_FRAMES_ENTITY_TYPE_TAG) && entity instanceof ItemFrame itemFrame) {
            if (isNewlySpawned || FastItemFrames.CONFIG.get(ServerConfig.class).convertAllExistingItemFrames) {
                serverLevel.getServer().schedule(new TickTask(serverLevel.getServer().getTickCount(), () -> {
                    Block block = ItemFrameBlock.BY_ITEM.get(itemFrame.getFrameItemStack().getItem());
                    BlockPos blockPos = itemFrame.blockPosition();
                    // Require air; another item frame block might already be placed in this location or a decorative block.
                    // Do not check for replaceable blocks, will break parity with vanilla otherwise.
                    if (block != null && serverLevel.hasChunkAt(blockPos) && (serverLevel.isEmptyBlock(blockPos)
                            || serverLevel.getBlockState(blockPos).is(Blocks.WATER))) {
                        BlockState blockState = getItemFrameStateForPlacement(serverLevel, block, blockPos, itemFrame);
                        if (blockState != null) {
                            setItemFrameBlock(serverLevel, blockPos, blockState, itemFrame);
                            // Use kill, not discard, so that the item frame is properly removed from a potential map item it is holding.
                            itemFrame.kill(serverLevel);
                        }
                    }
                }));
            }
        }

        return EventResult.PASS;
    }

    private static @Nullable BlockState getItemFrameStateForPlacement(ServerLevel serverLevel, Block block, BlockPos blockPos, ItemFrame itemFrame) {
        BlockHitResult blockHitResult = getAttachedBlockHitResult(itemFrame);
        BlockPlaceContext blockPlaceContext = new BlockPlaceContext(serverLevel,
                null,
                InteractionHand.MAIN_HAND,
                ItemStack.EMPTY,
                blockHitResult);
        BlockState blockState = block.getStateForPlacement(blockPlaceContext);
        if (blockState != null && blockState.canSurvive(serverLevel, blockPos) && serverLevel.isUnobstructed(blockState,
                blockPos,
                CollisionContext.empty())) {
            return blockState;
        } else {
            return null;
        }
    }

    private static void setItemFrameBlock(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, ItemFrame itemFrame) {
        DyedItemColor dyedItemColor = ModRegistry.ITEM_FRAME_COLOR_ATTACHMENT_TYPE.get(itemFrame);
        serverLevel.setBlock(blockPos,
                blockState.setValue(ItemFrameBlock.ROTATION, itemFrame.getRotation())
                        .setValue(ItemFrameBlock.WAXED,
                                itemFrame.fixed || ModRegistry.WAXED_ITEM_FRAME_ATTACHMENT_TYPE.has(itemFrame))
                        .setValue(ItemFrameBlock.DYED, dyedItemColor != null),
                Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE);
        if (serverLevel.getBlockEntity(blockPos) instanceof ItemFrameBlockEntity blockEntity) {
            blockEntity.setItem(0, itemFrame.getItem());
            blockEntity.setDropChance(itemFrame.dropChance);
            blockEntity.setColor(dyedItemColor);
            blockEntity.markUpdated(serverLevel);
        }
    }

    private static BlockHitResult getAttachedBlockHitResult(Entity entity) {
        BlockPos blockPos = entity.blockPosition();
        BlockPos neihgborBlockPos = blockPos.relative(entity.getDirection().getOpposite());
        // The vector is mostly used for distance from the player checks, which should remain at the original position.
        return new BlockHitResult(Vec3.atCenterOf(blockPos), entity.getDirection(), neihgborBlockPos, false);
    }

    public static EventResultHolder<InteractionResult> onUseEntity(Player player, Level level, InteractionHand interactionHand, Entity entity) {
        if (entity.getType().is(ModRegistry.ITEM_FRAMES_ENTITY_TYPE_TAG) && entity instanceof ItemFrame itemFrame) {
            if (itemFrame.fixed) {
                return EventResultHolder.pass();
            }

            if (ModRegistry.WAXED_ITEM_FRAME_ATTACHMENT_TYPE.has(itemFrame)) {
                return EventResultHolder.interrupt(InteractionResult.PASS);
            }

            if (player.isSecondaryUseActive()) {
                // The entity requires additional checks compared to the block.
                if (player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) {
                    // Support toggling invisibility via shift+right-clicking with an empty hand.
                    if (!itemFrame.getItem().isEmpty()) {
                        itemFrame.setInvisible(!itemFrame.isInvisible());
                        if (level instanceof ServerLevel) {
                            itemFrame.playSound(itemFrame.getRotateItemSound(), 1.0F, 1.0F);
                            itemFrame.gameEvent(GameEvent.BLOCK_CHANGE, player);
                        }

                        return EventResultHolder.interrupt(InteractionResult.SUCCESS);
                    }
                }
            } else {
                ItemStack itemInHand = player.getItemInHand(interactionHand);
                if (!itemFrame.getItem().isEmpty() && itemInHand.is(ModRegistry.APPLIES_WAX_ITEM_TAG)) {
                    ModRegistry.WAXED_ITEM_FRAME_ATTACHMENT_TYPE.set(itemFrame, Unit.INSTANCE);
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.levelEvent(null,
                                LevelEvent.PARTICLES_AND_SOUND_WAX_ON,
                                itemFrame.blockPosition(),
                                0);
                        serverLevel.gameEvent(player, GameEvent.BLOCK_CHANGE, itemFrame.blockPosition());
                        itemInHand.consume(1, player);
                    }

                    return EventResultHolder.interrupt(InteractionResult.SUCCESS);
                }
            }

            if (player.isSecondaryUseActive()) {
                // don't allow sneak+right-clicking when hand not empty just like with the block
                return EventResultHolder.interrupt(InteractionResult.PASS);
            }
        }

        return EventResultHolder.pass();
    }

    public static EventResult onAttackEntity(Player player, Level level, InteractionHand interactionHand, Entity entity) {
        if (entity.getType().is(ModRegistry.ITEM_FRAMES_ENTITY_TYPE_TAG) && entity instanceof ItemFrame itemFrame) {
            if (!itemFrame.fixed && !itemFrame.getItem().isEmpty()) {
                itemFrame.setInvisible(false);
                if (level instanceof ServerLevel serverLevel && ModRegistry.WAXED_ITEM_FRAME_ATTACHMENT_TYPE.has(
                        itemFrame)) {
                    // Drop both the item frame and the item at the same time when waxed, as no new item can be inserted into an empty frame.
                    itemFrame.hurtServer(serverLevel, serverLevel.damageSources().playerAttack(player), 1.0F);
                }
            }
        }

        return EventResult.PASS;
    }
}

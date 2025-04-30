package fuzs.fastitemframes.handler;

import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import fuzs.puzzleslib.api.util.v1.InteractionResultHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class ItemFrameHandler {

    public static EventResult onBreakBlock(ServerLevel level, BlockPos pos, BlockState state, Player player, ItemStack itemInHand) {
        if (state.is(ModRegistry.ITEM_FRAMES_BLOCK_TAG)) {
            if (level.getBlockEntity(pos) instanceof ItemFrameBlockEntity blockEntity) {
                ItemStack itemStack = blockEntity.getItem();
                if (!itemStack.isEmpty()) {
                    blockEntity.getEntityRepresentation()
                            .hurtServer(level, level.damageSources().playerAttack(player), 1.0F);
                    blockEntity.markUpdated();
                    return EventResult.INTERRUPT;
                }
            }
        }

        return EventResult.PASS;
    }

    public static EventResult onEntityLoad(Entity entity, ServerLevel serverLevel) {

        if (entity.getType().is(ModRegistry.ITEM_FRAMES_ENTITY_TYPE_TAG) && entity instanceof ItemFrame itemFrame) {

            serverLevel.getServer().schedule(new TickTask(serverLevel.getServer().getTickCount(), () -> {

                Block block = ItemFrameBlock.BY_ITEM.get(itemFrame.getFrameItemStack().getItem());
                BlockPos blockPos = entity.blockPosition();
                // require air, another item frame block might already be placed in this location, or a decorative block
                // do not check for replaceable blocks, will break parity with vanilla otherwise
                if (block != null && serverLevel.hasChunkAt(blockPos) &&
                        (serverLevel.isEmptyBlock(blockPos) || serverLevel.getBlockState(blockPos).is(Blocks.WATER))) {

                    BlockHitResult blockHitResult = new BlockHitResult(new Vec3(0.5, 0.5, 0.5),
                            itemFrame.getDirection(),
                            blockPos.relative(itemFrame.getDirection().getOpposite()),
                            false);
                    BlockPlaceContext blockPlaceContext = new BlockPlaceContext(serverLevel,
                            null,
                            InteractionHand.MAIN_HAND,
                            ItemStack.EMPTY,
                            blockHitResult);
                    BlockState blockState = block.getStateForPlacement(blockPlaceContext);
                    if (blockState != null && blockState.canSurvive(serverLevel, blockPos) &&
                            serverLevel.isUnobstructed(blockState, blockPos, CollisionContext.empty())) {

                        serverLevel.setBlock(blockPos, blockState, 2);
                        if (serverLevel.getBlockEntity(blockPos) instanceof ItemFrameBlockEntity blockEntity) {

                            blockEntity.load(itemFrame);
                            blockEntity.setChanged();
                            // client caches the wrong block color when block entity data is synced in the same tick as the block being set
                            // this will cause a brief flicker, but the color will show correctly after that
                            blockEntity.markUpdated();
                        }

                        entity.discard();
                    }
                }
            }));
        }

        return EventResult.PASS;
    }

    public static EventResultHolder<InteractionResult> onUseEntity(Player player, Level level, InteractionHand interactionHand, Entity entity) {
        if (entity.getType().is(ModRegistry.ITEM_FRAMES_ENTITY_TYPE_TAG) && entity instanceof ItemFrame itemFrame) {
            if (!itemFrame.fixed && itemFrame.getItem().isEmpty()) {
                itemFrame.setRotation(0);
            }
            if (player.isSecondaryUseActive()) {
                if (player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty() &&
                        !itemFrame.getItem().isEmpty()) {
                    // support toggling invisibility with empty hand + sneak+right-click just like for block
                    itemFrame.setInvisible(!itemFrame.isInvisible());
                    itemFrame.playSound(itemFrame.getRotateItemSound(), 1.0F, 1.0F);
                    return EventResultHolder.interrupt(InteractionResultHelper.sidedSuccess(level.isClientSide));
                } else {
                    // don't allow sneak+right-clicking when hand not empty just like with the block
                    return EventResultHolder.interrupt(InteractionResult.PASS);
                }
            }
        }

        return EventResultHolder.pass();
    }

    public static EventResult onAttackEntity(Player player, Level level, InteractionHand interactionHand, Entity entity) {
        if (entity.getType().is(ModRegistry.ITEM_FRAMES_ENTITY_TYPE_TAG) && entity instanceof ItemFrame itemFrame) {
            if (!itemFrame.fixed && !itemFrame.getItem().isEmpty()) {
                itemFrame.setInvisible(false);
            }
        }

        return EventResult.PASS;
    }
}

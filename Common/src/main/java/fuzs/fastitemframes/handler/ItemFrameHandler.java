package fuzs.fastitemframes.handler;

import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ItemFrameHandler {

    public static EventResult onBreakBlock(ServerLevel level, BlockPos pos, BlockState state, Player player, ItemStack itemInHand) {
        if (state.is(ModRegistry.ITEM_FRAMES_BLOCK_TAG)) {
            if (level.getBlockEntity(pos) instanceof ItemFrameBlockEntity blockEntity) {
                ItemStack itemStack = blockEntity.getItem();
                if (!itemStack.isEmpty()) {
                    blockEntity.getEntityRepresentation().hurt(level.damageSources().playerAttack(player), 1.0F);
                    blockEntity.markUpdated();
                    return EventResult.INTERRUPT;
                }
            }
        }

        return EventResult.PASS;
    }

    public static EventResult onEntityLoad(Entity entity, ServerLevel serverLevel) {

        if (entity.getType().is(ModRegistry.ITEM_FRAMES_ENTITY_TYPE_TAG) && entity instanceof ItemFrame itemFrame) {

            serverLevel.getServer().tell(new TickTask(serverLevel.getServer().getTickCount(), () -> {

                Block block = ItemFrameBlock.BY_ITEM.get(itemFrame.getFrameItemStack().getItem());
                BlockPos blockPos = entity.blockPosition();
                // require air, another item frame block might already be placed in this location, or a decorative item such as coral fans
                if (block != null && serverLevel.isEmptyBlock(blockPos)) {

                    serverLevel.setBlock(blockPos,
                            block.defaultBlockState().setValue(ItemFrameBlock.FACING, itemFrame.getDirection()),
                            2);

                    if (serverLevel.getBlockEntity(blockPos) instanceof ItemFrameBlockEntity blockEntity) {

                        blockEntity.load(itemFrame);
                        blockEntity.setChanged();
                        // client caches the wrong block color when block entity data is synced in the same tick as the block being set
                        // this will cause a brief flicker, but the color will show correctly after that
                        blockEntity.markUpdated();
                    }

                    entity.discard();
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
                    return EventResultHolder.interrupt(InteractionResult.sidedSuccess(level.isClientSide));
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

    public static InteractionResult itemFrameCauldronInteraction(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof DyeableLeatherItem item)) {
            return InteractionResult.PASS;
        } else {
            if (!item.hasCustomColor(itemStack)) {
                return InteractionResult.PASS;
            } else {
                if (!level.isClientSide) {
                    ItemStack newItemStack = itemStack.copyWithCount(1);
                    item.clearColor(newItemStack);
                    player.setItemInHand(interactionHand,
                            ItemUtils.createFilledResult(itemStack, player, newItemStack));
                    LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
    }
}

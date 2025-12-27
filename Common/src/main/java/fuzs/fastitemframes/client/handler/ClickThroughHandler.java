package fuzs.fastitemframes.client.handler;

import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.qualitylife.init.ModRegistry;
import fuzs.qualitylife.mixin.client.accessor.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ClickThroughHandler {
    private static boolean isProcessingInteraction;

    public static EventResult onUseInteraction(Minecraft minecraft, LocalPlayer player, InteractionHand interactionHand, HitResult hitResult) {
        if (isProcessingInteraction) return EventResult.PASS;
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
            BlockState state = minecraft.level.getBlockState(pos);
            if (state.is(ModRegistry.CLICK_THROUGH_BLOCK_TAG) && state.hasProperty(HorizontalDirectionalBlock.FACING)) {
                Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
                pos = pos.relative(direction.getOpposite());
                if (!useItemOnMenuProvider(minecraft, player, direction, pos)) {
                    if (useItem(minecraft, player, interactionHand, (BlockHitResult) hitResult)) {
                        return EventResult.INTERRUPT;
                    }
                }
            }
        } else if (!player.isSecondaryUseActive() && hitResult.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hitResult).getEntity();
            if (entity.getType().is(ModRegistry.CLICK_THROUGH_ENTITY_TYPE_TAG)) {
                BlockPos pos = entity.blockPosition().relative(entity.getDirection().getOpposite());
                useItemOnMenuProvider(minecraft, player, entity.getDirection(), pos);
            }
        }
        if (minecraft.hitResult != hitResult && !ModLoaderEnvironment.INSTANCE.getModLoader().isForgeLike()) {
            ((MinecraftAccessor) minecraft).armorquickswap$callStartUseItem();
            return EventResult.INTERRUPT;
        } else {
            return EventResult.PASS;
        }
    }

    private static boolean useItemOnMenuProvider(Minecraft minecraft, LocalPlayer player, Direction direction, BlockPos pos) {
        if (minecraft.level.getBlockState(pos).getMenuProvider(minecraft.level, pos) != null) {
            if (!player.isSecondaryUseActive()) {
                Vec3 hitLocation = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                minecraft.hitResult = new BlockHitResult(hitLocation, direction, pos, false);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private static boolean useItem(Minecraft minecraft, LocalPlayer player, InteractionHand interactionHand, BlockHitResult hitResult) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (itemInHand.getItem() instanceof SignApplicator) {
            int itemCount = itemInHand.getCount();
            InteractionResult result = useItemWithoutSecondaryUse(minecraft, player, interactionHand, hitResult);
            if (result.consumesAction() && result.shouldSwing()) {
                player.swing(interactionHand);
                if (!itemInHand.isEmpty() &&
                        (itemInHand.getCount() != itemCount || minecraft.gameMode.hasInfiniteItems())) {
                    minecraft.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private static InteractionResult useItemWithoutSecondaryUse(Minecraft minecraft, LocalPlayer player, InteractionHand interactionHand, BlockHitResult hitResult) {
        boolean shiftKeyDown = player.input.shiftKeyDown;
        player.input.shiftKeyDown = false;
        player.connection.send(
                new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
        isProcessingInteraction = true;
        InteractionResult result = minecraft.gameMode.useItemOn(player, interactionHand, hitResult);
        isProcessingInteraction = false;
        player.input.shiftKeyDown = shiftKeyDown;
        player.connection.send(new ServerboundPlayerCommandPacket(player,
                shiftKeyDown ? ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY :
                        ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY
        ));
        return result;
    }
}

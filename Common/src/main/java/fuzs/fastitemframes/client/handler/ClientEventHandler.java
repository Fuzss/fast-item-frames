package fuzs.fastitemframes.client.handler;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.client.util.v1.RenderPropertyKey;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientEventHandler {
    public static final RenderPropertyKey<Integer> COLOR_RENDER_PROPERTY_KEY = new RenderPropertyKey<>(FastItemFrames.id(
            "color"));

    public static EventResult onAttackBlock(Player player, Level level, InteractionHand interactionHand, BlockPos pos, Direction direction) {
        if (level.isClientSide) {
            if (level.getBlockState(pos).is(ModRegistry.ITEM_FRAMES_BLOCK_TAG)) {
                if (level.getBlockEntity(pos) instanceof ItemFrameBlockEntity blockEntity) {
                    if (!blockEntity.getItem().isEmpty()) {
                        // make sure breaking is prevented on client as well to bypass visual glitch until server sends sync packet
                        // also set default destroy delay so that not both item and frame are destroyed at once
                        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
                        gameMode.destroyDelay = 5;
                        return EventResult.INTERRUPT;
                    }
                }
            }
        }

        return EventResult.PASS;
    }

    public static void onExtractRenderState(Entity entity, EntityRenderState entityRenderState, float partialTick) {
        if (entity instanceof ItemFrame && entityRenderState instanceof ItemFrameRenderState) {
            if (!entityRenderState.isInvisible && ModRegistry.ITEM_FRAME_COLOR_ATTACHMENT_TYPE.has(entity)) {
                RenderPropertyKey.setRenderProperty(entityRenderState,
                        COLOR_RENDER_PROPERTY_KEY,
                        ModRegistry.ITEM_FRAME_COLOR_ATTACHMENT_TYPE.get(entity));
                entityRenderState.isInvisible = true;
            }
        }
    }
}

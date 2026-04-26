package fuzs.fastitemframes.common.client.handler;

import fuzs.fastitemframes.common.FastItemFrames;
import fuzs.fastitemframes.common.client.renderer.blockentity.ItemFrameBlockRenderer;
import fuzs.fastitemframes.common.config.ClientConfig;
import fuzs.fastitemframes.common.init.ModRegistry;
import fuzs.fastitemframes.common.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ClientEventHandler {
    public static final ContextKey<Boolean> IS_BLOCK_VISIBLE_RENDER_PROPERTY_KEY = new ContextKey<>(FastItemFrames.id(
            "is_block_visible"));

    public static EventResult onAttackBlock(Player player, Level level, InteractionHand interactionHand, BlockPos pos, Direction direction) {
        if (level.isClientSide()) {
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

    public static void onExtractEntityRenderState(Entity entity, EntityRenderState entityRenderState, float partialTick) {
        if (entity instanceof ItemFrame && entityRenderState instanceof ItemFrameRenderState state) {
            if (!state.isInvisible && ModRegistry.ITEM_FRAME_COLOR_ATTACHMENT_TYPE.has(entity)) {
                BlockState blockState = ItemFrameBlockRenderer.getItemFrameBlockState(state.isGlowFrame,
                        state.mapId != null,
                        true);
                Minecraft.getInstance().blockModelResolver.update(state.frameModel,
                        blockState,
                        ItemFrameRenderer.BLOCK_DISPLAY_CONTEXT);
                state.frameModel.tintLayers().clear();
                int color = ModRegistry.ITEM_FRAME_COLOR_ATTACHMENT_TYPE.get(entity).rgb();
                state.frameModel.tintLayers().add(color);
            }

            if (FastItemFrames.CONFIG.get(ClientConfig.class).disableNameTagRendering) {
                state.nameTag = null;
            }
        }
    }
}

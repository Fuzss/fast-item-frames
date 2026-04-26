package fuzs.fastitemframes.common.client;

import fuzs.fastitemframes.common.client.handler.ClientEventHandler;
import fuzs.fastitemframes.common.client.renderer.blockentity.ItemFrameBlockRenderer;
import fuzs.fastitemframes.common.init.ModRegistry;
import fuzs.fastitemframes.common.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.common.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.common.api.client.core.v1.context.BlockColorsContext;
import fuzs.puzzleslib.common.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.common.api.client.event.v1.renderer.ExtractEntityRenderStateCallback;
import fuzs.puzzleslib.common.api.event.v1.entity.player.PlayerInteractEvents;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.state.BlockState;

public class FastItemFramesClient implements ClientModConstructor {
    private static final BlockTintSource ITEM_FRAME_BLOCK_TINT_SOURCE = new BlockTintSource() {
        @Override
        public int color(BlockState state) {
            return DyedItemColor.LEATHER_COLOR;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            if (level.getBlockEntity(pos) instanceof ItemFrameBlockEntity blockEntity
                    && blockEntity.getColor() != null) {
                return blockEntity.getColor().rgb();
            } else {
                return BlockTintSource.super.colorInWorld(state, level, pos);
            }
        }

        @Override
        public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return -1;
        }
    };

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        PlayerInteractEvents.ATTACK_BLOCK.register(ClientEventHandler::onAttackBlock);
        ExtractEntityRenderStateCallback.EVENT.register(ClientEventHandler::onExtractEntityRenderState);
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.ITEM_FRAME_BLOCK_ENTITY.value(), ItemFrameBlockRenderer::new);
    }

    @Override
    public void onRegisterBlockColorProviders(BlockColorsContext context) {
        context.registerBlockColor(ModRegistry.ITEM_FRAME_BLOCK.value(), ITEM_FRAME_BLOCK_TINT_SOURCE);
        context.registerBlockColor(ModRegistry.GLOW_ITEM_FRAME_BLOCK.value(), ITEM_FRAME_BLOCK_TINT_SOURCE);
    }
}

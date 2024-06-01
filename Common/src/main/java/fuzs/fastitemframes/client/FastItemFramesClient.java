package fuzs.fastitemframes.client;

import fuzs.fastitemframes.client.renderer.blockentity.ItemFrameRenderer;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.client.core.v1.context.ColorProvidersContext;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FastItemFramesClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        PlayerInteractEvents.ATTACK_BLOCK.register((player, level, interactionHand, pos, direction) -> {
            if (level.isClientSide) {
                if (level.getBlockState(pos).is(ModRegistry.ITEM_FRAME_BLOCK.value())) {
                    if (level.getBlockEntity(pos) instanceof ItemFrameBlockEntity blockEntity) {
                        if (!blockEntity.getItem().isEmpty()) {
                            MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
                            gameMode.destroyDelay = 5;
                            return EventResult.INTERRUPT;
                        }
                    }
                }
            }

            return EventResult.PASS;
        });
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.ITEM_FRAME_BLOCK_ENTITY.value(), ItemFrameRenderer::new);
    }

    @Override
    public void onRegisterBlockColorProviders(ColorProvidersContext<Block, BlockColor> context) {
        context.registerColorProvider((BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int tintIndex) -> {
            if (blockAndTintGetter != null && blockPos != null) {
                if (blockAndTintGetter.getBlockEntity(blockPos) instanceof ItemFrameBlockEntity blockEntity) {
                    return blockEntity.getColor();
                }
            }

            return 10511680;
        }, ModRegistry.ITEM_FRAME_BLOCK.value());
    }

    @Override
    public void onRegisterItemColorProviders(ColorProvidersContext<Item, ItemColor> context) {
        context.registerColorProvider((ItemStack stack, int tintIndex) -> tintIndex > 0 ?
                -1 :
                ((DyeableLeatherItem) stack.getItem()).getColor(stack), ModRegistry.ITEM_FRAME_ITEM.value());
    }
}

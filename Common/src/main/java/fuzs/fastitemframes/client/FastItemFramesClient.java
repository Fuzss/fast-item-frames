package fuzs.fastitemframes.client;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.client.handler.ClientEventHandler;
import fuzs.fastitemframes.client.renderer.blockentity.ItemFrameBlockRenderer;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.AdditionalModelsContext;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.client.core.v1.context.ColorProvidersContext;
import fuzs.puzzleslib.api.client.core.v1.context.ItemModelPropertiesContext;
import fuzs.puzzleslib.api.client.event.v1.renderer.ExtractRenderStateCallbackV2;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FastItemFramesClient implements ClientModConstructor {
    public static final ResourceLocation DYED_MODEL_PROPERTY = FastItemFrames.id("dyed");

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        PlayerInteractEvents.ATTACK_BLOCK.register(ClientEventHandler::onAttackBlock);
        ExtractRenderStateCallbackV2.EVENT.register(ClientEventHandler::onExtractRenderState);
    }

    @Override
    public void onRegisterAdditionalModels(AdditionalModelsContext context) {
        ItemFrameBlockRenderer.ITEM_FRAME_BLOCK_MODELS.values().forEach(context::registerAdditionalModel);
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.ITEM_FRAME_BLOCK_ENTITY.value(), ItemFrameBlockRenderer::new);
    }

    @Override
    public void onRegisterItemModelProperties(ItemModelPropertiesContext context) {
        context.registerItemProperty(DYED_MODEL_PROPERTY, (itemStack, clientLevel, livingEntity, i) -> {
            return itemStack.has(DataComponents.DYED_COLOR) ? 1.0F : 0.0F;
        }, Items.ITEM_FRAME, Items.GLOW_ITEM_FRAME);
    }

    @Override
    public void onRegisterBlockColorProviders(ColorProvidersContext<Block, BlockColor> context) {
        context.registerColorProvider((BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int tintIndex) -> {
            if (blockAndTintGetter != null && blockPos != null) {
                if (blockAndTintGetter.getBlockEntity(blockPos) instanceof ItemFrameBlockEntity blockEntity) {
                    return blockEntity.getColor().orElse(-1);
                }
            }

            return DyedItemColor.LEATHER_COLOR;
        }, ModRegistry.ITEM_FRAME_BLOCK.value(), ModRegistry.GLOW_ITEM_FRAME_BLOCK.value());
    }

    @Override
    public void onRegisterItemColorProviders(ColorProvidersContext<Item, ItemColor> context) {
        context.registerColorProvider((ItemStack itemStack, int tintIndex) -> {
            return tintIndex == 0 ? DyedItemColor.getOrDefault(itemStack, -1) : -1;
        }, Items.ITEM_FRAME, Items.GLOW_ITEM_FRAME);
    }

    public static ModelResourceLocation id(String path, String variant) {
        return new ModelResourceLocation(FastItemFrames.id(path), variant);
    }
}

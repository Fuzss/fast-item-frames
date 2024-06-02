package fuzs.fastitemframes.client;

import com.google.common.collect.ImmutableMap;
import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.client.handler.ClientItemFrameInteractionHandler;
import fuzs.fastitemframes.client.renderer.blockentity.ItemFrameBlockRenderer;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.AdditionalModelsContext;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.client.core.v1.context.ColorProvidersContext;
import fuzs.puzzleslib.api.client.core.v1.context.ItemModelPropertiesContext;
import fuzs.puzzleslib.api.client.event.v1.ModelEvents;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FastItemFramesClient implements ClientModConstructor {
    public static final ResourceLocation DYED_MODEL_PROPERTY = FastItemFrames.id("dyed");
    private static final Map<ResourceLocation, ModelResourceLocation> ITEM_FRAME_BLOCK_MODELS = ImmutableMap.<ResourceLocation, ModelResourceLocation>builder()
            .put(FastItemFrames.id("block/item_frame"), id("item_frame", "map=false"))
            .put(FastItemFrames.id("block/glow_item_frame"), id("glow_item_frame", "map=false"))
            .put(FastItemFrames.id("block/item_frame_map"), id("item_frame", "map=true"))
            .put(FastItemFrames.id("block/glow_item_frame_map"), id("glow_item_frame", "map=true"))
            .build();

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        PlayerInteractEvents.ATTACK_BLOCK.register(ClientItemFrameInteractionHandler::onAttackBlock);
        ModelEvents.ADDITIONAL_BAKED_MODEL.register((BiConsumer<ResourceLocation, BakedModel> modelAdder, Function<ResourceLocation, BakedModel> modelGetter, Supplier<ModelBaker> modelBaker) -> {
            ITEM_FRAME_BLOCK_MODELS.forEach((resourceLocation, modelResourceLocation) -> {
                modelAdder.accept(modelResourceLocation, modelGetter.apply(resourceLocation));
            });
        });
    }

    @Override
    public void onRegisterAdditionalModels(AdditionalModelsContext context) {
        ITEM_FRAME_BLOCK_MODELS.keySet().forEach(context::registerAdditionalModel);
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.ITEM_FRAME_BLOCK_ENTITY.value(), ItemFrameBlockRenderer::new);
    }

    @Override
    public void onRegisterItemModelProperties(ItemModelPropertiesContext context) {
        context.registerItemProperty(DYED_MODEL_PROPERTY, (itemStack, clientLevel, livingEntity, i) -> {
            return ((DyeableLeatherItem) itemStack.getItem()).hasCustomColor(itemStack) ? 1.0F : 0.0F;
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

            return DyeableLeatherItem.DEFAULT_LEATHER_COLOR;
        }, ModRegistry.ITEM_FRAME_BLOCK.value(), ModRegistry.GLOW_ITEM_FRAME_BLOCK.value());
    }

    @Override
    public void onRegisterItemColorProviders(ColorProvidersContext<Item, ItemColor> context) {
        context.registerColorProvider((ItemStack stack, int tintIndex) -> {
            if (tintIndex == 0 && ((DyeableLeatherItem) stack.getItem()).hasCustomColor(stack)) {
                return ((DyeableLeatherItem) stack.getItem()).getColor(stack);
            } else {
                return -1;
            }
        }, Items.ITEM_FRAME, Items.GLOW_ITEM_FRAME);
    }

    public static ModelResourceLocation id(String path, String variant) {
        return new ModelResourceLocation(FastItemFrames.MOD_ID, path, variant);
    }
}

package fuzs.fastitemframes.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.fastitemframes.client.handler.ClientEventHandler;
import fuzs.fastitemframes.client.renderer.blockentity.ItemFrameBlockRenderer;
import fuzs.puzzleslib.api.client.core.v1.ClientAbstractions;
import fuzs.puzzleslib.api.client.util.v1.RenderPropertyKey;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameRenderer.class)
abstract class ItemFrameRendererMixin<T extends ItemFrame> extends EntityRenderer<T, ItemFrameRenderState> {
    @Shadow
    @Final
    private BlockRenderDispatcher blockRenderer;

    protected ItemFrameRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(
            method = "render", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;itemStack:Lnet/minecraft/world/item/ItemStack;"
    )
    )
    public void render(ItemFrameRenderState renderState, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo callback) {
        if (RenderPropertyKey.containsRenderProperty(renderState, ClientEventHandler.COLOR_RENDER_PROPERTY_KEY)) {
            int color = RenderPropertyKey.getRenderProperty(renderState, ClientEventHandler.COLOR_RENDER_PROPERTY_KEY);
            ItemStack itemStack = renderState.itemStack;
            ModelResourceLocation modelResourceLocation = this.getFrameModelResourceLoc(renderState.isGlowFrame,
                    itemStack);
            ResourceLocation resourceLocation = ItemFrameBlockRenderer.ITEM_FRAME_BLOCK_MODELS.get(modelResourceLocation);
            BakedModel bakedModel;
            if (resourceLocation != null) {
                ModelManager modelManager = this.blockRenderer.getBlockModelShaper().getModelManager();
                bakedModel = ClientAbstractions.INSTANCE.getBakedModel(modelManager, resourceLocation);
            } else {
                ModelManager modelManager = this.blockRenderer.getBlockModelShaper().getModelManager();
                bakedModel = modelManager.getModel(modelResourceLocation);
            }
            poseStack.pushPose();
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            float red = ARGB.from8BitChannel(ARGB.red(color));
            float green = ARGB.from8BitChannel(ARGB.green(color));
            float blue = ARGB.from8BitChannel(ARGB.blue(color));
            this.blockRenderer.getModelRenderer()
                    .renderModel(poseStack.last(),
                            bufferSource.getBuffer(Sheets.solidBlockSheet()),
                            null,
                            bakedModel,
                            red,
                            green,
                            blue,
                            packedLight,
                            OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
            // moved here from later in the method when stored invisibility boolean is called upon again
            if (!itemStack.isEmpty()) {
                poseStack.translate(0.0F, 0.0F, -0.0625F);
            }
        }
    }

    @Shadow
    private ModelResourceLocation getFrameModelResourceLoc(boolean isGlowFrame, ItemStack itemStack) {
        throw new RuntimeException();
    }
}

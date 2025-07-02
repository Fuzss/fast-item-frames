package fuzs.fastitemframes.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.fastitemframes.client.handler.ClientEventHandler;
import fuzs.fastitemframes.client.renderer.blockentity.ItemFrameBlockRenderer;
import fuzs.puzzleslib.api.client.renderer.v1.RenderPropertyKey;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.block.state.BlockState;
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
            target = "Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;isInvisible:Z",
            shift = At.Shift.BEFORE,
            ordinal = 0
    )
    )
    public void render(ItemFrameRenderState renderState, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo callback) {
        // vanilla item frame rendering is prevented by setting the frame to invisible during extraction of the render state
        if (RenderPropertyKey.has(renderState, ClientEventHandler.COLOR_RENDER_PROPERTY_KEY)) {
            int color = RenderPropertyKey.get(renderState, ClientEventHandler.COLOR_RENDER_PROPERTY_KEY);
            BlockState blockState = ItemFrameBlockRenderer.getItemFrameBlockState(renderState.isGlowFrame,
                    renderState.mapId != null,
                    true);
            BlockStateModel blockStateModel = this.blockRenderer.getBlockModel(blockState);
            poseStack.pushPose();
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            float red = ARGB.redFloat(color);
            float green = ARGB.greenFloat(color);
            float blue = ARGB.blueFloat(color);
            ModelBlockRenderer.renderModel(poseStack.last(),
                    bufferSource.getBuffer(RenderType.entitySolidZOffsetForward(TextureAtlas.LOCATION_BLOCKS)),
                    blockStateModel,
                    red,
                    green,
                    blue,
                    packedLight,
                    OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
            // moved here from later in the method when stored invisibility boolean is called upon again
            if (!renderState.item.isEmpty()) {
                poseStack.translate(0.0F, 0.0F, -0.0625F);
            }
        }
    }
}

package fuzs.fastitemframes.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.fastitemframes.client.handler.ClientEventHandler;
import fuzs.fastitemframes.client.renderer.blockentity.ItemFrameBlockRenderer;
import fuzs.puzzleslib.api.client.renderer.v1.RenderStateExtraData;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
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

    @Inject(method = "submit",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;isInvisible:Z",
                     shift = At.Shift.BEFORE,
                     ordinal = 0))
    public void submit(ItemFrameRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo callback) {
        // vanilla item frame rendering is prevented by setting the frame to invisible during extraction of the render state
        if (RenderStateExtraData.has(renderState, ClientEventHandler.COLOR_RENDER_PROPERTY_KEY)) {
            int color = RenderStateExtraData.getOrDefault(renderState,
                    ClientEventHandler.COLOR_RENDER_PROPERTY_KEY,
                    -1);
            BlockState blockState = ItemFrameBlockRenderer.getItemFrameBlockState(renderState.isGlowFrame,
                    renderState.mapId != null,
                    true);
            BlockStateModel blockStateModel = this.blockRenderer.getBlockModel(blockState);
            poseStack.pushPose();
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            float red = ARGB.redFloat(color);
            float green = ARGB.greenFloat(color);
            float blue = ARGB.blueFloat(color);
            submitNodeCollector.submitBlockModel(poseStack,
                    RenderTypes.entitySolidZOffsetForward(TextureAtlas.LOCATION_BLOCKS),
                    blockStateModel,
                    red,
                    green,
                    blue,
                    renderState.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    renderState.outlineColor);
            poseStack.popPose();
            // moved here from later in the method when stored invisibility boolean is called upon again
            if (!renderState.item.isEmpty()) {
                poseStack.translate(0.0F, 0.0F, -0.0625F);
            }
        }
    }
}

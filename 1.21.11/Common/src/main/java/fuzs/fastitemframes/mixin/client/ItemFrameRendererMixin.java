package fuzs.fastitemframes.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.client.handler.ClientEventHandler;
import fuzs.fastitemframes.client.renderer.blockentity.ItemFrameBlockRenderer;
import fuzs.fastitemframes.config.ClientConfig;
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
import org.objectweb.asm.Opcodes;
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

    @ModifyExpressionValue(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
                           at = @At(value = "FIELD",
                                    target = "Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;isInvisible:Z",
                                    ordinal = 0,
                                    opcode = Opcodes.GETFIELD))
    public boolean submit(boolean isInvisible, ItemFrameRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!isInvisible && RenderStateExtraData.has(renderState, ClientEventHandler.COLOR_RENDER_PROPERTY_KEY)) {
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
            // Prevent vanilla from rendering the item frame entity block state itself by returning true for isInvisible.
            return true;
        } else {
            return isInvisible;
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;isInvisible:Z",
                     ordinal = 1,
                     opcode = Opcodes.GETFIELD))
    public void submit(ItemFrameRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo callback) {
        if (renderState.isInvisible && FastItemFrames.CONFIG.get(ClientConfig.class).disableItemOffsetWhenInvisible) {
            poseStack.translate(0.0F, 0.0F, -0.0625F);
        }
    }
}

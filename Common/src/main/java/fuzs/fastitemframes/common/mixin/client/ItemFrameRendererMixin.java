package fuzs.fastitemframes.common.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.fastitemframes.common.FastItemFrames;
import fuzs.fastitemframes.common.client.handler.ClientEventHandler;
import fuzs.fastitemframes.common.config.ClientConfig;
import fuzs.puzzleslib.common.api.client.renderer.v1.RenderStateExtraData;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameRenderer.class)
abstract class ItemFrameRendererMixin<T extends ItemFrame> extends EntityRenderer<T, ItemFrameRenderState> {

    protected ItemFrameRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;rotation:I",
                     opcode = Opcodes.GETFIELD),
            slice = @Slice(from = @At(value = "INVOKE",
                                      target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;isEmpty()Z")))
    public void submit(ItemFrameRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo callback) {
        if (renderState.isInvisible && !RenderStateExtraData.getOrDefault(renderState,
                ClientEventHandler.IS_BLOCK_VISIBLE_RENDER_PROPERTY_KEY,
                Boolean.FALSE)) {
            if (FastItemFrames.CONFIG.get(ClientConfig.class).disableItemOffsetWhenInvisible) {
                poseStack.translate(0.0F, 0.0F, -0.0625F);
            }

            double itemScaleWhenInvisible = FastItemFrames.CONFIG.get(ClientConfig.class).itemScaleWhenInvisible;
            // A config value of 0.5 is supposed to match vanilla, so it should result in a scale of 1.0.
            // A scaling of 2.0 will render the item as 16x16 pixels, so it will fill a full block face.
            if (itemScaleWhenInvisible != 0.5) {
                // Prevent z-fighting and scaling by zero.
                float scaleValue = 2.0F * (float) Math.clamp(itemScaleWhenInvisible, 0.0005, 0.9995);
                poseStack.scale(scaleValue, scaleValue, scaleValue);
            }
        }
    }
}

package fuzs.fastitemframes.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.fastitemframes.client.renderer.blockentity.ItemFrameBlockRenderer;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.puzzleslib.api.client.core.v1.ClientAbstractions;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemFrameRenderer.class)
abstract class ItemFrameRendererMixin<T extends ItemFrame> extends EntityRenderer<T> {
    @Shadow
    @Final
    private BlockRenderDispatcher blockRenderer;

    protected ItemFrameRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 0)
    public boolean render(boolean isInvisible, T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!isInvisible && ModRegistry.ITEM_FRAME_COLOR_ATTACHMENT_TYPE.has(entity)) {
            int color = ModRegistry.ITEM_FRAME_COLOR_ATTACHMENT_TYPE.get(entity);
            ItemStack itemStack = entity.getItem();
            ModelResourceLocation modelResourceLocation = this.getFrameModelResourceLoc(entity, itemStack);
            ResourceLocation resourceLocation = ItemFrameBlockRenderer.ITEM_FRAME_BLOCK_MODELS.get(modelResourceLocation);
            BakedModel bakedModel;
            if (resourceLocation != null) {
                bakedModel = ClientAbstractions.INSTANCE.getBakedModel(resourceLocation);
            } else {
                ModelManager modelManager = this.blockRenderer.getBlockModelShaper().getModelManager();
                bakedModel = modelManager.getModel(modelResourceLocation);
            }
            poseStack.pushPose();
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            float red = FastColor.ARGB32.red(color) / 255.0F;
            float green = FastColor.ARGB32.green(color) / 255.0F;
            float blue = FastColor.ARGB32.blue(color) / 255.0F;
            this.blockRenderer.getModelRenderer()
                    .renderModel(poseStack.last(), buffer.getBuffer(Sheets.solidBlockSheet()), null, bakedModel, red, green, blue, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
            // moved here from later in the method when stored invisibility boolean is called upon again
            if (!itemStack.isEmpty()) {
                poseStack.translate(0.0F, 0.0F, -0.0625F);
            }

            return true;
        }

        return isInvisible;
    }

    @Shadow
    private ModelResourceLocation getFrameModelResourceLoc(T entity, ItemStack item) {
        throw new RuntimeException();
    }
}

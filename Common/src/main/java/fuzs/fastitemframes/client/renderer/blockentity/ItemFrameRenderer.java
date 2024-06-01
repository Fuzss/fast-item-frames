package fuzs.fastitemframes.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.phys.Vec3;

public class ItemFrameRenderer implements BlockEntityRenderer<ItemFrameBlockEntity> {
    private final EntityRenderDispatcher entityRenderer;

    public ItemFrameRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.getEntityRenderer();
    }

    @Override
    public void render(ItemFrameBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!blockEntity.getItem().isEmpty()) {
            ItemFrame itemFrame = blockEntity.getEntityRepresentation();
            if (itemFrame != null) {
                EntityRenderer<? super ItemFrame> renderer = this.entityRenderer.getRenderer(itemFrame);
                poseStack.pushPose();
                poseStack.translate(0.5F, 0.25F, 0.5F);
                Direction direction = itemFrame.getDirection();
                poseStack.translate(direction.getStepX() * -0.1675F,
                        direction.getStepY() * -0.46875F,
                        direction.getStepZ() * -0.1675F
                );
                // the internal item frame entity is always set to invisible so the block itself does not render as it is handled as a block model
                // we only use the renderer for the contained item
                if (!blockEntity.isInvisible()) {
                    poseStack.translate(direction.getStepX() * 0.0625F,
                            direction.getStepY() * 0.0625F,
                            direction.getStepZ() * 0.0625F
                    );
                }
                renderer.render(itemFrame, 0.0F, partialTick, poseStack, buffer, packedLight);
                poseStack.popPose();
            }
        }
    }

    @Override
    public boolean shouldRender(ItemFrameBlockEntity blockEntity, Vec3 cameraPos) {
        ItemFrame itemFrame = blockEntity.getEntityRepresentation();
        if (itemFrame != null) {
            return itemFrame.shouldRender(cameraPos.x(), cameraPos.y(), cameraPos.z());
        } else {
            return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos);
        }
    }
}

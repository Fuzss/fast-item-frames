package fuzs.fastitemframes.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ItemFrameBlockRenderer implements BlockEntityRenderer<ItemFrameBlockEntity> {
    private final Minecraft minecraft = Minecraft.getInstance();
    private final EntityRenderDispatcher entityRenderDispatcher;

    public ItemFrameBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderDispatcher = context.getEntityRenderer();
    }

    /**
     * Glow item frame entities should really use a separate block model, as the model used for the blocks has a set
     * light emission, which the entity already applies separately though.
     */
    public static BlockState getItemFrameBlockState(boolean isGlowFrame, boolean isMapFrame, boolean isDyed) {
        Block block = getItemFrameBlock(isGlowFrame);
        return block.defaultBlockState().setValue(ItemFrameBlock.MAP, isMapFrame).setValue(ItemFrameBlock.DYED, isDyed);
    }

    private static Block getItemFrameBlock(boolean isGlowFrame) {
        return isGlowFrame ? ModRegistry.GLOW_ITEM_FRAME_BLOCK.value() : ModRegistry.ITEM_FRAME_BLOCK.value();
    }

    @Override
    public void render(ItemFrameBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 cameraPosition) {
        if (!blockEntity.getItem().isEmpty()) {
            ItemFrame itemFrame = blockEntity.getEntityRepresentation();
            if (itemFrame != null) {
                EntityRenderer<? super ItemFrame, ItemFrameRenderState> entityRenderer = (EntityRenderer<? super ItemFrame, ItemFrameRenderState>) this.entityRenderDispatcher.getRenderer(
                        itemFrame);
                poseStack.pushPose();
                poseStack.translate(0.5F, 0.25F, 0.5F);
                Direction direction = itemFrame.getDirection();
                poseStack.translate(direction.getStepX() * -0.1675F,
                        direction.getStepY() * -0.46875F,
                        direction.getStepZ() * -0.1675F);

                // the internal item frame entity is always set to invisible so the block itself does not render as it is handled as a block model
                // we only use the renderer for the contained item
                if (!blockEntity.isInvisible()) {
                    poseStack.translate(direction.getStepX() * 0.0625F,
                            direction.getStepY() * 0.0625F,
                            direction.getStepZ() * 0.0625F);
                }

                ItemFrameRenderState renderState = entityRenderer.createRenderState(itemFrame, partialTick);
                renderState.isInvisible = true;

                if (this.shouldShowName(blockEntity, itemFrame)) {
                    renderState.nameTag = entityRenderer.getNameTag(itemFrame);
                }

                if (renderState.nameTag != null) {
                    entityRenderer.renderNameTag(renderState,
                            renderState.nameTag,
                            poseStack,
                            bufferSource,
                            packedLight);
                }

                entityRenderer.render(renderState, poseStack, bufferSource, packedLight);
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

    protected boolean shouldShowName(ItemFrameBlockEntity blockEntity, ItemFrame entity) {
        if (Minecraft.renderNames() && !entity.getItem().isEmpty() && entity.getItem()
                .has(DataComponents.CUSTOM_NAME)) {
            HitResult hitResult = this.minecraft.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && blockEntity.getBlockPos()
                    .equals((((BlockHitResult) hitResult).getBlockPos()))) {
                double distanceToEntity = this.minecraft.gameRenderer.getMainCamera()
                        .getPosition()
                        .distanceToSqr(entity.position());
                double permittedDistance = entity.isDiscrete() ? 32.0 : 64.0;
                return distanceToEntity < (permittedDistance * permittedDistance);
            }
        }

        return false;
    }
}

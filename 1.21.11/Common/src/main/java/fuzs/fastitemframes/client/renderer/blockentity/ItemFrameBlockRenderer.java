package fuzs.fastitemframes.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.fastitemframes.client.handler.ClientEventHandler;
import fuzs.fastitemframes.client.renderer.blockentity.state.ItemFrameBlockRenderState;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.client.renderer.v1.RenderStateExtraData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ItemFrameBlockRenderer implements BlockEntityRenderer<ItemFrameBlockEntity, ItemFrameBlockRenderState> {
    private final EntityRenderDispatcher entityRenderDispatcher;

    public ItemFrameBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderDispatcher = context.entityRenderer();
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
    public ItemFrameBlockRenderState createRenderState() {
        return new ItemFrameBlockRenderState();
    }

    @Override
    public void extractRenderState(ItemFrameBlockEntity blockEntity, ItemFrameBlockRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity,
                renderState,
                partialTick,
                cameraPosition,
                crumblingOverlay);
        ItemFrame itemFrame = blockEntity.getEntityRepresentation();
        if (itemFrame != null) {
            EntityRenderer<? super ItemFrame, ItemFrameRenderState> entityRenderer = (EntityRenderer<? super ItemFrame, ItemFrameRenderState>) this.entityRenderDispatcher.getRenderer(
                    itemFrame);
            renderState.isInvisible = blockEntity.isInvisible();
            renderState.entityRenderState = entityRenderer.createRenderState(itemFrame, partialTick);
            RenderStateExtraData.remove(renderState.entityRenderState, ClientEventHandler.COLOR_RENDER_PROPERTY_KEY);
            renderState.entityRenderState.isInvisible = true;
            if (this.shouldShowName(blockEntity, itemFrame, cameraPosition)) {
                renderState.entityRenderState.nameTag = entityRenderer.getNameTag(itemFrame);
                renderState.entityRenderState.nameTagAttachment = itemFrame.getAttachments()
                        .getNullable(EntityAttachment.NAME_TAG, 0, itemFrame.getYRot(partialTick));
            } else {
                renderState.entityRenderState.nameTag = null;
            }
        }
    }

    protected boolean shouldShowName(ItemFrameBlockEntity blockEntity, ItemFrame itemFrame, Vec3 cameraPosition) {
        if (Minecraft.renderNames() && !itemFrame.getItem().isEmpty() && itemFrame.getItem()
                .has(DataComponents.CUSTOM_NAME)) {
            HitResult hitResult = Minecraft.getInstance().hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && blockEntity.getBlockPos()
                    .equals((((BlockHitResult) hitResult).getBlockPos()))) {
                double distanceToEntity = cameraPosition.distanceToSqr(itemFrame.position());
                double permittedDistance = itemFrame.isDiscrete() ? 32.0 : 64.0;
                return distanceToEntity < (permittedDistance * permittedDistance);
            }
        }

        return false;
    }

    @Override
    public void submit(ItemFrameBlockRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!renderState.entityRenderState.item.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.25F, 0.5F);
            Direction direction = renderState.entityRenderState.direction;
            poseStack.translate(direction.getStepX() * -0.1675F,
                    direction.getStepY() * -0.46875F,
                    direction.getStepZ() * -0.1675F);

            // the internal item frame entity is always set to invisible so the block itself does not render as it is handled as a block model
            // we only use the renderer for the contained item
            if (!renderState.isInvisible) {
                poseStack.translate(direction.getStepX() * 0.0625F,
                        direction.getStepY() * 0.0625F,
                        direction.getStepZ() * 0.0625F);
            }

            EntityRenderer<?, ? super ItemFrameRenderState> entityRenderer = this.entityRenderDispatcher.getRenderer(
                    renderState.entityRenderState);
            entityRenderer.submit(renderState.entityRenderState, poseStack, submitNodeCollector, cameraRenderState);
            poseStack.popPose();
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

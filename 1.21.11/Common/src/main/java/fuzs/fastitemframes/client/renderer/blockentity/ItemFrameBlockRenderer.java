package fuzs.fastitemframes.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.client.handler.ClientEventHandler;
import fuzs.fastitemframes.client.renderer.blockentity.state.ItemFrameBlockRenderState;
import fuzs.fastitemframes.config.ClientConfig;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.client.renderer.v1.RenderStateExtraData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ItemFrameBlockRenderer implements BlockEntityRenderer<ItemFrameBlockEntity, ItemFrameBlockRenderState> {
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemModelResolver itemModelResolver;
    private final MapRenderer mapRenderer;

    public ItemFrameBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderDispatcher = context.entityRenderer();
        this.itemModelResolver = context.itemModelResolver();
        this.mapRenderer = Minecraft.getInstance().getMapRenderer();
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
        EntityRenderer<? super ItemFrame, ItemFrameRenderState> entityRenderer = (EntityRenderer<? super ItemFrame, ItemFrameRenderState>) this.entityRenderDispatcher.renderers.get(
                blockEntity.getEntityType());
        renderState.entityRenderState = entityRenderer.createRenderState();
        this.extractItemFrameRenderState(blockEntity, renderState.entityRenderState);
        this.extractCustomItemFrameRenderState(blockEntity, renderState.entityRenderState, cameraPosition);
        renderState.entityRenderState.lightCoords = renderState.lightCoords;
        renderState.isInvisible = blockEntity.getBlockState().getValue(ItemFrameBlock.INVISIBLE);
        RenderStateExtraData.set(renderState.entityRenderState,
                ClientEventHandler.IS_BLOCK_VISIBLE_RENDER_PROPERTY_KEY,
                !renderState.isInvisible);
    }

    /**
     * @see net.minecraft.client.renderer.entity.ItemFrameRenderer#extractRenderState(ItemFrame,
     *         ItemFrameRenderState, float)
     */
    private void extractItemFrameRenderState(ItemFrameBlockEntity blockEntity, ItemFrameRenderState renderState) {
        renderState.direction = blockEntity.getBlockState().getValue(ItemFrameBlock.FACING);
        ItemStack itemStack = blockEntity.getItem();
        this.itemModelResolver.updateForTopItem(renderState.item,
                itemStack,
                ItemDisplayContext.FIXED,
                blockEntity.getLevel(),
                null,
                0);
        renderState.rotation = blockEntity.getBlockState().getValue(ItemFrameBlock.ROTATION);
        renderState.isGlowFrame = blockEntity.getEntityType() == EntityType.GLOW_ITEM_FRAME;
        renderState.mapId = null;
        if (!itemStack.isEmpty() && blockEntity.hasLevel()) {
            MapId mapId = blockEntity.getFramedMapId(itemStack);
            if (mapId != null) {
                MapItemSavedData mapItemSavedData = blockEntity.getLevel().getMapData(mapId);
                if (mapItemSavedData != null) {
                    this.mapRenderer.extractRenderState(mapId, mapItemSavedData, renderState.mapRenderState);
                    renderState.mapId = mapId;
                }
            }
        }
    }

    private void extractCustomItemFrameRenderState(ItemFrameBlockEntity blockEntity, ItemFrameRenderState renderState, Vec3 cameraPosition) {
        renderState.entityType = blockEntity.getEntityType();
        // Prevent the item frame entity renderer from rendering the block itself, we only want to use it for rendering the item.
        renderState.isInvisible = true;
        // The item frame entity shows it's name when it matches the entity picked by the crosshair, which is not possible anymore, as it's only internally stored on the block.
        // So we need to fully reevaluate the name tag ourselves.
        if (this.shouldShowName(blockEntity, cameraPosition)
                && !FastItemFrames.CONFIG.get(ClientConfig.class).disableNameTagRendering) {
            renderState.nameTag = blockEntity.getItem().getHoverName();
            renderState.nameTagAttachment = blockEntity.getEntityType()
                    .getDimensions()
                    .attachments()
                    .getNullable(EntityAttachment.NAME_TAG, 0, 0.0F);
        } else {
            renderState.nameTag = null;
        }
    }

    /**
     * @see net.minecraft.client.renderer.entity.ItemFrameRenderer#shouldShowName(ItemFrame, double)
     */
    protected boolean shouldShowName(ItemFrameBlockEntity blockEntity, Vec3 cameraPosition) {
        if (Minecraft.renderNames() && !blockEntity.getItem().isEmpty() && blockEntity.getItem()
                .has(DataComponents.CUSTOM_NAME)) {
            HitResult hitResult = Minecraft.getInstance().hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && blockEntity.getBlockPos()
                    .equals((((BlockHitResult) hitResult).getBlockPos()))) {
                double distanceToEntity = cameraPosition.distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos()));
                return distanceToEntity < 4096.0;
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

            // The internal item frame entity is always set to invisible, so the block itself does not render as it is handled as a block model.
            // We only use the renderer for the contained item.
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

    /**
     * @see ItemFrame#shouldRenderAtSqrDistance(double)
     */
    @Override
    public int getViewDistance() {
        return 16 * 64;
    }
}

package fuzs.fastitemframes.data.client;

import com.mojang.math.Quadrant;
import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import fuzs.puzzleslib.api.client.data.v2.AbstractModelProvider;
import fuzs.puzzleslib.api.client.data.v2.models.ModelLocationHelper;
import fuzs.puzzleslib.api.client.data.v2.models.ModelTemplateHelper;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.conditional.HasComponent;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ModModelProvider extends AbstractModelProvider {
    public static final TextureSlot WOOD = TextureSlot.create("wood");
    public static final ModelTemplate TEMPLATE_ITEM_FRAME_DYED = ModelTemplateHelper.createBlockModelTemplate(
            FastItemFrames.id("template_item_frame_dyed"),
            WOOD,
            TextureSlot.BACK,
            TextureSlot.PARTICLE);
    public static final ModelTemplate TEMPLATE_ITEM_FRAME_MAP_DYED = ModelTemplateHelper.createBlockModelTemplate(
            FastItemFrames.id("template_item_frame_map_dyed"),
            WOOD,
            TextureSlot.BACK,
            TextureSlot.PARTICLE);
    public static final ModelTemplate TEMPLATE_GLOW_ITEM_FRAME = ModelTemplateHelper.createBlockModelTemplate(
            FastItemFrames.id("template_glow_item_frame"),
            WOOD,
            TextureSlot.BACK,
            TextureSlot.PARTICLE);
    public static final ModelTemplate TEMPLATE_GLOW_ITEM_FRAME_MAP = ModelTemplateHelper.createBlockModelTemplate(
            FastItemFrames.id("template_glow_item_frame_map"),
            WOOD,
            TextureSlot.BACK,
            TextureSlot.PARTICLE);
    public static final ModelTemplate TEMPLATE_GLOW_ITEM_FRAME_DYED = ModelTemplateHelper.createBlockModelTemplate(
            FastItemFrames.id("template_glow_item_frame_dyed"),
            WOOD,
            TextureSlot.BACK,
            TextureSlot.PARTICLE);
    public static final ModelTemplate TEMPLATE_GLOW_ITEM_FRAME_MAP_DYED = ModelTemplateHelper.createBlockModelTemplate(
            FastItemFrames.id("template_glow_item_frame_map_dyed"),
            WOOD,
            TextureSlot.BACK,
            TextureSlot.PARTICLE);
    public static final PropertyDispatch<VariantMutator> ROTATIONS_ITEM_FRAME_FACING = PropertyDispatch.modify(
                    BlockStateProperties.FACING)
            .select(Direction.DOWN,
                    VariantMutator.X_ROT.withValue(Quadrant.R90).then(VariantMutator.Y_ROT.withValue(Quadrant.R180)))
            .select(Direction.UP,
                    VariantMutator.X_ROT.withValue(Quadrant.R270).then(VariantMutator.Y_ROT.withValue(Quadrant.R180)))
            .select(Direction.NORTH, BlockModelGenerators.NOP)
            .select(Direction.SOUTH, VariantMutator.Y_ROT.withValue(Quadrant.R180))
            .select(Direction.WEST, VariantMutator.Y_ROT.withValue(Quadrant.R270))
            .select(Direction.EAST, VariantMutator.Y_ROT.withValue(Quadrant.R90));

    public ModModelProvider(DataProviderContext context) {
        super(context);
    }

    public static TextureMapping createItemFrameMapping(Block block) {
        return createItemFrameMapping(ModelLocationHelper.getBlockTexture(block));
    }

    public static TextureMapping createItemFrameMapping(ResourceLocation resourceLocation) {
        return new TextureMapping().put(WOOD, ModelLocationHelper.getBlockTexture(Blocks.BIRCH_PLANKS))
                .put(TextureSlot.BACK, resourceLocation)
                .put(TextureSlot.PARTICLE, ModelLocationHelper.getBlockTexture(Blocks.BIRCH_PLANKS));
    }

    @Override
    public void addBlockModels(BlockModelGenerators blockModelGenerators) {
        this.createItemFrame(ModRegistry.ITEM_FRAME_BLOCK.value(), blockModelGenerators);
        this.createGlowItemFrame(ModRegistry.GLOW_ITEM_FRAME_BLOCK.value(), blockModelGenerators);
    }

    public final void createItemFrame(Block block, BlockModelGenerators blockModelGenerators) {
        ResourceLocation blockModel = ModelLocationHelper.getBlockModel(ResourceLocationHelper.withDefaultNamespace(
                ModelLocationHelper.getBlockName(block)));
        ResourceLocation mapModel = ModelLocationHelper.getBlockModel(ResourceLocationHelper.withDefaultNamespace(
                ModelLocationHelper.getBlockName(block)), "_map");
        ResourceLocation dyedBlockModel = TEMPLATE_ITEM_FRAME_DYED.createWithSuffix(block,
                "_dyed",
                createItemFrameMapping(block),
                blockModelGenerators.modelOutput);
        ResourceLocation dyedMapModel = TEMPLATE_ITEM_FRAME_MAP_DYED.createWithSuffix(block,
                "_map_dyed",
                createItemFrameMapping(block),
                blockModelGenerators.modelOutput);
        this.createItemFrame(block, blockModel, mapModel, dyedBlockModel, dyedMapModel, blockModelGenerators);
    }

    public final void createGlowItemFrame(Block block, BlockModelGenerators blockModelGenerators) {
        ResourceLocation blockModel = TEMPLATE_GLOW_ITEM_FRAME.create(block,
                createItemFrameMapping(ModelLocationHelper.getBlockTexture(ResourceLocationHelper.withDefaultNamespace(
                        ModelLocationHelper.getBlockName(block)))),
                blockModelGenerators.modelOutput);
        ResourceLocation mapModel = TEMPLATE_GLOW_ITEM_FRAME_MAP.createWithSuffix(block,
                "_map",
                createItemFrameMapping(ModelLocationHelper.getBlockModel(ResourceLocationHelper.withDefaultNamespace(
                        ModelLocationHelper.getBlockName(block)))),
                blockModelGenerators.modelOutput);
        ResourceLocation dyedBlockModel = TEMPLATE_GLOW_ITEM_FRAME_DYED.createWithSuffix(block,
                "_dyed",
                createItemFrameMapping(block),
                blockModelGenerators.modelOutput);
        ResourceLocation dyedMapModel = TEMPLATE_GLOW_ITEM_FRAME_MAP_DYED.createWithSuffix(block,
                "_map_dyed",
                createItemFrameMapping(block),
                blockModelGenerators.modelOutput);
        this.createItemFrame(block, blockModel, mapModel, dyedBlockModel, dyedMapModel, blockModelGenerators);
    }

    public final void createItemFrame(Block block, ResourceLocation blockModel, ResourceLocation mapModel, ResourceLocation dyedBlockModel, ResourceLocation dyedMapModel, BlockModelGenerators blockModelGenerators) {
        ResourceLocation invisibleModel = ModelTemplates.PARTICLE_ONLY.create(ModelLocationHelper.getBlockModel(block,
                "_invisible"), TextureMapping.particle(Blocks.BIRCH_PLANKS), blockModelGenerators.modelOutput);
        blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(ItemFrameBlock.MAP, ItemFrameBlock.DYED)
                        .select(Boolean.FALSE, Boolean.FALSE, BlockModelGenerators.plainVariant(blockModel))
                        .select(Boolean.TRUE, Boolean.FALSE, BlockModelGenerators.plainVariant(mapModel))
                        .select(Boolean.FALSE, Boolean.TRUE, BlockModelGenerators.plainVariant(dyedBlockModel))
                        .select(Boolean.TRUE, Boolean.TRUE, BlockModelGenerators.plainVariant(dyedMapModel)))
                .with(PropertyDispatch.modify(ItemFrameBlock.INVISIBLE)
                        .select(Boolean.TRUE, VariantMutator.MODEL.withValue(invisibleModel))
                        .select(Boolean.FALSE, BlockModelGenerators.NOP))
                .with(ROTATIONS_ITEM_FRAME_FACING));
    }

    @Override
    public void addItemModels(ItemModelGenerators itemModelGenerators) {
        this.generateItemFrame(Items.ITEM_FRAME, itemModelGenerators);
        this.generateItemFrame(Items.GLOW_ITEM_FRAME, itemModelGenerators);

    }

    public final void generateItemFrame(Item item, ItemModelGenerators itemModelGenerators) {
        ItemModel.Unbaked itemModel = ItemModelUtils.plainModel(ModelLocationHelper.getItemModel(item));
        ResourceLocation resourceLocation = FastItemFrames.id(ModelLocationHelper.getItemName(item));
        ItemModel.Unbaked dyedModel = ItemModelUtils.tintedModel(itemModelGenerators.generateLayeredItem(
                ModelLocationHelper.getItemModel(resourceLocation),
                ModelLocationHelper.getItemTexture(resourceLocation),
                ModelLocationHelper.getItemTexture(resourceLocation, "_overlay")), new Dye(-1));
        itemModelGenerators.generateBooleanDispatch(item,
                new HasComponent(DataComponents.DYED_COLOR, false),
                dyedModel,
                itemModel);
    }
}

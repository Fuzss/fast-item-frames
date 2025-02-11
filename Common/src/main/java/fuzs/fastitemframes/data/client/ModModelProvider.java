package fuzs.fastitemframes.data.client;

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
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.*;
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
    public static final ModelTemplate TEMPLATE_ITEM_FRAME = ModelTemplateHelper.createBlockModelTemplate(FastItemFrames.id(
            "template_item_frame"), WOOD, TextureSlot.BACK, TextureSlot.PARTICLE);
    public static final ModelTemplate TEMPLATE_ITEM_FRAME_MAP = ModelTemplateHelper.createBlockModelTemplate(
            FastItemFrames.id("template_item_frame_map"),
            WOOD,
            TextureSlot.BACK,
            TextureSlot.PARTICLE);

    public ModModelProvider(DataProviderContext context) {
        super(context);
    }

    public static TextureMapping createItemFrameMapping(Block block) {
        return new TextureMapping().put(WOOD, ModelLocationHelper.getBlockTexture(Blocks.BIRCH_PLANKS))
                .put(TextureSlot.BACK, ModelLocationHelper.getBlockTexture(block))
                .put(TextureSlot.PARTICLE, ModelLocationHelper.getBlockTexture(Blocks.BIRCH_PLANKS));
    }

    @Override
    public void addBlockModels(BlockModelGenerators blockModelGenerators) {
        this.createItemFrame(ModRegistry.ITEM_FRAME_BLOCK.value(), blockModelGenerators);
        this.createItemFrame(ModRegistry.GLOW_ITEM_FRAME_BLOCK.value(), blockModelGenerators);
    }

    public final void createItemFrame(Block block, BlockModelGenerators blockModelGenerators) {
        ResourceLocation blockModel = ModelLocationHelper.getBlockModel(ResourceLocationHelper.withDefaultNamespace(
                ModelLocationHelper.getBlockName(block)));
        ResourceLocation mapModel = ModelLocationHelper.getBlockModel(ResourceLocationHelper.withDefaultNamespace(
                ModelLocationHelper.getBlockName(block)), "_map");
        ResourceLocation dyedBlockModel = TEMPLATE_ITEM_FRAME.create(block,
                createItemFrameMapping(block),
                blockModelGenerators.modelOutput);
        ResourceLocation dyedMapModel = TEMPLATE_ITEM_FRAME_MAP.createWithSuffix(block,
                "_map",
                createItemFrameMapping(block),
                blockModelGenerators.modelOutput);
        ResourceLocation invisibleModel = ModelTemplates.PARTICLE_ONLY.create(ModelLocationHelper.getBlockModel(block,
                "_invisible"), TextureMapping.particle(Blocks.BIRCH_PLANKS), blockModelGenerators.modelOutput);
        blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(PropertyDispatch.properties(ItemFrameBlock.HAS_MAP, ItemFrameBlock.DYED)
                        .select(Boolean.FALSE,
                                Boolean.FALSE,
                                Variant.variant().with(VariantProperties.MODEL, blockModel))
                        .select(Boolean.TRUE, Boolean.FALSE, Variant.variant().with(VariantProperties.MODEL, mapModel))
                        .select(Boolean.FALSE,
                                Boolean.TRUE,
                                Variant.variant().with(VariantProperties.MODEL, dyedBlockModel))
                        .select(Boolean.TRUE,
                                Boolean.TRUE,
                                Variant.variant().with(VariantProperties.MODEL, dyedMapModel)))
                .with(createFacingDispatch())
                .with(PropertyDispatch.property(ItemFrameBlock.INVISIBLE)
                        .select(Boolean.TRUE, Variant.variant().with(VariantProperties.MODEL, invisibleModel))
                        .select(Boolean.FALSE, Variant.variant())));
    }

    public static PropertyDispatch createFacingDispatch() {
        return PropertyDispatch.property(BlockStateProperties.FACING)
                .select(Direction.DOWN,
                        Variant.variant()
                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .select(Direction.UP,
                        Variant.variant()
                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .select(Direction.NORTH, Variant.variant())
                .select(Direction.SOUTH,
                        Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .select(Direction.WEST,
                        Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                .select(Direction.EAST,
                        Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
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

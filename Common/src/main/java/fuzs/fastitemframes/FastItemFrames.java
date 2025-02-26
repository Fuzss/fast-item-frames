package fuzs.fastitemframes;

import fuzs.fastitemframes.handler.ItemFrameHandler;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.api.event.v1.entity.ServerEntityLevelEvents;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.api.event.v1.level.BlockEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastItemFrames implements ModConstructor {
    public static final String MOD_ID = "fastitemframes";
    public static final String MOD_NAME = "Fast Item Frames";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onConstructMod() {
        ModRegistry.bootstrap();
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        BlockEvents.BREAK.register(ItemFrameHandler::onBreakBlock);
        ServerEntityLevelEvents.LOAD.register(ItemFrameHandler::onEntityLoad);
        PlayerInteractEvents.USE_ENTITY.register(ItemFrameHandler::onUseEntity);
        PlayerInteractEvents.ATTACK_ENTITY.register(ItemFrameHandler::onAttackEntity);
    }

    @Override
    public void onCommonSetup() {
        CauldronInteraction.WATER.map().put(Items.ITEM_FRAME, FastItemFrames::dyedItemIteration);
        CauldronInteraction.WATER.map().put(Items.GLOW_ITEM_FRAME, FastItemFrames::dyedItemIteration);
    }

    static InteractionResult dyedItemIteration(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack) {
        if (!itemStack.is(ItemTags.DYEABLE)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        } else if (!itemStack.has(DataComponents.DYED_COLOR)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        } else {
            if (!level.isClientSide) {
                ItemStack newItemStack = itemStack.copyWithCount(1);
                newItemStack.remove(DataComponents.DYED_COLOR);
                player.setItemInHand(interactionHand,
                        ItemUtils.createFilledResult(itemStack, player, newItemStack, false));
                LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
            }

            return InteractionResult.SUCCESS;
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocationHelper.fromNamespaceAndPath(MOD_ID, path);
    }
}

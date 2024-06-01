package fuzs.fastitemframes;

import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import fuzs.puzzleslib.api.event.v1.entity.ServerEntityLevelEvents;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.api.event.v1.level.BlockEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FastItemFrames implements ModConstructor {
    public static final String MOD_ID = "fastitemframes";
    public static final String MOD_NAME = "Fast Item Frames";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        BlockEvents.BREAK.register(FastItemFrames::onBreakBlock);
        ServerEntityLevelEvents.LOAD.register(FastItemFrames::onEntityLoad);
        PlayerInteractEvents.USE_ENTITY.register(FastItemFrames::onUseEntity);
    }

    private static EventResult onBreakBlock(ServerLevel level, BlockPos pos, BlockState state, Player player, ItemStack itemInHand) {
        if (state.is(ModRegistry.ITEM_FRAME_BLOCK.value())) {
            if (level.getBlockEntity(pos) instanceof ItemFrameBlockEntity blockEntity) {
                ItemStack itemStack = blockEntity.getItem();
                if (!itemStack.isEmpty()) {
                    blockEntity.getEntityRepresentation().hurt(level.damageSources().playerAttack(player), 1.0F);
                    // TODO probably remove again as the client does not receive the new delta movement
                    List<ItemEntity> list = level.getEntitiesOfClass(ItemEntity.class,
                            new AABB(pos),
                            itemEntity -> ItemStack.isSameItemSameTags(itemStack, itemEntity.getItem())
                    );
                    if (!list.isEmpty()) {
                        ItemEntity itemEntity = list.get(0);
                        itemEntity.setDeltaMovement(Vec3.ZERO);
                        itemEntity.hasImpulse = true;
                    }
                    blockEntity.setChanged();
                    return EventResult.INTERRUPT;
                }
            }
        }

        return EventResult.PASS;
    }

    private static EventResult onEntityLoad(Entity entity, ServerLevel level) {
        if (entity instanceof ItemFrame itemFrame) {
            if (level.isEmptyBlock(entity.blockPosition())) {
                level.setBlock(entity.blockPosition(),
                        ModRegistry.ITEM_FRAME_BLOCK.value().defaultBlockState().setValue(ItemFrameBlock.FACING, itemFrame.getDirection()),
                        2
                );
                CompoundTag compoundTag = new CompoundTag();
                itemFrame.addAdditionalSaveData(compoundTag);
                if (level.getBlockEntity(entity.blockPosition()) instanceof ItemFrameBlockEntity blockEntity) {
                    blockEntity.load(compoundTag);
                }
                return EventResult.INTERRUPT;
            }
        }

        return EventResult.PASS;
    }

    private static EventResultHolder<InteractionResult> onUseEntity(Player player, Level level, InteractionHand interactionHand, Entity entity) {
        if (entity instanceof ItemFrame itemFrame) {
            if (player.isSecondaryUseActive()) {
                if (player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) {
                    itemFrame.setInvisible(!itemFrame.isInvisible());
                    itemFrame.playSound(itemFrame.getRotateItemSound(), 1.0F, 1.0F);
                    return EventResultHolder.interrupt(InteractionResult.sidedSuccess(level.isClientSide));
                } else {
                    return EventResultHolder.interrupt(InteractionResult.FAIL);
                }
            }
        }

        return EventResultHolder.pass();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}

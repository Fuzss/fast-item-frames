package fuzs.fastitemframes.client.handler;

import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.entity.ItemFrameBlockEntity;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientItemFrameInteractionHandler {

    public static EventResult onAttackBlock(Player player, Level level, InteractionHand interactionHand, BlockPos pos, Direction direction) {
        if (level.isClientSide) {
            if (level.getBlockState(pos).is(ModRegistry.ITEM_FRAMES_BLOCK_TAG)) {
                if (level.getBlockEntity(pos) instanceof ItemFrameBlockEntity blockEntity) {
                    if (!blockEntity.getItem().isEmpty()) {
                        // make sure breaking is prevented on client as well to bypass visual glitch until server sends sync packet
                        // also set default destroy delay so that not both item and frame are destroyed at once
                        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
                        gameMode.destroyDelay = 5;
                        return EventResult.INTERRUPT;
                    }
                }
            }
        }

        return EventResult.PASS;
    }
}

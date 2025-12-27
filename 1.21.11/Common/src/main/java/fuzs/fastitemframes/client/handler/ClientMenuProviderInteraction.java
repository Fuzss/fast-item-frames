package fuzs.fastitemframes.client.handler;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.config.ClientConfig;
import fuzs.fastitemframes.handler.AbstractMenuProviderInteraction;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.network.v4.NetworkingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class ClientMenuProviderInteraction extends AbstractMenuProviderInteraction {
    public static final ClientMenuProviderInteraction INSTANCE = new ClientMenuProviderInteraction();
    private boolean isProcessingInteraction;

    private ClientMenuProviderInteraction() {
        // NO-OP
    }

    public EventResult onUseInteraction(Minecraft minecraft, LocalPlayer player, InteractionHand interactionHand, HitResult hitResult) {
        if (!this.supportsCurrentEnvironment(true)) {
            return EventResult.PASS;
        }

        InteractionResult interactionResult;
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            interactionResult = this.interactWithBlock(player,
                    minecraft.level,
                    interactionHand,
                    (BlockHitResult) hitResult);
        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            interactionResult = this.interactWithEntity(player,
                    minecraft.level,
                    interactionHand,
                    ((EntityHitResult) hitResult).getEntity());
        } else {
            interactionResult = null;
        }

        // The event runs much later on Fabric compared to NeoForge: Which is when the interaction is already being processed instead of when trying to decide what to do with a hit result.
        // So, when we replace the hit result, we must also invoke the entire interaction again on Fabric, while for NeoForge the vanilla handling will come afterward anyway.
        if (ModLoaderEnvironment.INSTANCE.getModLoader().isFabricLike() && interactionResult != null) {
            if (!this.isProcessingInteraction && interactionResult.consumesAction()) {
                this.isProcessingInteraction = true;
                minecraft.startUseItem();
                this.isProcessingInteraction = false;
            }

            return EventResult.INTERRUPT;
        } else {
            return EventResult.PASS;
        }
    }

    @Override
    protected boolean supportsCurrentEnvironment(boolean isClientSide) {
        if (!FastItemFrames.CONFIG.get(ClientConfig.class).passClicksToAttachedBlock) {
            return false;
        } else if (NetworkingHelper.isModPresentServerside(FastItemFrames.MOD_ID)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected boolean isEntityPassingThroughClicks(Entity entity) {
        return FastItemFrames.CONFIG.get(ClientConfig.class).passesEntityClicksThrough.contains(entity.getType());
    }

    @Override
    protected boolean isBlockPassingThroughClicks(BlockState blockState) {
        return FastItemFrames.CONFIG.get(ClientConfig.class).passesBlockClicksThrough.contains(blockState.getBlock());
    }

    @Override
    protected boolean requiresEmptyHand() {
        return FastItemFrames.CONFIG.get(ClientConfig.class).requiresEmptyHand;
    }

    @Override
    protected InteractionResult useBlock(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult hitResult) {
        Minecraft.getInstance().hitResult = hitResult;
        return InteractionResult.SUCCESS;
    }
}

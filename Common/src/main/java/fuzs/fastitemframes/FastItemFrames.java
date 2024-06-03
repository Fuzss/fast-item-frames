package fuzs.fastitemframes;

import fuzs.fastitemframes.handler.ItemFrameHandler;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.event.v1.entity.ServerEntityLevelEvents;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.api.event.v1.level.BlockEvents;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastItemFrames implements ModConstructor {
    public static final String MOD_ID = "fastitemframes";
    public static final String MOD_NAME = "Fast Item Frames";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onConstructMod() {
        ModRegistry.touch();
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        BlockEvents.BREAK.register(ItemFrameHandler::onBreakBlock);
        ServerEntityLevelEvents.LOAD.register(ItemFrameHandler::onEntityLoad);
        PlayerInteractEvents.USE_ENTITY.register(ItemFrameHandler::onUseEntity);
        PlayerInteractEvents.ATTACK_ENTITY.register(ItemFrameHandler::onAttackEntity);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}

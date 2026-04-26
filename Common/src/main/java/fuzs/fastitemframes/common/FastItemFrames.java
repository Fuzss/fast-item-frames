package fuzs.fastitemframes.common;

import fuzs.fastitemframes.common.config.ClientConfig;
import fuzs.fastitemframes.common.config.ServerConfig;
import fuzs.fastitemframes.common.handler.ItemFrameHandler;
import fuzs.fastitemframes.common.init.ModRegistry;
import fuzs.puzzleslib.common.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.common.api.core.v1.ModConstructor;
import fuzs.puzzleslib.common.api.event.v1.entity.ServerEntityLevelEvents;
import fuzs.puzzleslib.common.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.common.api.event.v1.level.BlockEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastItemFrames implements ModConstructor {
    public static final String MOD_ID = "fastitemframes";
    public static final String MOD_NAME = "Fast Item Frames";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID)
            .client(ClientConfig.class)
            .server(ServerConfig.class);

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

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}

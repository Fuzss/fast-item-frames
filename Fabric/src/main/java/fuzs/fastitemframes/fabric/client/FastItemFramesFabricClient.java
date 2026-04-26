package fuzs.fastitemframes.fabric.client;

import fuzs.fastitemframes.common.FastItemFrames;
import fuzs.fastitemframes.common.client.FastItemFramesClient;
import fuzs.puzzleslib.common.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;

public class FastItemFramesFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(FastItemFrames.MOD_ID, FastItemFramesClient::new);
    }
}

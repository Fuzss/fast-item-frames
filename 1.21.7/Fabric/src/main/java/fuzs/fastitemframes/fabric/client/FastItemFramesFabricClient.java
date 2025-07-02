package fuzs.fastitemframes.fabric.client;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.client.FastItemFramesClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;

public class FastItemFramesFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(FastItemFrames.MOD_ID, FastItemFramesClient::new);
    }
}

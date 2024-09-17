package fuzs.fastitemframes.neoforge.client;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.client.FastItemFramesClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = FastItemFrames.MOD_ID, dist = Dist.CLIENT)
public class FastItemFramesNeoForgeClient {

    public FastItemFramesNeoForgeClient() {
        ClientModConstructor.construct(FastItemFrames.MOD_ID, FastItemFramesClient::new);
    }
}

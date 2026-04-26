package fuzs.fastitemframes.neoforge.client;

import fuzs.fastitemframes.common.FastItemFrames;
import fuzs.fastitemframes.common.client.FastItemFramesClient;
import fuzs.fastitemframes.common.data.client.ModModelProvider;
import fuzs.puzzleslib.common.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = FastItemFrames.MOD_ID, dist = Dist.CLIENT)
public class FastItemFramesNeoForgeClient {

    public FastItemFramesNeoForgeClient() {
        ClientModConstructor.construct(FastItemFrames.MOD_ID, FastItemFramesClient::new);
        DataProviderHelper.registerDataProviders(FastItemFrames.MOD_ID, ModModelProvider::new);
    }
}

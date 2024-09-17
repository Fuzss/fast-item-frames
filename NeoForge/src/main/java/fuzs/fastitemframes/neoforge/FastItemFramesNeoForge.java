package fuzs.fastitemframes.neoforge;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.data.ModBlockLootProvider;
import fuzs.fastitemframes.data.ModBlockTagProvider;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

@Mod(FastItemFrames.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FastItemFramesNeoForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(FastItemFrames.MOD_ID, FastItemFrames::new);
        DataProviderHelper.registerDataProviders(FastItemFrames.MOD_ID,
                ModBlockLootProvider::new,
                ModBlockTagProvider::new
        );
    }
}

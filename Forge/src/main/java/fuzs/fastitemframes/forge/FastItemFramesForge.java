package fuzs.fastitemframes.forge;

import fuzs.fastitemframes.FastItemFrames;
import fuzs.fastitemframes.data.ModBlockLootProvider;
import fuzs.fastitemframes.data.ModBlockTagProvider;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.data.v2.core.DataProviderHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(FastItemFrames.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FastItemFramesForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(FastItemFrames.MOD_ID, FastItemFrames::new);
        DataProviderHelper.registerDataProviders(FastItemFrames.MOD_ID,
                ModBlockLootProvider::new,
                ModBlockTagProvider::new);
    }
}

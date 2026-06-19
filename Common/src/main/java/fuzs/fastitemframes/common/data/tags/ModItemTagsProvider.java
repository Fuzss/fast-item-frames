package fuzs.fastitemframes.common.data.tags;

import fuzs.fastitemframes.common.init.ModRegistry;
import fuzs.puzzleslib.common.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.common.api.data.v2.tags.AbstractTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.ItemIds;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;

public class ModItemTagsProvider extends AbstractTagProvider<Item> {

    public ModItemTagsProvider(DataProviderContext context) {
        super(Registries.ITEM, context);
    }

    @Override
    public void addTags(HolderLookup.Provider registries) {
        this.tag(ItemTags.CAULDRON_CAN_REMOVE_DYE).add(ItemIds.ITEM_FRAME, ItemIds.GLOW_ITEM_FRAME);
        this.tag(ModRegistry.APPLIES_WAX_ITEM_TAG).add(ItemIds.HONEYCOMB);
    }
}

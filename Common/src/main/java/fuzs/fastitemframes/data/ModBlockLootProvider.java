package fuzs.fastitemframes.data;

import fuzs.fastitemframes.init.ModRegistry;
import fuzs.puzzleslib.api.data.v2.AbstractLootProviderV2;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class ModBlockLootProvider extends AbstractLootProviderV2.Blocks {

    public ModBlockLootProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addLootTables() {
        this.add(ModRegistry.ITEM_FRAME_BLOCK.value(), this::createItemFrameDrop);
        this.add(ModRegistry.GLOW_ITEM_FRAME_BLOCK.value(), this::createItemFrameDrop);
    }

    public LootTable.Builder createItemFrameDrop(Block block) {
        return LootTable.lootTable()
                .withPool(this.applyExplosionCondition(block,
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(block)
                                        .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                                .copy(DyeableLeatherItem.TAG_COLOR,
                                                        DyeableLeatherItem.TAG_DISPLAY + "." +
                                                                DyeableLeatherItem.TAG_COLOR
                                                )))
                ));
    }
}

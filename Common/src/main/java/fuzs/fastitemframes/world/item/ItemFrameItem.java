package fuzs.fastitemframes.world.item;

import fuzs.fastitemframes.init.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ItemFrameItem extends BlockItem implements DyeableLeatherItem {
    public ItemFrameItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void setColor(ItemStack stack, int color) {
        DyeableLeatherItem.super.setColor(stack, color);
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("color", color);
        setBlockEntityData(stack, ModRegistry.ITEM_FRAME_BLOCK_ENTITY.value(), compoundTag);
    }
}

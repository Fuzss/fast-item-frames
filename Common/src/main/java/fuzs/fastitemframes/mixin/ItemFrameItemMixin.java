package fuzs.fastitemframes.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemFrameItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemFrameItem.class)
abstract class ItemFrameItemMixin extends HangingEntityItem implements DyeableLeatherItem {

    public ItemFrameItemMixin(EntityType<? extends HangingEntity> type, Properties properties) {
        super(type, properties);
    }
}

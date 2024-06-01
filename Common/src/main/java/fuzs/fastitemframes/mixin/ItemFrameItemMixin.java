package fuzs.fastitemframes.mixin;

import fuzs.fastitemframes.init.ModRegistry;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemFrameItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemFrameItem.class)
abstract class ItemFrameItemMixin extends HangingEntityItem implements DyeableLeatherItem {

    public ItemFrameItemMixin(EntityType<? extends HangingEntity> type, Properties properties) {
        super(type, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // TODO find a better solution so we don't have to register a dummy item
        return ModRegistry.ITEM_FRAME_ITEM.value().useOn(context);
    }
}

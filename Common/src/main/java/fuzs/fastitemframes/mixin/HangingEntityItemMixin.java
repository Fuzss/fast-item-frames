package fuzs.fastitemframes.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fuzs.fastitemframes.init.ModRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HangingEntityItem.class)
abstract class HangingEntityItemMixin extends Item {

    public HangingEntityItemMixin(Properties properties) {
        super(properties);
    }

    @Inject(
            method = "useOn", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
            shift = At.Shift.BEFORE
    )
    )
    public void useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> callback, @Local HangingEntity hangingEntity) {
        // we need to set the color before adding the entity to the level,
        // so that our event can copy the color to the block entity if applicable
        if (hangingEntity instanceof ItemFrame itemFrame) {
            ItemStack itemInHand = context.getItemInHand();
            if (itemInHand.is(ItemTags.DYEABLE) && itemInHand.has(DataComponents.DYED_COLOR)) {
                int rgb = itemInHand.get(DataComponents.DYED_COLOR).rgb();
                ModRegistry.ITEM_FRAME_COLOR_ATTACHMENT_TYPE.set(itemFrame, rgb);
            }
        }
    }
}

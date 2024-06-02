package fuzs.fastitemframes.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fuzs.fastitemframes.init.ModRegistry;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.DyeableLeatherItem;
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
            method = "useOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    public void useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> callback, @Local HangingEntity hangingEntity) {
        // pass on color to entity
        if (this instanceof DyeableLeatherItem item) {
            ItemStack itemInHand = context.getItemInHand();
            if (item.hasCustomColor(itemInHand)) {
                ModRegistry.ITEM_FRAME_COLOR_CAPABILITY.get((ItemFrame) hangingEntity)
                        .setColor(item.getColor(itemInHand));
            }
        }
    }
}

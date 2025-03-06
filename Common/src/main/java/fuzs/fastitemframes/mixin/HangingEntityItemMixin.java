package fuzs.fastitemframes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fuzs.fastitemframes.capability.ItemFrameColorCapability;
import fuzs.fastitemframes.init.ModRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HangingEntityItem.class)
abstract class HangingEntityItemMixin extends Item {

    public HangingEntityItemMixin(Properties properties) {
        super(properties);
    }

    @WrapOperation(
            method = "useOn", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
    )
    )
    public boolean useOn(Level level, Entity entity, Operation<Boolean> operation, UseOnContext context) {

        ItemFrameColorCapability capability = null;

        // we need to set the color before adding the entity to the level,
        // so that our event can copy the color to the block entity if applicable
        if (entity instanceof ItemFrame itemFrame && this instanceof DyeableLeatherItem item) {
            ItemStack itemInHand = context.getItemInHand();
            if (item.hasCustomColor(itemInHand)) {
                capability = ModRegistry.ITEM_FRAME_COLOR_CAPABILITY.get(itemFrame);
                capability.setColor(item.getColor(itemInHand));
            }
        }

        // if the entity was added and not turned into a block sync the color to the client which has not happened before
        // as the entity was not yet added to the level and therefore hasn't been present on the client
        boolean result = operation.call(level, entity);
        if (result && capability != null) {
            capability.setChanged();
        }

        return result;
    }
}

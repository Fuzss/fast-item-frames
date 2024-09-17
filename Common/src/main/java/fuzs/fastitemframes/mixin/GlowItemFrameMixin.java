package fuzs.fastitemframes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GlowItemFrame.class)
abstract class GlowItemFrameMixin extends HangingEntity {

    protected GlowItemFrameMixin(EntityType<? extends HangingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "getFrameItemStack", at = @At("TAIL"))
    public ItemStack getFrameItemStack(ItemStack itemStack) {
        return ItemFrameBlock.setItemFrameColor(itemStack,
                ModRegistry.ITEM_FRAME_COLOR_CAPABILITY.get(ItemFrame.class.cast(this)).getColor()
        );
    }
}

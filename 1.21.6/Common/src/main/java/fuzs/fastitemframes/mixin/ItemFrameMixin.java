package fuzs.fastitemframes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fuzs.fastitemframes.init.ModRegistry;
import fuzs.fastitemframes.world.level.block.ItemFrameBlock;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemFrame.class)
abstract class ItemFrameMixin extends HangingEntity {

    protected ItemFrameMixin(EntityType<? extends HangingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "survives", at = @At("TAIL"))
    public boolean survives(boolean survives) {
        if (survives) {
            // prevent placing item frame entities inside item frame blocks with the same direction
            BlockState blockState = this.level().getBlockState(this.pos);
            return !(blockState.getBlock() instanceof ItemFrameBlock)
                    || blockState.getValue(ItemFrameBlock.FACING) != this.getDirection();
        } else {
            return false;
        }
    }

    @ModifyReturnValue(method = "getFrameItemStack", at = @At("TAIL"))
    public ItemStack getFrameItemStack(ItemStack itemStack) {
        return ItemFrameBlock.setItemFrameColor(itemStack, ModRegistry.ITEM_FRAME_COLOR_ATTACHMENT_TYPE.get(this));
    }
}

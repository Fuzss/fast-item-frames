package fuzs.fastitemframes.capability;

import fuzs.puzzleslib.api.capability.v2.data.CapabilityComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeableLeatherItem;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public class ItemFrameColorCapability implements CapabilityComponent {
    @Nullable
    private Integer color;

    public OptionalInt getColor() {
        return this.color != null ? OptionalInt.of(this.color) : OptionalInt.empty();
    }

    public void setColor(int color) {
        if (this.color == null || this.color != color) {
            this.color = color;
//            this.setChanged();
        }
    }

    @Override
    public void write(CompoundTag compoundTag) {
        if (this.color != null) {
            compoundTag.putInt(DyeableLeatherItem.TAG_COLOR, this.color);
        }
    }

    @Override
    public void read(CompoundTag compoundTag) {
        if (compoundTag.contains(DyeableLeatherItem.TAG_COLOR, Tag.TAG_INT)) {
            this.color = compoundTag.getInt(DyeableLeatherItem.TAG_COLOR);
        }
    }
}

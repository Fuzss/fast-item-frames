package fuzs.fastitemframes.common.client.renderer.blockentity.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;

public class ItemFrameBlockRenderState extends BlockEntityRenderState {
    public ItemFrameRenderState entityRenderState = new ItemFrameRenderState();
    public boolean isInvisible;
}

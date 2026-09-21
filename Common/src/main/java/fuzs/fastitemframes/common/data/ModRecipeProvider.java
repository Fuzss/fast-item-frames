package fuzs.fastitemframes.common.data;

import fuzs.puzzleslib.common.api.data.v3.recipes.AbstractRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

public class ModRecipeProvider extends AbstractRecipeProvider {

    public ModRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    @Override
    public void buildRecipes() {
        this.dyedItem(Items.ITEM_FRAME, getItemName(Items.ITEM_FRAME));
        this.dyedItem(Items.GLOW_ITEM_FRAME, getItemName(Items.ITEM_FRAME));
    }
}

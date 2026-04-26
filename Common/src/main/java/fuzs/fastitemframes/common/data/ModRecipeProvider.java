package fuzs.fastitemframes.common.data;

import fuzs.puzzleslib.common.api.data.v2.AbstractRecipeProvider;
import fuzs.puzzleslib.common.api.data.v2.core.DataProviderContext;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

public class ModRecipeProvider extends AbstractRecipeProvider {

    public ModRecipeProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addRecipes(RecipeOutput recipeOutput) {
        this.dyedItem(Items.ITEM_FRAME, getItemName(Items.ITEM_FRAME));
        this.dyedItem(Items.GLOW_ITEM_FRAME, getItemName(Items.ITEM_FRAME));
    }
}

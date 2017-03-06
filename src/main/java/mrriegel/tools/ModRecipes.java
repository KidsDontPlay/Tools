package mrriegel.tools;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModRecipes {

	public static void init() {
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.glowsand, 4), "gsg", "sgs", "gsg", 's', Blocks.SOUL_SAND, 'g', Items.GLOWSTONE_DUST);
	}

}

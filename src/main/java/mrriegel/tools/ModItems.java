package mrriegel.tools;

import mrriegel.limelib.item.CommonItem;
import mrriegel.tools.item.ItemFoodBag;
import mrriegel.tools.item.ItemPick;
import mrriegel.tools.item.ItemToolUpgrade;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {

	public static final Item pick = new ItemPick();
	public static final CommonItem foodbag = new ItemFoodBag();
	public static final CommonItem upgrade = new ItemToolUpgrade();

	public static void init() {
		GameRegistry.register(pick);
		foodbag.registerItem();
		upgrade.registerItem();
	}

	public static void initClient() {
		ModelLoader.setCustomModelResourceLocation(pick, 0, new ModelResourceLocation(pick.getRegistryName(), "inventory"));
		foodbag.initModel();
		upgrade.initModel();;
	}
}

package mrriegel.tools;

import mrriegel.limelib.item.CommonItem;
import mrriegel.tools.item.ItemAxe;
import mrriegel.tools.item.ItemAxpickvel;
import mrriegel.tools.item.ItemFoodBag;
import mrriegel.tools.item.ItemPick;
import mrriegel.tools.item.ItemShovel;
import mrriegel.tools.item.ItemToolUpgrade;
import mrriegel.tools.item.ItemTorchLauncher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {

	public static final Item pick = new ItemPick();
	public static final Item axe = new ItemAxe();
	public static final Item shovel = new ItemShovel();
	public static final Item multi = new ItemAxpickvel();
	public static final CommonItem foodbag = new ItemFoodBag();
	public static final CommonItem upgrade = new ItemToolUpgrade();
	public static final CommonItem torch = new ItemTorchLauncher();

	public static void init() {
		register(pick);
		register(axe);
		register(shovel);
		register(multi);
		foodbag.registerItem();
		upgrade.registerItem();
		torch.registerItem();
	}

	public static void initClient() {
		initModel(pick);
		initModel(axe);
		initModel(shovel);
		initModel(multi);
		foodbag.initModel();
		upgrade.initModel();
		torch.initModel();
	}

	private static void register(Item item) {
		GameRegistry.register(item);
	}

	private static void initModel(Item item) {
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
}

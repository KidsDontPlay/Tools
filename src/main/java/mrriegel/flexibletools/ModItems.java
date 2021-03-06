package mrriegel.flexibletools;

import mrriegel.flexibletools.item.GenericItemTool;
import mrriegel.flexibletools.item.ItemSword;
import mrriegel.flexibletools.item.ItemToolUpgrade;
import mrriegel.limelib.helper.RegistryHelper;
import mrriegel.limelib.item.CommonItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

public class ModItems {

	public static final Item sword = new ItemSword();
	public static final CommonItem pick = new GenericItemTool("picki", "pickaxe");
	public static final CommonItem axe = new GenericItemTool("axi", "axe");
	public static final CommonItem shovel = new GenericItemTool("shovi", "shovel");
	public static final CommonItem multi = new GenericItemTool("multi", "pickaxe", "axe", "shovel");
	public static final CommonItem upgrade_area = new ItemToolUpgrade("area");
	public static final CommonItem upgrade_transport = new ItemToolUpgrade("transport");
	public static final CommonItem upgrade_effect = new ItemToolUpgrade("effect");
	public static final CommonItem upgrade_support = new ItemToolUpgrade("support");
	public static final CommonItem upgrade_skill = new ItemToolUpgrade("skill");

	public static void init() {
		register(sword);
		pick.registerItem();
		axe.registerItem();
		shovel.registerItem();
		multi.registerItem();
		upgrade_area.registerItem();
		upgrade_transport.registerItem();
		upgrade_effect.registerItem();
		upgrade_support.registerItem();
		upgrade_skill.registerItem();
	}

	public static void initClient() {
		initModel(sword);
		pick.initModel();
		axe.initModel();
		shovel.initModel();
		multi.initModel();
		upgrade_area.initModel();
		upgrade_transport.initModel();
		upgrade_effect.initModel();
		upgrade_support.initModel();
		upgrade_skill.initModel();
	}

	private static void register(Item item) {
		RegistryHelper.register(item);
	}

	private static void initModel(Item item) {
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}

}

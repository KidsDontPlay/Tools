package mrriegel.tools;

import mrriegel.tools.item.ItemPick;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {

	public static final Item pick = new ItemPick();

	public static void init() {
		GameRegistry.register(pick);
	}

	public static void initClient() {
		ModelLoader.setCustomModelResourceLocation(pick, 0, new ModelResourceLocation(pick.getRegistryName(), "inventory"));
	}
}

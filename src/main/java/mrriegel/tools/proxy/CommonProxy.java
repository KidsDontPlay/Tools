package mrriegel.tools.proxy;

import mrriegel.limelib.datapart.DataPartRegistry;
import mrriegel.tools.ModBlocks;
import mrriegel.tools.ModItems;
import mrriegel.tools.Tools;
import mrriegel.tools.handler.ConfigHandler;
import mrriegel.tools.handler.GuiHandler;
import mrriegel.tools.item.ItemPick.Miner;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.refreshConfig(event.getSuggestedConfigurationFile());
		ModItems.init();
		ModBlocks.init();
		DataPartRegistry.register("picki", Miner.class);
	}

	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(Tools.instance, new GuiHandler());
	}

	public void postInit(FMLPostInitializationEvent event) {
	}

}

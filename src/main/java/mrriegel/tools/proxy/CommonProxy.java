package mrriegel.tools.proxy;

import mrriegel.limelib.datapart.DataPartRegistry;
import mrriegel.limelib.network.PacketHandler;
import mrriegel.tools.ModBlocks;
import mrriegel.tools.ModItems;
import mrriegel.tools.Tools;
import mrriegel.tools.handler.ConfigHandler;
import mrriegel.tools.handler.GuiHandler;
import mrriegel.tools.item.ItemPick.Miner;
import mrriegel.tools.network.MessageButton;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.refreshConfig(event.getSuggestedConfigurationFile());
		ModItems.init();
		ModBlocks.init();
		DataPartRegistry.register("picki", Miner.class);
	}

	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(Tools.instance, new GuiHandler());
		PacketHandler.registerMessage(MessageButton.class, Side.SERVER);
	}

	public void postInit(FMLPostInitializationEvent event) {
	}

}

package mrriegel.tools.proxy;

import java.util.List;
import java.util.stream.Collectors;

import mrriegel.limelib.datapart.DataPart;
import mrriegel.limelib.datapart.DataPartRegistry;
import mrriegel.limelib.network.PacketHandler;
import mrriegel.tools.ModBlocks;
import mrriegel.tools.ModItems;
import mrriegel.tools.ModRecipes;
import mrriegel.tools.Tools;
import mrriegel.tools.handler.ConfigHandler;
import mrriegel.tools.handler.GuiHandler;
import mrriegel.tools.item.GenericItemTool;
import mrriegel.tools.item.ItemPick.Miner;
import mrriegel.tools.item.ItemTorchLauncher.TorchPart;
import mrriegel.tools.network.MessageButton;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.refreshConfig(event.getSuggestedConfigurationFile());
		ModItems.init();
		ModBlocks.init();
		ModRecipes.init();
		DataPartRegistry.register("picki", Miner.class);
	}

	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(Tools.instance, new GuiHandler());
		PacketHandler.registerMessage(MessageButton.class, Side.SERVER);
		MinecraftForge.EVENT_BUS.register(CommonProxy.class);
		MinecraftForge.EVENT_BUS.register(GenericItemTool.class);
	}

	public void postInit(FMLPostInitializationEvent event) {
	}

	@SubscribeEvent
	public static void harvest(HarvestDropsEvent event) {
		if (!event.getWorld().isRemote && event.getState().getBlock() == Blocks.TORCH) {
			DataPartRegistry reg = DataPartRegistry.get(event.getWorld());
			List<DataPart> parts = reg.getParts().stream().//
					filter(p -> p instanceof TorchPart && ((TorchPart) p).torch.equals(event.getPos())).//
					collect(Collectors.toList());
			if (!parts.isEmpty()) {
				event.getDrops().clear();
				reg.removeDataPart(parts.get(0).getPos());
			}
		}
	}

}

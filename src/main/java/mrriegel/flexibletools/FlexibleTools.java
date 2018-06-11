package mrriegel.flexibletools;

import mrriegel.flexibletools.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = FlexibleTools.MODID, name = FlexibleTools.MODNAME, version = FlexibleTools.VERSION, acceptedMinecraftVersions = "[1.12,1.13)", dependencies = "required-after:limelib@[1.7.5,)")
public class FlexibleTools {
	public static final String MODID = "flexibletools";
	public static final String VERSION = "1.2.11";
	public static final String MODNAME = "Flexible Tools";

	@Instance(FlexibleTools.MODID)
	public static FlexibleTools instance;

	@SidedProxy(clientSide = "mrriegel.flexibletools.proxy.ClientProxy", serverSide = "mrriegel.flexibletools.proxy.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

}

package mrriegel.tools;

import com.google.common.reflect.Reflection;

import mrriegel.tools.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Tools.MODID, name = Tools.MODNAME, version = Tools.VERSION, dependencies = "required-after:limelib@[1.4.0,)")
public class Tools {
	public static final String MODID = "tools";
	public static final String VERSION = "1.0.0";
	public static final String MODNAME = "Tools";

	@Instance(Tools.MODID)
	public static Tools instance;

	@SidedProxy(clientSide = "mrriegel.tools.proxy.ClientProxy", serverSide = "mrriegel.tools.proxy.CommonProxy")
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

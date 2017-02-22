package mrriegel.tools;

import java.util.List;

import mrriegel.limelib.LimeLib;
import mrriegel.limelib.helper.WorldHelper;
import mrriegel.tools.proxy.CommonProxy;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

	@SubscribeEvent
	public void render(DrawBlockHighlightEvent event) {
		List<BlockPos> lis = WorldHelper.getCuboid(event.getTarget().getBlockPos(), 2);
		lis.remove(event.getTarget().getBlockPos());
		for (BlockPos p : lis) {
			event.getContext().drawSelectionBox(LimeLib.proxy.getClientPlayer(), new RayTraceResult(Vec3d.ZERO, EnumFacing.DOWN, p), 0, event.getPartialTicks());
		}
	}

}

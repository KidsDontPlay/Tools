package mrriegel.tools.proxy;

import java.util.List;
import java.util.stream.Collectors;

import mrriegel.limelib.LimeLib;
import mrriegel.limelib.helper.BlockHelper;
import mrriegel.limelib.item.CommonItemTool;
import mrriegel.limelib.network.OpenGuiMessage;
import mrriegel.limelib.network.PacketHandler;
import mrriegel.tools.ModBlocks;
import mrriegel.tools.ModItems;
import mrriegel.tools.ToolHelper;
import mrriegel.tools.Tools;
import mrriegel.tools.handler.GuiHandler;
import mrriegel.tools.item.GenericItemTool;
import mrriegel.tools.item.ITool;
import mrriegel.tools.item.ItemToolUpgrade.Upgrade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

public class ClientProxy extends CommonProxy {

	public static final KeyBinding TOOL_GUI = new KeyBinding("Open Tool GUI", Keyboard.KEY_G, Tools.MODID);

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		ModItems.initClient();
		ModBlocks.initClient();
		ClientRegistry.registerKeyBinding(TOOL_GUI);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		MinecraftForge.EVENT_BUS.register(ClientProxy.class);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
	}

	@SubscribeEvent
	public static void render(DrawBlockHighlightEvent event) {
		if (event.getTarget().typeOfHit != Type.BLOCK)
			return;
		List<BlockPos> lis = Lists.newArrayList();
		BlockPos pos = event.getTarget().getBlockPos();
		EntityPlayer player = LimeLib.proxy.getClientPlayer();
		ItemStack itemstack = player.getHeldItemMainhand();
		if (player.isSneaking() || player.isCreative() || !BlockHelper.isToolEffective(itemstack, player.world, pos, false))
			return;
		if (!(itemstack.getItem() instanceof GenericItemTool))
			return;
		if ((ToolHelper.isUpgrade(itemstack, Upgrade.ExE) || ToolHelper.isUpgrade(itemstack, Upgrade.SxS)) && player.world.getTileEntity(pos) == null) {
			int radius = ToolHelper.isUpgrade(itemstack, Upgrade.ExE) ? 1 : 2;
			EnumFacing side = event.getTarget().sideHit;
			for (int i = -radius; i <= radius; i++) {
				for (int j = -radius; j <= radius; j++) {
					if (side.getAxis() == Axis.Y)
						lis.add(new BlockPos(i + pos.getX(), pos.getY(), j + pos.getZ()));
					else if (side.getAxis() == Axis.Z)
						lis.add(new BlockPos(i + pos.getX(), j + pos.getY(), pos.getZ()));
					else if (side.getAxis() == Axis.X)
						lis.add(new BlockPos(pos.getX(), i + pos.getY(), j + pos.getZ()));
				}
			}
		}
		lis.remove(event.getTarget().getBlockPos());
		lis = lis.stream().filter(p -> BlockHelper.isToolEffective(itemstack, player.world, p, false) && player.world.getBlockState(p).getBlock().getHarvestLevel(player.world.getBlockState(p)) <= ((CommonItemTool) itemstack.getItem()).getToolMaterial().getHarvestLevel()).collect(Collectors.toList());
		for (BlockPos p : lis) {
			event.getContext().drawSelectionBox(player, new RayTraceResult(Vec3d.ZERO, EnumFacing.DOWN, p), 0, event.getPartialTicks());
		}
	}

	@SubscribeEvent
	public static void line(RenderWorldLastEvent event) {
		boolean line = true;
		if (line) {
			EntityPlayer player = LimeLib.proxy.getClientPlayer();
			Vec3d eye = player.getPositionEyes(event.getPartialTicks());
			Vec3d look = player.getLook(event.getPartialTicks());
			Vec3d v1 = eye.add(look.scale(5));
			Vec3d v2 = v1.addVector(.3, 3.1, .89);

			double playerX = TileEntityRendererDispatcher.staticPlayerX;
			double playerY = TileEntityRendererDispatcher.staticPlayerY;
			double playerZ = TileEntityRendererDispatcher.staticPlayerZ;

			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();
			//        GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			//        GL11.glDisable(GL11.GL_DEPTH_TEST);

			GL11.glTranslated(-playerX, -playerY, -playerZ);
			GL11.glColor4ub((byte) 0, (byte) 145, (byte) 229, (byte) 200);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
			//			GL11.glLineWidth(4f);
			GL11.glBegin(GL11.GL_LINES);

			GL11.glVertex3d(v1.xCoord + 0.5, v1.yCoord + 0.5, v1.zCoord + 0.5);
			GL11.glVertex3d(v2.xCoord + 0.5, v2.yCoord + 0.5, v2.zCoord + 0.5);
			GL11.glEnd();
			//        GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
			//        GL11.glEnable(GL11.GL_DEPTH_TEST);
			GlStateManager.popAttrib();
			GlStateManager.popMatrix();

		}
	}

	@SubscribeEvent
	public static void key(InputEvent.KeyInputEvent event) {
		if (Minecraft.getMinecraft().inGameHasFocus && Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof ITool && TOOL_GUI.isPressed()) {
			PacketHandler.sendToServer(new OpenGuiMessage(Tools.MODID, GuiHandler.ID.TOOL.ordinal(), null));
		}
	}
}

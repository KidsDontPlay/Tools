package mrriegel.flexibletools.proxy;

import java.util.List;
import java.util.stream.Collectors;

import mrriegel.flexibletools.FlexibleTools;
import mrriegel.flexibletools.ModBlocks;
import mrriegel.flexibletools.ModItems;
import mrriegel.flexibletools.ToolHelper;
import mrriegel.flexibletools.handler.GuiHandler;
import mrriegel.flexibletools.item.GenericItemTool;
import mrriegel.flexibletools.item.ITool;
import mrriegel.flexibletools.item.ItemToolUpgrade.QuarryPart;
import mrriegel.flexibletools.item.ItemToolUpgrade.Upgrade;
import mrriegel.limelib.LimeLib;
import mrriegel.limelib.datapart.RenderRegistry;
import mrriegel.limelib.helper.BlockHelper;
import mrriegel.limelib.item.CommonItemTool;
import mrriegel.limelib.network.OpenGuiMessage;
import mrriegel.limelib.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

public class ClientProxy extends CommonProxy {

	public static final KeyBinding TOOL_GUI = new KeyBinding("Open Tool GUI", Keyboard.KEY_G, FlexibleTools.MODID);

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
		RenderRegistry.register(QuarryPart.class, new RenderRegistry.RenderDataPart<QuarryPart>() {
			@Override
			public void render(QuarryPart part, double x, double y, double z, float partialTicks) {
				ItemStack inputStack = part.getTool();
				Minecraft mc = Minecraft.getMinecraft();
				if (inputStack == null || inputStack.isEmpty())
					return;

				GlStateManager.pushMatrix();
				GlStateManager.translate(x, y, z);
				RenderItem itemRenderer = mc.getRenderItem();
				GlStateManager.translate(0.5, 0.5, 0.5);
				EntityItem entityitem = new EntityItem(part.getWorld(), 0.0D, 0.0D, 0.0D, inputStack);
				entityitem.hoverStart = 0.0F;
				GlStateManager.pushMatrix();
				GlStateManager.pushAttrib();
				{
					GL11.glPushMatrix();
					GL11.glTranslatef(0, .5f, 0);
					GL11.glRotatef(180f, 0f, 0f, 1f);
					GL11.glRotatef(mc.player.rotationYaw, 0f, 1f, 0f);
					GL11.glRotatef(mc.player.rotationPitch, -1f, 0f, 0f);
					GL11.glScalef(0.02f, 0.02f, 0.02f);
					int durab = ToolHelper.getDurability(inputStack);
					int fuel = part.getFuel() / part.fuelPerBlock();
					int left = part.getLeft();
					String[] ar = new String[] { "Durability: " + (durab < 100 ? TextFormatting.RED : "") + durab, "Blocks left: " + (left == 0 ? TextFormatting.GREEN : "") + left, "Fuel: " + (fuel < 10 ? TextFormatting.RED : "") + fuel };
					for (int i = 0; i < ar.length; i++) {
						String finalText = ar[i];
						mc.fontRenderer.drawString(finalText, -mc.fontRenderer.getStringWidth(finalText) / 2, -i * 8, 0xFFFFFFFF);
					}
					GL11.glPopMatrix();
				}
				GlStateManager.disableLighting();
				if (!mc.isGamePaused()) {
					float rotation = (float) (4720.0 * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL);
					GlStateManager.rotate(rotation, 0.0F, 1.0F, 0);
				}
				GlStateManager.scale(0.5F, 0.5F, 0.5F);
				//				GlStateManager.pushAttrib();
				RenderHelper.enableStandardItemLighting();
				itemRenderer.renderItem(entityitem.getEntityItem(), ItemCameraTransforms.TransformType.FIXED);
				RenderHelper.disableStandardItemLighting();
				//				GlStateManager.popAttrib();

				GlStateManager.enableLighting();
				GlStateManager.popAttrib();
				GlStateManager.popMatrix();
				GlStateManager.popMatrix();
			}
		});
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
		if (!(itemstack.getItem() instanceof GenericItemTool))
			return;
		if (player.isSneaking() || player.isCreative() || !BlockHelper.isToolEffective(itemstack, player.world, pos, false))
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
		boolean line = !true;
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
		if (Minecraft.getMinecraft().inGameHasFocus && TOOL_GUI.isPressed()) {
			int hand = Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof ITool ? EnumHand.MAIN_HAND.ordinal() : Minecraft.getMinecraft().player.getHeldItemOffhand().getItem() instanceof ITool ? EnumHand.OFF_HAND.ordinal() : -1;
			//			if (hand != -1)
			if (hand == 0)
				PacketHandler.sendToServer(new OpenGuiMessage(FlexibleTools.MODID, GuiHandler.ID.TOOL.ordinal(), new BlockPos(hand, 0, 0)));
		}
	}

	@SubscribeEvent
	public static void join(EntityJoinWorldEvent event) {
		increaseReach(event.getEntity());
	}

	@SubscribeEvent
	public static void close(GuiOpenEvent event) {
		increaseReach(Minecraft.getMinecraft().player);
	}

	private static void increaseReach(Entity entity) {
		if (entity != null && entity.world.isRemote && entity instanceof EntityPlayer) {
			Minecraft mc = Minecraft.getMinecraft();
			try {
				if (!(mc.playerController instanceof Controller)) {
					NetHandlerPlayClient handler = ReflectionHelper.getPrivateValue(PlayerControllerMP.class, mc.playerController, 1);
					Controller con = new Controller(mc, handler, mc.playerController);
					if (mc.playerController.getCurrentGameType() != null)
						con.setGameType(mc.playerController.getCurrentGameType());
					mc.playerController = con;
				}
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}

	private static class Controller extends PlayerControllerMP {

		PlayerControllerMP sup;

		public Controller(Minecraft mcIn, NetHandlerPlayClient netHandler, PlayerControllerMP sup) {
			super(mcIn, netHandler);
			this.sup = sup;
		}

		@Override
		public float getBlockReachDistance() {
			EntityPlayer player = Minecraft.getMinecraft().player;
			if (player.getHeldItemMainhand().getItem() instanceof GenericItemTool && ToolHelper.isUpgrade(player.getHeldItemMainhand(), Upgrade.REACH))
				return Math.max(12f, sup.getBlockReachDistance());
			return Math.max(super.getBlockReachDistance(), sup.getBlockReachDistance());
		}

	}
}

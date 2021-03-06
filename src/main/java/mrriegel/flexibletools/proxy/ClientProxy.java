package mrriegel.flexibletools.proxy;

import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import mrriegel.flexibletools.FlexibleTools;
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class ClientProxy extends CommonProxy {

	public static final KeyBinding TOOL_GUI = new KeyBinding("Open Tool GUI", Keyboard.KEY_G, FlexibleTools.MODID);

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		ModItems.initClient();
		ClientRegistry.registerKeyBinding(TOOL_GUI);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		MinecraftForge.EVENT_BUS.register(ClientProxy.class);
		RenderRegistry.register(QuarryPart.class, new RenderRegistry.RenderDataPart<QuarryPart>() {
			int index = -1;

			private void render(QuarryPart part, double x, double y, double z) {
				boolean generate = false;
				if (index == -1) {
					index = GLAllocation.generateDisplayLists(1);
					generate = true;
				}
				if (!generate) {
					GlStateManager.callList(index);
					return;
				}
				if (generate)
					GlStateManager.glNewList(index, GL11.GL_COMPILE);
				Minecraft mc = Minecraft.getMinecraft();
				GlStateManager.pushMatrix();
				GlStateManager.translate(x + .5, y + 1, z + .5);
				GlStateManager.rotate(180f, 0f, 0f, 1f);
				GlStateManager.rotate(mc.player.rotationYaw, 0f, 1f, 0f);
				GlStateManager.rotate(mc.player.rotationPitch, -1f, 0f, 0f);
				GlStateManager.scale(0.02f, 0.02f, 0.02f);
				mc.fontRenderer.drawString("Meisterbrötchen", 0, 0, 0xFFFF0077);
				GlStateManager.popMatrix();
				if (generate)
					GlStateManager.glEndList();
			}

			@Override
			public void render(QuarryPart part, double x, double y, double z, float partialTicks) {
				if (!true) {
					render(part, x, y, z);
					return;
				}
				ItemStack inputStack = part.getTool();
				Minecraft mc = Minecraft.getMinecraft();
				if (inputStack == null || inputStack.isEmpty())
					return;
				GlStateManager.pushMatrix();
				GlStateManager.translate(x + .5, y + .5, z + .5);
				{
					GlStateManager.pushMatrix();
					GlStateManager.translate(0, .5f, 0);
					GlStateManager.rotate(180f, 0f, 0f, 1f);
					GlStateManager.rotate(mc.player.rotationYaw, 0f, 1f, 0f);
					GlStateManager.rotate(mc.player.rotationPitch, -1f, 0f, 0f);
					GlStateManager.scale(0.02f, 0.02f, 0.02f);
					int durab = ToolHelper.getDurability(inputStack);
					int fuel = part.getFuel() / part.fuelPerBlock();
					int left = part.getLeft();
					String[] ar = new String[] { "Durability: " + (durab < 100 ? TextFormatting.RED : "") + durab, "Blocks left: " + (left == 0 ? TextFormatting.GREEN : "") + left, "Fuel: " + (fuel < 10 ? TextFormatting.RED : "") + fuel };
					for (int i = 0; i < ar.length; i++) {
						String finalText = ar[i];
						mc.fontRenderer.drawString(finalText, -mc.fontRenderer.getStringWidth(finalText) / 2, -i * 8, 0xFFFFFFFF);
					}
					GlStateManager.popMatrix();
				}
				if (!mc.isGamePaused()) {
					float rotation = (float) (4720.0 * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL);
					GlStateManager.rotate(rotation, 0.0F, 1.0F, 0);
				}
				GlStateManager.scale(0.5F, 0.5F, 0.5F);
				RenderHelper.enableStandardItemLighting();
				mc.getRenderItem().renderItem(inputStack, ItemCameraTransforms.TransformType.FIXED);
				RenderHelper.disableStandardItemLighting();

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
		lis = lis.stream().filter(p -> {
			IBlockState state = player.world.getBlockState(p);
			return BlockHelper.isToolEffective(itemstack, player.world, p, false) && state.getBlock().getHarvestLevel(state) <= ((CommonItemTool) itemstack.getItem()).getToolMaterial().getHarvestLevel() && state.getSelectedBoundingBox(player.world, p) != null;
		}).collect(Collectors.toList());
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

			GL11.glVertex3d(v1.x + 0.5, v1.y + 0.5, v1.z + 0.5);
			GL11.glVertex3d(v2.x + 0.5, v2.y + 0.5, v2.z + 0.5);
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
		if (Minecraft.getMinecraft().inGameHasFocus && TOOL_GUI.isPressed() && Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof ITool) {
			PacketHandler.sendToServer(new OpenGuiMessage(FlexibleTools.MODID, GuiHandler.ID.TOOL.ordinal(), BlockPos.ORIGIN));
		}
	}

}

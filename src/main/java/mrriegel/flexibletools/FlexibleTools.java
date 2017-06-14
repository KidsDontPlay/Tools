package mrriegel.flexibletools;

import mrriegel.flexibletools.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderLivingEvent.Post;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.opengl.GL11;

@Mod(modid = FlexibleTools.MODID, name = FlexibleTools.MODNAME, version = FlexibleTools.VERSION, dependencies = "required-after:limelib@[1.5.3,)")
public class FlexibleTools {
	public static final String MODID = "flexibletools";
	public static final String VERSION = "1.1.1";
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

	@SubscribeEvent
	public void render(Post<?> event) {
		if (event.getEntity() instanceof EntityCow && !"".isEmpty()) {
			ItemStack inputStack = new ItemStack(Blocks.BEDROCK);
			Minecraft mc = Minecraft.getMinecraft();

			GlStateManager.pushMatrix();
			GlStateManager.translate(event.getX(), event.getY(), event.getZ());
			RenderItem itemRenderer = mc.getRenderItem();
			GlStateManager.translate(0.5, 0.5, 0.5);
			//			EntityItem entityitem = new EntityItem(mc.world, 0.0D, 0.0D, 0.0D, inputStack);
			//			entityitem.hoverStart = 0.0F;
			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();
			GlStateManager.disableLighting();
			{
				GL11.glPushMatrix();
				GL11.glTranslatef(0, .5f, 0);
				GL11.glRotatef(180f, 0f, 0f, 1f);
				//				System.out.println(mc.player.rotationPitch+"");
				GL11.glRotatef(event.getEntity().rotationYawHead + 180f, 0f, 1f, 0f);
				GL11.glRotatef(event.getEntity().rotationPitch, 1f, 0f, 0f);
				GL11.glScalef(0.02f, 0.02f, 0.02f);
				String finalText = "Kuh";
				mc.fontRenderer.drawString(finalText, -mc.fontRenderer.getStringWidth(finalText) / 2, -40, 0xFFFFFFFF);
				GL11.glPopMatrix();
			}
			if (!mc.isGamePaused()) {
				float rotation = (float) (4720.0 * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL);
				GlStateManager.rotate(rotation, 0.0F, 1.0F, 0);
			}
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			//				GlStateManager.pushAttrib();
			RenderHelper.enableStandardItemLighting();
			itemRenderer.renderItem(inputStack, ItemCameraTransforms.TransformType.FIXED);
			RenderHelper.disableStandardItemLighting();
			//				GlStateManager.popAttrib();

			GlStateManager.enableLighting();
			GlStateManager.popAttrib();
			GlStateManager.popMatrix();
			GlStateManager.popMatrix();
		}
	}

}

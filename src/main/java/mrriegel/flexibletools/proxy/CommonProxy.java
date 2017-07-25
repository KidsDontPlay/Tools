package mrriegel.flexibletools.proxy;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import mrriegel.flexibletools.FlexibleTools;
import mrriegel.flexibletools.ModItems;
import mrriegel.flexibletools.ModRecipes;
import mrriegel.flexibletools.ToolHelper;
import mrriegel.flexibletools.handler.ConfigHandler;
import mrriegel.flexibletools.handler.GuiHandler;
import mrriegel.flexibletools.item.GenericItemTool;
import mrriegel.flexibletools.item.ITool;
import mrriegel.flexibletools.item.ItemToolUpgrade.TorchPart;
import mrriegel.flexibletools.item.ItemToolUpgrade.Upgrade;
import mrriegel.flexibletools.network.MessageParticle;
import mrriegel.limelib.datapart.DataPart;
import mrriegel.limelib.datapart.DataPartRegistry;
import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.network.PacketHandler;
import mrriegel.limelib.util.GlobalBlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent.Close;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.refreshConfig(event.getSuggestedConfigurationFile());
		ModItems.init();
		ModRecipes.init();
	}

	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(FlexibleTools.instance, new GuiHandler());
		PacketHandler.registerMessage(MessageParticle.class, Side.CLIENT);
		MinecraftForge.EVENT_BUS.register(CommonProxy.class);
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

	@SubscribeEvent
	public static void loot(LootingLevelEvent event) {
		if (event.getDamageSource().getTrueSource() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.getDamageSource().getTrueSource();
			ItemStack tool = player.getHeldItemMainhand();
			if (tool.getItem() instanceof ITool) {
				event.setLootingLevel(ToolHelper.getUpgradeCount(tool, Upgrade.LUCK));
			}
		}
	}

	@SubscribeEvent
	public static void drop(LivingExperienceDropEvent event) {
		if (event.getAttackingPlayer() != null) {
			ItemStack tool = event.getAttackingPlayer().getHeldItemMainhand();
			if (tool.getItem() instanceof ITool) {
				event.setDroppedExperience(event.getDroppedExperience() * (ToolHelper.getUpgradeCount(tool, Upgrade.XP) + 1));
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void drop(LivingDropsEvent event) {
		if (event.getSource().getTrueSource() instanceof EntityPlayer && !event.isCanceled()) {
			EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
			ItemStack tool = player.getHeldItemMainhand();
			if (tool.getItem() instanceof ITool) {
				if (ToolHelper.isUpgrade(tool, Upgrade.MAGNET)) {
					for (EntityItem ei : event.getDrops()) {
						ei.getEntityData().setBoolean(FlexibleTools.MODID + "_magnet", true);
						ei.getEntityData().setString(FlexibleTools.MODID + "_magnet_id", player.getUniqueID().toString());
					}
				} else if (ToolHelper.isUpgrade(tool, Upgrade.TELE) && NBTStackHelper.hasTag(tool, "gpos")) {
					GlobalBlockPos gpos = GlobalBlockPos.loadGlobalPosFromNBT(NBTStackHelper.get(tool, "gpos", NBTTagCompound.class));
					IItemHandler inv = InvHelper.getItemHandler(gpos.getWorld(), gpos.getPos(), null);
					if (inv == null) {
						player.sendStatusMessage(new TextComponentString("Inventory was removed"),true);
						return;
					}
					if (!event.getDrops().isEmpty())
						PacketHandler.sendTo(new MessageParticle(new BlockPos(event.getDrops().get(0)), MessageParticle.TELE), (EntityPlayerMP) player);
					for (EntityItem s : event.getDrops())
						s.setItem(ItemHandlerHelper.insertItem(inv, s.getItem().copy(), false));
					Iterator<EntityItem> it = event.getDrops().iterator();
					while (it.hasNext()) {
						EntityItem ei = it.next();
						if (ei.getItem().isEmpty())
							it.remove();
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void spawn(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityItem && event.getEntity().getEntityData().getBoolean(FlexibleTools.MODID + "_magnet")) {
			event.getEntity().getEntityData().removeTag(FlexibleTools.MODID + "_magnet");
			EntityPlayer player = event.getWorld().getPlayerEntityByUUID(UUID.fromString(event.getEntity().getEntityData().getString(FlexibleTools.MODID + "_magnet_id")));
			if (player != null)
				event.getEntity().setPositionAndUpdate(player.posX, player.posY + .3, player.posZ);
		}
		increaseReach(event.getEntity());
	}

	@SubscribeEvent
	public static void close(Close event) {
		increaseReach(event.getEntityPlayer());
	}

	@SubscribeEvent
	public static void change(LivingEquipmentChangeEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer && ((EntityPlayer) event.getEntityLiving()).openContainer instanceof ContainerPlayer)
			increaseReach(event.getEntityLiving());
	}

	private static void increaseReach(Entity entity) {
		if (entity instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) entity;
			if (player.getHeldItemMainhand().getItem() instanceof GenericItemTool && ToolHelper.isUpgrade(player.getHeldItemMainhand(), Upgrade.REACH))
				player.interactionManager.setBlockReachDistance(Math.max(12, player.interactionManager.getBlockReachDistance()));
			else
				player.interactionManager.setBlockReachDistance(Math.max(5, player.interactionManager.getBlockReachDistance()));
		}
	}

}

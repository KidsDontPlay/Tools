package mrriegel.tools;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import mrriegel.limelib.helper.BlockHelper;
import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.helper.StackHelper;
import mrriegel.limelib.network.PacketHandler;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.limelib.util.StackWrapper;
import mrriegel.tools.item.ItemToolUpgrade.Upgrade;
import mrriegel.tools.network.MessageParticle;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ToolHelper {

	public static final ToolMaterial fin = EnumHelper.addToolMaterial("dorphy", 4, 2222, 7.5f, 2.5f, 20);

	public static List<Upgrade> getUpgrades(ItemStack stack) {
		return NBTStackHelper.getItemStackList(stack, "items").stream().map(s -> s.isEmpty() ? null : Upgrade.values()[s.getItemDamage()]).filter(u -> u != null).collect(Collectors.toList());
	}

	public static boolean isUpgrade(ItemStack stack, Upgrade upgrade) {
		return getUpgrades(stack).contains(upgrade);
	}

	public static int getUpgradeCount(ItemStack stack, Upgrade upgrade) {
		return (int) getUpgrades(stack).stream().filter(u -> u == upgrade).count();
	}

	public static void breakBlocks(ItemStack tool, EntityPlayer player, BlockPos orig, List<BlockPos> posses) {
		if (player.world.isRemote)
			return;
		NonNullList<ItemStack> drops = NonNullList.<ItemStack> create();
		float origHard = player.world.getBlockState(orig).getBlockHardness(player.world, orig);
		boolean radius = isUpgrade(tool, Upgrade.ExE) || isUpgrade(tool, Upgrade.SxS);
		for (BlockPos pos : posses) {
			if (radius && origHard + 25F < player.world.getBlockState(pos).getBlockHardness(player.world, pos))
				continue;
			if (!tool.isEmpty() && !player.world.isAirBlock(pos) && (pos.equals(orig) || (BlockHelper.isToolEffective(tool, player.world, pos, false) && BlockHelper.canToolHarvestBlock(player.world, pos, tool)))) {
				NonNullList<ItemStack> tmp = BlockHelper.breakBlock(player.world, pos, player.world.getBlockState(pos), player, isUpgrade(tool, Upgrade.SILK), getUpgradeCount(tool, Upgrade.LUCK), true, true);
				for (ItemStack s : tmp)
					StackHelper.addStack(drops, s);
				if (player.world.isAirBlock(pos)) {
					damageItem(1, player, tool);
				}
			}
		}
		if (!drops.isEmpty()) {
			boolean fire = smeltItems(player, drops);
			handleItems(player, orig, drops, posses);
			if (fire)
				for (BlockPos p : posses)
					PacketHandler.sendTo(new MessageParticle(p, MessageParticle.SMELT), (EntityPlayerMP) player);
		}
	}

	public static void breakBlock(ItemStack tool, EntityPlayer player, BlockPos orig, BlockPos pos) {
		breakBlocks(tool, player, orig, Collections.singletonList(pos));
	}

	public static void damageItem(int damage, EntityPlayer player, ItemStack tool) {
		if (tool.getItemDamage() == tool.getMaxDamage()) {
			for (ItemStack s : NBTStackHelper.getItemStackList(tool, "items")) {
				if (!s.isEmpty())
					player.world.spawnEntity(new EntityItem(player.world, player.posX, player.posY + .3, player.posZ, s));
			}
		}
		tool.damageItem(1, player);
	}

	private static void handleItems(EntityPlayer player, BlockPos orig, NonNullList<ItemStack> stacks, Iterable<BlockPos> posses) {
		ItemStack tool = player.getHeldItemMainhand();
		if (ToolHelper.isUpgrade(tool, Upgrade.TELE) && NBTStackHelper.hasTag(tool, "gpos")) {
			GlobalBlockPos gpos = GlobalBlockPos.loadGlobalPosFromNBT(NBTStackHelper.getTag(tool, "gpos"));
			IItemHandler inv = InvHelper.getItemHandler(gpos.getWorld(), gpos.getPos(), null);
			if (inv == null) {
				handleItemsDefault(player, orig, stacks, posses);
				player.sendMessage(new TextComponentString("Inventory was removed"));
				return;
			}
			NonNullList<ItemStack> set = NonNullList.create();
			for (ItemStack s : stacks)
				set.add(ItemHandlerHelper.insertItem(inv, s.copy(), false));
			PacketHandler.sendTo(new MessageParticle(orig, MessageParticle.TELE), (EntityPlayerMP) player);
			handleItemsDefault(player, orig, set, posses);
		} else
			handleItemsDefault(player, orig, stacks, posses);
	}

	private static boolean smeltItems(EntityPlayer player, NonNullList<ItemStack> stacks) {
		boolean fire = false;
		ItemStack tool = player.getHeldItemMainhand();
		if (!isUpgrade(tool, Upgrade.FIRE))
			return false;
		List<ItemStack> copy = Lists.newArrayList(stacks);
		stacks.clear();
		while (!copy.isEmpty()) {
			ItemStack s = copy.remove(0);
			ItemStack burned = FurnaceRecipes.instance().getSmeltingResult(s).copy();
			if (!burned.isEmpty() && !tool.isEmpty()) {
				fire = true;
				stacks.addAll(StackWrapper.toStackList(new StackWrapper(burned, burned.getCount() * s.getCount())));
				if (player.world.rand.nextBoolean())
					damageItem(1, player, tool);
				continue;
			} else
				stacks.add(s);

		}
		return fire;
	}

	private static void handleItemsDefault(EntityPlayer player, BlockPos orig, NonNullList<ItemStack> stacks, Iterable<BlockPos> posses) {
		Vec3d block = new Vec3d(orig.getX() + .5, orig.getY() + .3, orig.getZ() + .5);
		ItemStack tool = player.getHeldItemMainhand();
		if (!player.world.isAirBlock(orig)) {
			for (EnumFacing face : EnumFacing.VALUES) {
				if (face.getAxis().isVertical())
					face = face.getOpposite();
				if (player.world.isAirBlock(orig.offset(face))) {
					block = new Vec3d(orig.offset(face).getX() + .5, orig.offset(face).getY() + .3, orig.offset(face).getZ() + .5);
					break;
				}
			}
		}
		for (ItemStack s : stacks) {
			if (s.isEmpty())
				continue;
			EntityItem ei = new EntityItem(player.world, block.xCoord, block.yCoord, block.zCoord, s.copy());
			player.world.spawnEntity(ei);
			Vec3d vec = new Vec3d(player.posX - ei.posX, player.posY + .5 - ei.posY, player.posZ - ei.posZ).normalize().scale(0.9);
			if (isUpgrade(tool, Upgrade.MAGNET) || isUpgrade(tool, Upgrade.AUTOMINE)) {
				ei.motionX = vec.xCoord;
				ei.motionY = vec.yCoord;
				ei.motionZ = vec.zCoord;
			} else
				ei.motionY = .2;
		}
	}

	public static void damageEntity(ItemStack tool, EntityPlayer player, EntityLivingBase victim) {
		double rad = ToolHelper.isUpgrade(tool, Upgrade.ExE) ? 1.5D : ToolHelper.isUpgrade(tool, Upgrade.SxS) ? 3.0 : 0;
		List<EntityLivingBase> around = victim.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(victim.getPositionVector().addVector(-rad, -rad, -rad), victim.getPositionVector().addVector(rad, rad, rad)));
		double damage = tool.getItem().getAttributeModifiers(EntityEquipmentSlot.MAINHAND, tool).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()).iterator().next().getAmount();
		long damageModis = ToolHelper.getUpgradeCount(tool, Upgrade.DAMAGE);
		if (!around.contains(victim))
			around.add(victim);
		for (EntityLivingBase elb : around) {
			if (elb instanceof EntityPlayer)
				continue;
			if (elb != victim) {
				elb.attackEntityFrom(DamageSource.causePlayerDamage(player), (float) (((damage * victim.world.rand.nextDouble()) / 4d) * damageModis));
				if (victim.world.rand.nextBoolean())
					ToolHelper.damageItem(1, player, tool);
			}
			if (ToolHelper.isUpgrade(tool, Upgrade.POISON)) {
				if (victim.world.rand.nextDouble() < .7)
					victim.addPotionEffect(new PotionEffect(Potion.getPotionById(19), 140, 2));
			} else if (ToolHelper.isUpgrade(tool, Upgrade.FIRE)) {
				if (victim.world.rand.nextDouble() < .8)
					victim.setFire(7);
			} else if (ToolHelper.isUpgrade(tool, Upgrade.SLOW)) {
				if (victim.world.rand.nextDouble() < .6)
					victim.addPotionEffect(new PotionEffect(Potion.getPotionById(2), 140, 2));
			} else if (ToolHelper.isUpgrade(tool, Upgrade.WITHER)) {
				if (victim.world.rand.nextDouble() < .25)
					victim.addPotionEffect(new PotionEffect(Potion.getPotionById(20), 140, 2));
			} else if (ToolHelper.isUpgrade(tool, Upgrade.HEAL)) {
				player.heal(victim.world.rand.nextFloat() * 1.2F);
			}
		}
	}

}

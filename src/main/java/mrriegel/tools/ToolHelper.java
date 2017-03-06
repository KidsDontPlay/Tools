package mrriegel.tools;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import mrriegel.limelib.helper.BlockHelper;
import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.helper.StackHelper;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.limelib.util.StackWrapper;
import mrriegel.tools.item.ITool;
import mrriegel.tools.item.ItemToolUpgrade.Upgrade;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ToolHelper {

	public static final ToolMaterial fin = EnumHelper.addToolMaterial("dorphy", 4, 2222, 7.5f, 2.5f, 20);

	public static Set<Upgrade> getUpgrades(ItemStack stack) {
//		if (!(stack.getItem() instanceof ITool))
//			return Collections.EMPTY_SET;
		return NBTStackHelper.getItemStackList(stack, "items").stream().map(s -> s.isEmpty() ? null : Upgrade.values()[s.getItemDamage()]).filter(u -> u != null).collect(Collectors.toSet());
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
		if (!drops.isEmpty())
			handleItems(player, orig, drops);
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

	public static void breakBlock(ItemStack tool, EntityPlayer player, BlockPos orig, BlockPos pos) {
		breakBlocks(tool, player, orig, Collections.singletonList(pos));
	}

	private static void handleItems(EntityPlayer player, BlockPos pos, NonNullList<ItemStack> stacks) {
		if (player.world.isRemote)
			return;
		ItemStack tool = player.getHeldItemMainhand();
		if (ToolHelper.isUpgrade(tool, Upgrade.TELE) && NBTStackHelper.hasTag(tool, "gpos")) {
			GlobalBlockPos gpos = GlobalBlockPos.loadGlobalPosFromNBT(NBTStackHelper.getTag(tool, "gpos"));
			IItemHandler inv = InvHelper.getItemHandler(gpos.getWorld(), gpos.getPos(), null);
			if (inv == null) {
				handleItemsDefault(player, pos, stacks);
				player.sendMessage(new TextComponentString("Inventory was removed"));
				return;
			}
			NonNullList<ItemStack> set = NonNullList.create();
			for (ItemStack s : stacks)
				set.add(ItemHandlerHelper.insertItem(inv, s.copy(), false));
			handleItemsDefault(player, pos, set);
		} else
			handleItemsDefault(player, pos, stacks);
	}

	private static void handleItemsDefault(EntityPlayer player, BlockPos pos, NonNullList<ItemStack> stacks) {
		Vec3d block = new Vec3d(pos.getX() + .5, pos.getY() + .3, pos.getZ() + .5);
		ItemStack tool = player.getHeldItemMainhand();
		if (!player.world.isAirBlock(pos)) {
			for (EnumFacing face : EnumFacing.VALUES) {
				if (face.getAxis().isVertical())
					face = face.getOpposite();
				if (player.world.isAirBlock(pos.offset(face))) {
					block = new Vec3d(pos.offset(face).getX() + .5, pos.offset(face).getY() + .3, pos.offset(face).getZ() + .5);
					break;
				}
			}
		}
		while (!stacks.isEmpty()) {
			ItemStack s = stacks.remove(0);
			if (isUpgrade(tool, Upgrade.FIRE)) {
				ItemStack burned = FurnaceRecipes.instance().getSmeltingResult(s).copy();
				if (!burned.isEmpty() && !tool.isEmpty()) {
					stacks.addAll(StackWrapper.toStackList(new StackWrapper(burned, burned.getCount() * s.getCount())));
					s = ItemStack.EMPTY;
					damageItem(1, player, tool);
					continue;
				}
			}
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

}

package mrriegel.tools.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;

import mrriegel.limelib.LimeLib;
import mrriegel.limelib.helper.BlockHelper;
import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.item.CommonItemTool;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.tools.ToolHelper;
import mrriegel.tools.item.ItemToolUpgrade.Upgrade;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public abstract class GenericItemTool extends CommonItemTool implements ITool {

	protected GenericItemTool(String name, String... classes) {
		super(name, ToolHelper.newMat, classes);
		setMaxDamage(getMaxDamage() * toolClasses.size());
	}

	@Override
	public int getItemEnchantability(ItemStack stack) {
		return 0;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (!GuiScreen.isShiftKeyDown())
			tooltip.add(TextFormatting.ITALIC + "Hold SHIFT to see upgrades");
		else
			for (Upgrade u : Upgrade.values()) {
				int count = ToolHelper.getUpgradeCount(stack, u);
				if (count > 0) {
					tooltip.add(TextFormatting.BLUE.toString() + WordUtils.capitalize(u.toString().toLowerCase()) + " " + count);
				}
			}
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (!worldIn.isRemote && worldIn.rand.nextInt(140) == 0 && ToolHelper.isUpgrade(stack, Upgrade.REPAIR) && entityIn instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) entityIn;
			if (stack.getItemDamage() > 0) {
				stack.damageItem(-1, player);
			}
		}
	}

	@Override
	protected float getDigSpeed(ItemStack stack, float efficiencyOnProperMaterial) {
		float s = super.getDigSpeed(stack, efficiencyOnProperMaterial);
		for (Upgrade u : ToolHelper.getUpgrades(stack))
			s *= u.speedMultiplier;
		return s;
	}

	@Override
	protected double getAttackDamage(ItemStack stack) {
		double d = super.getAttackDamage(stack);
		for (Upgrade u : ToolHelper.getUpgrades(stack))
			if (u == Upgrade.DAMAGE)
				d += 2.5;
		return d;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack tool, BlockPos pos, EntityPlayer player) {
		if (player.isCreative())
			return false;
		IBlockState state = player.world.getBlockState(pos);
		if (!BlockHelper.canToolHarvestBlock(player.world, pos, tool))
			return false;
		if (!BlockHelper.isToolEffective(tool, player.world, pos, false) || player.isSneaking()) {
			ToolHelper.breakBlock(tool, player, pos, pos);
			return true;
		}
		if ((ToolHelper.isUpgrade(tool, Upgrade.ExE) || ToolHelper.isUpgrade(tool, Upgrade.SxS)) && player.world.getTileEntity(pos) == null) {
			int radius = ToolHelper.isUpgrade(tool, Upgrade.ExE) ? 1 : 2;
			EnumFacing side = ForgeHooks.rayTraceEyes(player, LimeLib.proxy.getReachDistance(player)).sideHit;
			NonNullList<BlockPos> lis = NonNullList.create();
			switch (side.getAxis()) {
			case X:
				Iterables.addAll(lis, BlockPos.getAllInBox(pos.north(radius).down(radius), pos.north(-radius).down(-radius)));
				break;
			case Y:
				Iterables.addAll(lis, BlockPos.getAllInBox(pos.east(radius).north(radius), pos.east(-radius).north(-radius)));
				break;
			case Z:
				Iterables.addAll(lis, BlockPos.getAllInBox(pos.east(radius).down(radius), pos.east(-radius).down(-radius)));
				break;
			}
			ToolHelper.breakBlocks(tool, player, pos, lis);
			if (radius == 2)
				player.addExhaustion(3F);
			return true;
		} else if (ToolHelper.isUpgrade(tool, Upgrade.VEIN) && player.world.getTileEntity(pos) == null) {
			LinkedList<BlockPos> research = Lists.newLinkedList(Collections.singleton(pos));
			Set<BlockPos> done = Sets.newHashSet();
			Block orig = state.getBlock();
			main: while (!research.isEmpty()) {
				BlockPos current = research.poll();
				List<BlockPos> poss = new ArrayList<BlockPos>(27);
				Iterables.addAll(poss, BlockPos.getAllInBox(current.north().east().up(), current.south().west().down()));
				Collections.shuffle(poss);
				for (BlockPos searchPos : poss) {
					if (!player.world.isBlockLoaded(searchPos))
						continue;
					if (player.world.getBlockState(searchPos).getBlock().getUnlocalizedName().equals(orig.getUnlocalizedName())) {
						if (!done.contains(searchPos)) {
							done.add(searchPos);
							research.add(searchPos);
							if (done.size() >= 50)
								break main;
						}
					}
				}
			}
			List<BlockPos> lis = Lists.newLinkedList(done);
			lis.add(0, pos);
			ToolHelper.breakBlocks(tool, player, pos, lis);
			return true;
		} else if (ToolHelper.isUpgrade(tool, Upgrade.AUTOMINE) && !state.getMaterial().isToolNotRequired()) {
			BlockPos ore = null;
			main: for (int i = cache.containsKey(pos) ? cache.get(pos) : 0; i < pos.getY(); i++) {
				for (BlockPos bl : BlockPos.getAllInBox(pos.north(2).west(2).down(i), pos.south(2).east(2).down(i))) {
					if (BlockHelper.isOre(player.world, bl) && BlockHelper.isToolEffective(tool, player.world, bl, false) && player.world.getBlockState(bl).getBlock().getHarvestLevel(player.world.getBlockState(bl)) <= toolMaterial.getHarvestLevel()) {
						ore = bl;
						cache.put(pos, i);
						break main;
					}
				}
			}
			if (ore != null) {
				ToolHelper.breakBlock(tool, player, pos, ore);
				return true;
			} else
				cache.remove(pos);
		}
		ToolHelper.breakBlock(tool, player, pos, pos);
		return true;
	}

	private Map<BlockPos, Integer> cache = Maps.newHashMap();

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (ToolHelper.performSkill(playerIn.getHeldItem(handIn), playerIn, handIn, playerIn.isSneaking())) {
			return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (InvHelper.hasItemHandler(worldIn, pos, null) && player.isSneaking()) {
			NBTTagCompound nbt = new NBTTagCompound();
			new GlobalBlockPos(pos, worldIn).writeToNBT(nbt);
			NBTStackHelper.setTag(player.getHeldItem(hand), "gpos", nbt);
			if (!worldIn.isRemote)
				player.sendMessage(new TextComponentString("Bound to " + worldIn.getBlockState(pos).getBlock().getLocalizedName()));
			return EnumActionResult.SUCCESS;
		}
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		if (attacker instanceof EntityPlayer) {
			ToolHelper.damageEntity(stack, (EntityPlayer) attacker, target);
		}
		return super.hitEntity(stack, target, attacker);
	}

}

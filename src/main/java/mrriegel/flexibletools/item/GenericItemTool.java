package mrriegel.flexibletools.item;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import mrriegel.flexibletools.CTab;
import mrriegel.flexibletools.ToolHelper;
import mrriegel.flexibletools.item.ItemToolUpgrade.Upgrade;
import mrriegel.limelib.LimeLib;
import mrriegel.limelib.helper.BlockHelper;
import mrriegel.limelib.helper.EnergyHelper;
import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.item.CommonItemTool;
import mrriegel.limelib.util.GlobalBlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class GenericItemTool extends CommonItemTool implements ITool {

	public GenericItemTool(String name, String... classes) {
		super(name, ToolHelper.newMat, classes);
		setMaxDamage(getMaxDamage() * toolClasses.size());
		setCreativeTab(CTab.TAB);
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
	public boolean showDurabilityBar(ItemStack stack) {
		return super.showDurabilityBar(stack) || ToolHelper.isUpgrade(stack, Upgrade.ENERGY);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		if (ToolHelper.isUpgrade(stack, Upgrade.ENERGY))
			return 1. - ((double) EnergyHelper.getEnergy(stack, null) / (double) EnergyHelper.getMaxEnergy(stack, null));
		return super.getDurabilityForDisplay(stack);
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		if (ToolHelper.isUpgrade(stack, Upgrade.ENERGY))
			return 0x338a9e;
		return super.getRGBDurabilityForDisplay(stack);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new CP(stack);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		addInfo(stack, tooltip);
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		int count = ToolHelper.getUpgradeCount(stack, Upgrade.REPAIR);
		if (ToolHelper.isUpgrade(stack, Upgrade.REPAIR) && !worldIn.isRemote && worldIn.rand.nextInt(140 / count) == 0 && entityIn instanceof EntityPlayer) {
			ToolHelper.damageItem(-1, (EntityPlayer) entityIn, stack, null);
		}
	}

	@Override
	protected float getDigSpeed(ItemStack stack, float efficiencyOnProperMaterial) {
		float s = super.getDigSpeed(stack, efficiencyOnProperMaterial);
		if (ToolHelper.isUpgrade(stack, Upgrade.AUTOMINE)) {
			return s / 5f;
		}
		for (Upgrade u : ToolHelper.getUpgrades(stack))
			s *= u.speedMultiplier;
		int count = ToolHelper.getUpgradeCount(stack, Upgrade.SPEED);
		if (count == 4)
			s /= Upgrade.SPEED.speedMultiplier;
		return s;
	}

	@Override
	protected double getAttackDamage(ItemStack stack) {
		double d = super.getAttackDamage(stack);
		d += 2.5 * ToolHelper.getUpgradeCount(stack, Upgrade.DAMAGE);
		return d;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack tool, BlockPos pos, EntityPlayer player) {
		if (player.isCreative())
			return false;
		//		if(player.world.isRemote)return true;
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
			List<BlockPos> posses = null;
			switch (side.getAxis()) {
			case X:
				posses = Lists.newArrayList(BlockPos.getAllInBox(pos.north(radius).down(radius), pos.north(-radius).down(-radius)));
				break;
			case Y:
				posses = Lists.newArrayList(BlockPos.getAllInBox(pos.east(radius).north(radius), pos.east(-radius).north(-radius)));
				break;
			case Z:
				posses = Lists.newArrayList(BlockPos.getAllInBox(pos.east(radius).down(radius), pos.east(-radius).down(-radius)));
				break;
			}
			ToolHelper.breakBlocks(tool, player, pos, posses);
			return true;
		} else if (ToolHelper.isUpgrade(tool, Upgrade.VEIN)) {
			LinkedList<BlockPos> research = Lists.newLinkedList(Collections.singleton(pos));
			Set<BlockPos> done = Sets.newHashSet(), researched = Sets.newHashSet();
			Block orig = state.getBlock();
			main: while (!research.isEmpty()) {
				BlockPos current = research.poll();
				researched.add(current);
				List<BlockPos> poss = Lists.newArrayList(BlockPos.getAllInBox(current.add(1, 1, 1), current.add(-1, -1, -1)));
				Collections.shuffle(poss);
				for (BlockPos searchPos : poss) {
					if (player.world.getBlockState(searchPos).getBlock().getUnlocalizedName().equals(orig.getUnlocalizedName())) {
						if (!done.contains(searchPos)) {
							done.add(searchPos);
							if (!researched.contains(searchPos))
								research.add(searchPos);
							if (done.size() >= 100)
								break main;
						}
					}
				}
			}
			done.remove(pos);
			List<BlockPos> lis = Lists.newLinkedList(done);
			lis.add(0, pos);
			ToolHelper.breakBlocks(tool, player, pos, lis);
			return true;
		} else if (ToolHelper.isUpgrade(tool, Upgrade.AUTOMINE) && !state.getMaterial().isToolNotRequired()) {
			BlockPos ore = null;
			GlobalBlockPos gpos = new GlobalBlockPos(pos, player.world);
			if (cache == null)
				cache = Maps.newHashMap();
			main: for (int i = cache.containsKey(gpos) ? cache.get(gpos) : 0; i < pos.getY(); i++) {
				for (BlockPos bl : BlockPos.getAllInBox(pos.north(2).west(2).down(i), pos.south(2).east(2).down(i))) {
					if (BlockHelper.isOre(player.world, bl) && BlockHelper.isToolEffective(tool, player.world, bl, false) && player.world.getBlockState(bl).getBlock().getHarvestLevel(player.world.getBlockState(bl)) <= toolMaterial.getHarvestLevel()) {
						ore = bl;
						cache.put(gpos, i);
						break main;
					}
				}
			}
			if (ore != null) {
				ToolHelper.breakBlock(tool, player, pos, ore);
				ToolHelper.damageItem(2, player, tool, false);
				return true;
			} else {
				cache.remove(gpos);
				if (!player.world.isRemote)
					player.sendStatusMessage(new TextComponentString("No ores in this area."),true);
			}
		}
		ToolHelper.breakBlock(tool, player, pos, pos);
		return true;
	}

	private Map<GlobalBlockPos, Integer> cache = null;

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (ToolHelper.performSkill(playerIn.getHeldItem(handIn), playerIn, handIn)) {
			return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (InvHelper.hasItemHandler(worldIn, pos, null) && player.isSneaking()) {
			NBTTagCompound nbt = new NBTTagCompound();
			new GlobalBlockPos(pos, worldIn).writeToNBT(nbt);
			NBTStackHelper.set(player.getHeldItem(hand), "gpos", nbt);
			if (!worldIn.isRemote)
				player.sendStatusMessage(new TextComponentString("Bound to " + worldIn.getBlockState(pos).getBlock().getLocalizedName()),true);
			return EnumActionResult.SUCCESS;
		}
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		if (attacker instanceof EntityPlayer) {
			ToolHelper.damageEntity(stack, (EntityPlayer) attacker, target);
		}
		return true;
	}

}

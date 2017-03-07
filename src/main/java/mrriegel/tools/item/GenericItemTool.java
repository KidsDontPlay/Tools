package mrriegel.tools.item;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import mrriegel.limelib.helper.BlockHelper;
import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.item.CommonItemTool;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.tools.ToolHelper;
import mrriegel.tools.Tools;
import mrriegel.tools.handler.GuiHandler.ID;
import mrriegel.tools.item.ItemToolUpgrade.Upgrade;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.EnumHelper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public abstract class GenericItemTool extends CommonItemTool implements ITool{

	public static final ToolMaterial fin = EnumHelper.addToolMaterial("dorphy", 4, 2048, 7.5f, 2.5f, 20);

	protected GenericItemTool(String name, ToolMaterial materialIn, String... classes) {
		super(name, materialIn, classes);
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
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (!worldIn.isRemote && worldIn.rand.nextInt(120) == 0 && ToolHelper.isUpgrade(stack, Upgrade.REPAIR) && entityIn instanceof EntityPlayerMP) {
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
			EnumFacing side = ForgeHooks.rayTraceEyes(player, 5d).sideHit;
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
				List<EnumFacing> es = Lists.newArrayList(EnumFacing.VALUES);
				Collections.shuffle(es);
				List<BlockPos> poss = es.stream().map(f -> current.offset(f)).collect(Collectors.toList());
				Iterables.addAll(poss, BlockPos.getAllInBox(current.north().east().up(), current.south().west().down()));
				poss = poss.stream().distinct().collect(Collectors.toList());
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
		if (playerIn.isSneaking()) {
			if (!worldIn.isRemote)
				playerIn.openGui(Tools.instance, ID.TOOL.ordinal(), worldIn, handIn.ordinal(), 0, 0);
			return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
		} else {
			//TODO perfom skill
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (InvHelper.hasItemHandler(worldIn, pos, null)) {
			NBTTagCompound nbt = new NBTTagCompound();
			new GlobalBlockPos(pos, worldIn).writeToNBT(nbt);
			NBTStackHelper.setTag(player.getHeldItem(hand), "gpos", nbt);
			if (!worldIn.isRemote)
				player.sendMessage(new TextComponentString("Bind to " + worldIn.getBlockState(pos).getBlock().getLocalizedName()));
			return EnumActionResult.SUCCESS;
		}
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		if (attacker instanceof EntityPlayer) {
			double rad = ToolHelper.isUpgrade(stack, Upgrade.ExE) ? 1.5D : ToolHelper.isUpgrade(stack, Upgrade.SxS) ? 3.0 : 0;
			List<EntityLivingBase> around = target.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(target.getPositionVector().addVector(-rad, -rad, -rad), target.getPositionVector().addVector(rad, rad, rad)));
			double damage = getAttributeModifiers(EntityEquipmentSlot.MAINHAND, stack).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()).iterator().next().getAmount();
			long damageModis = ToolHelper.getUpgradeCount(stack, Upgrade.DAMAGE);
			if (!around.contains(target))
				around.add(target);
			for (EntityLivingBase elb : around) {
				if (elb instanceof EntityPlayer)
					continue;
				if (elb != target) {
					elb.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), (float) (((damage * target.world.rand.nextDouble()) / 4d) * damageModis));
					if (target.world.rand.nextBoolean())
						ToolHelper.damageItem(1, (EntityPlayer) attacker, stack);
				}
				if (ToolHelper.isUpgrade(stack, Upgrade.POISON)) {
					if (target.world.rand.nextDouble() < .7)
						target.addPotionEffect(new PotionEffect(Potion.getPotionById(19), 140, 2));
				} else if (ToolHelper.isUpgrade(stack, Upgrade.FIRE)) {
					if (target.world.rand.nextDouble() < .8)
						target.setFire(7);
				} else if (ToolHelper.isUpgrade(stack, Upgrade.SLOW)) {
					if (target.world.rand.nextDouble() < .6)
						target.addPotionEffect(new PotionEffect(Potion.getPotionById(2), 140, 2));
				} else if (ToolHelper.isUpgrade(stack, Upgrade.WITHER)) {
					if (target.world.rand.nextDouble() < .2)
						target.addPotionEffect(new PotionEffect(Potion.getPotionById(20), 140, 2));
				} else if (ToolHelper.isUpgrade(stack, Upgrade.HEAL)) {
					attacker.heal(target.world.rand.nextFloat() * 1.2F);
				}
			}
		}
		return super.hitEntity(stack, target, attacker);
	}

}

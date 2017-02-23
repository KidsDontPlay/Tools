package mrriegel.tools.item;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mrriegel.limelib.helper.BlockHelper;
import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.helper.StackHelper;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.tools.item.ItemToolUpgrade.Upgrade;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public abstract class GenericItemTool extends ItemTool {

	public static final ToolMaterial fin = EnumHelper.addToolMaterial("dorphy", 4, 2222, 7.5f, 2.5f, 20);

	private Set<String> classes;

	protected GenericItemTool(ToolMaterial materialIn, String... classes) {
		super(materialIn, Collections.EMPTY_SET);
		if (classes != null)
			this.classes = Sets.newHashSet(classes);
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass, EntityPlayer player, IBlockState blockState) {
		return getToolClasses(stack).contains(toolClass) ? toolMaterial.getHarvestLevel() : super.getHarvestLevel(stack, toolClass, player, blockState);
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack) {
		return classes != null ? classes : Collections.EMPTY_SET;
	}

	protected final double getBaseDamage(ItemStack stack) {
		if (getToolClasses(stack).contains("shovel"))
			return 1.5f;
		else if (getToolClasses(stack).contains("pickaxe"))
			return 1.0f;
		else if (getToolClasses(stack).contains("axe"))
			return 0.0f;
		return 0f;
	}

	protected final double getBaseSpeed(ItemStack stack) {
		if (getToolClasses(stack).contains("axe"))
			return 0.0f;
		else if (getToolClasses(stack).contains("pickaxe"))
			return -2.8f;
		else if (getToolClasses(stack).contains("shovel"))
			return -3.0f;
		return 0f;
	}

	protected double getAttackDamage(ItemStack stack) {
		return getBaseDamage(stack) + toolMaterial.getDamageVsEntity();
	};

	protected double getAttackSpeed(ItemStack stack) {
		return getBaseSpeed(stack);
	};

	protected float getDigSpeed(ItemStack stack, IBlockState state) {
		float speed = efficiencyOnProperMaterial;
		for (Upgrade u : getUpgrades(stack))
			speed *= u.speedMultiplier;
		return speed;
	};

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = HashMultimap.create();
		if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", getAttackDamage(stack), 0));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", getAttackSpeed(stack), 0));
		}
		return multimap;
	}

	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState state) {
		for (String type : getToolClasses(stack)) {
			if (state.getBlock().isToolEffective(type, state))
				return getDigSpeed(stack, state);
		}
		return 1.0F;
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
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		if (player.world.isRemote)
			return false;
		boolean magnet = isUpgrade(itemstack, Upgrade.MAGNET);
		boolean silk = isUpgrade(itemstack, Upgrade.SILK);
		boolean fortune = isUpgrade(itemstack, Upgrade.LUCK);
		boolean fire = isUpgrade(itemstack, Upgrade.FIRE);
		if (isUpgrade(itemstack, Upgrade.ExE) || isUpgrade(itemstack, Upgrade.SxS) && player.world.getTileEntity(pos) != null) {
			int radius = isUpgrade(itemstack, Upgrade.ExE) ? 1 : 2;
			EnumFacing side = ForgeHooks.rayTraceEyes(player, 4.5d).sideHit;
			NonNullList<BlockPos> lis = NonNullList.create();
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
			NonNullList<ItemStack> drops = NonNullList.create();
			for (BlockPos p : lis) {
				breaK(player, itemstack, p, drops, silk, fortune);
			}
			handleItems(player, pos, drops, magnet, fire);
			return true;
		} else if (isUpgrade(itemstack, Upgrade.VEIN)) {
			LinkedList<BlockPos> research = Lists.newLinkedList(Collections.singleton(pos));
			Set<BlockPos> done = Sets.newHashSet();
			main: while (!research.isEmpty()) {
				BlockPos current = research.poll();
				List<EnumFacing> es = Lists.newArrayList(EnumFacing.VALUES);
				Collections.shuffle(es);
				for (EnumFacing facing : es) {
					BlockPos searchPos = current.offset(facing);
					if (!player.world.isBlockLoaded(searchPos))
						continue;
					if (player.world.getBlockState(searchPos).getBlock() instanceof BlockCrops) {
						if (!done.contains(searchPos)) {
							done.add(searchPos);
							research.add(searchPos);
							if (done.size() >= 30)
								break main;
						}
					}
				}
			}
			NonNullList<ItemStack> drops = NonNullList.create();
			for (BlockPos p : done) {
				breaK(player, itemstack, p, drops, silk, fortune);
			}
			handleItems(player, pos, drops, true, fire);
			return true;
		} else if (isUpgrade(itemstack, Upgrade.AUTOMINE)) {
			Set<BlockPos> done = Sets.newHashSet();
			main: for (int i = 0; i < pos.getY(); i++) {
				for (BlockPos bl : BlockPos.getAllInBox(pos.north().west().down(i), pos.south().east().down(i))) {
					if (BlockHelper.isOre(player.world, bl)) {
						done.add(bl);
						break main;
					}
				}
			}
			NonNullList<ItemStack> drops = NonNullList.create();
			for (BlockPos p : done) {
				breaK(player, itemstack, p, drops, silk, fortune);
			}
			handleItems(player, pos, drops, true, fire);
			return true;
		}
		handleItems(player, pos, silk ? BlockHelper.breakBlockWithSilk(player.world, pos, player, false, true, true) : BlockHelper.breakBlockWithFortune(player.world, pos, fortune ? 3 : 0, player, false, true), magnet, fire);
		return true;
	}

	private void breaK(EntityPlayer player, ItemStack itemstack, BlockPos p, NonNullList<ItemStack> drops, boolean silk, boolean fortune) {
		if (itemstack.isEmpty())
			return;
		itemstack.damageItem(1, player);
		for (ItemStack s : silk ? BlockHelper.breakBlockWithSilk(player.world, p, player, false, true, true) : BlockHelper.breakBlockWithFortune(player.world, p, fortune ? 3 : 0, player, false, true))
			StackHelper.addStack(drops, s);
	}

	protected void handleItems(EntityPlayer player, BlockPos pos, List<ItemStack> stacks, boolean magnet, boolean fire) {
		ItemStack tool = player.inventory.getCurrentItem();
		if (isUpgrade(tool, Upgrade.MAGNET) || magnet)
			for (ItemStack s : stacks) {
				Block.spawnAsEntity(player.world, new BlockPos(player), s);
			}
		else if (isUpgrade(tool, Upgrade.TELE) && NBTStackHelper.hasTag(tool, "gpos")) {
			GlobalBlockPos gpos = GlobalBlockPos.loadGlobalPosFromNBT(NBTStackHelper.getTag(tool, "gpos"));
			World dim = gpos.getWorld();
			IItemHandler inv = InvHelper.getItemHandler(dim, gpos.getPos(), null);
			if (inv == null) {
				handleItemsDefault(player, pos, stacks, fire);
				return;
			}
			for (ItemStack s : stacks) {
				s = ItemHandlerHelper.insertItem(inv, s, false);
			}
			handleItemsDefault(player, pos, stacks, fire);
		} else
			handleItemsDefault(player, pos, stacks, fire);
	}

	private final void handleItemsDefault(EntityPlayer player, BlockPos pos, List<ItemStack> stacks, boolean fire) {
		for (ItemStack s : stacks) {
			Block.spawnAsEntity(player.world, pos, s);
		}
	}

	public static Set<Upgrade> getUpgrades(ItemStack stack) {
		if (!(stack.getItem() instanceof GenericItemTool))
			return Collections.EMPTY_SET;
		//TODO
		return Collections.EMPTY_SET;
	}

	public static boolean isUpgrade(ItemStack stack, Upgrade upgrade) {
		return getUpgrades(stack).stream().anyMatch(u -> u == upgrade);
	}

}

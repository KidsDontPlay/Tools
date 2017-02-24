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
import mrriegel.limelib.helper.StackHelper;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.limelib.util.StackWrapper;
import mrriegel.tools.Tools;
import mrriegel.tools.handler.GuiHandler.ID;
import mrriegel.tools.item.ItemToolUpgrade.Upgrade;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public abstract class GenericItemTool extends ItemTool {

	public static final ToolMaterial fin = EnumHelper.addToolMaterial("dorphy", 4, 2222, 7.5f, 2.5f, 20);
	public static final Map<String, Set<Material>> material = Maps.newHashMap();
	{
		material.put("pickaxe", Sets.newHashSet(Material.IRON, Material.ANVIL, Material.ROCK));
		material.put("axe", Sets.newHashSet(Material.WOOD, Material.PLANTS, Material.VINE));
		material.put("shovel", Sets.newHashSet(Material.SNOW, Material.CRAFTED_SNOW));
	}

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

	protected float getDigSpeed(ItemStack stack, float efficiencyOnProperMaterial) {
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
		if (classes.contains("pickaxe"))
			return getDigSpeed(stack, Items.DIAMOND_PICKAXE.getStrVsBlock(stack, state));
		if (classes.contains("axe"))
			return getDigSpeed(stack, Items.DIAMOND_AXE.getStrVsBlock(stack, state));
		for (String type : getToolClasses(stack)) {
			if (state.getBlock().isToolEffective(type, state))
				return getDigSpeed(stack, efficiencyOnProperMaterial);
		}
		return 1.0F;
	}

	@Override
	public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
		if (classes.contains("pickaxe"))
			return Items.DIAMOND_PICKAXE.canHarvestBlock(state, stack);
		if (classes.contains("shovel"))
			return Items.DIAMOND_SHOVEL.canHarvestBlock(state, stack);
		return super.canHarvestBlock(state, stack);
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
	public boolean onBlockStartBreak(ItemStack tool, BlockPos pos, EntityPlayer player) {
		//		if (player.world.isRemote)
		//			return !false;
		boolean magnet = isUpgrade(tool, Upgrade.MAGNET);
		boolean silk = isUpgrade(tool, Upgrade.SILK);
		boolean fortune = isUpgrade(tool, Upgrade.LUCK);
		boolean fire = isUpgrade(tool, Upgrade.FIRE)||true;
		IBlockState state = player.world.getBlockState(pos);
		if (state.getBlock().getHarvestLevel(state) > toolMaterial.getHarvestLevel())
			return false;
		if (!ForgeHooks.isToolEffective(player.world, pos, tool) || player.isSneaking()) {
			handleItems(player, pos, silk ? BlockHelper.breakBlockWithSilk(player.world, pos, player, false, true, true) : BlockHelper.breakBlockWithFortune(player.world, pos, fortune ? 3 : 0, player, false, true), magnet, fire);
			return true;
		}
		if ((isUpgrade(tool, Upgrade.ExE) || isUpgrade(tool, Upgrade.SxS)) && player.world.getTileEntity(pos) == null) {
			int radius = isUpgrade(tool, Upgrade.ExE) ? 1 : 2;
			EnumFacing side = ForgeHooks.rayTraceEyes(player, 5d).sideHit;
			NonNullList<BlockPos> lis = NonNullList.create();
			switch (side.getAxis()) {
			case X:
				lis.addAll(Lists.newArrayList(BlockPos.getAllInBox(pos.north(radius).down(radius), pos.north(-radius).down(-radius))));
				break;
			case Y:
				lis.addAll(Lists.newArrayList(BlockPos.getAllInBox(pos.east(radius).north(radius), pos.east(-radius).north(-radius))));
				break;
			case Z:
				lis.addAll(Lists.newArrayList(BlockPos.getAllInBox(pos.east(radius).down(radius), pos.east(-radius).down(-radius))));
				break;

			}
			NonNullList<ItemStack> drops = NonNullList.create();
			for (BlockPos p : lis) {
				breaK(player, tool, p, drops, silk, fortune);
			}
			handleItems(player, pos, drops, magnet, fire);
			return true;
		} else if (isUpgrade(tool, Upgrade.VEIN) && player.world.getTileEntity(pos) == null) {
			LinkedList<BlockPos> research = Lists.newLinkedList(Collections.singleton(pos));
			Set<BlockPos> done = Sets.newHashSet();
			Block orig = state.getBlock();
			main: while (!research.isEmpty()) {
				BlockPos current = research.poll();
				List<EnumFacing> es = Lists.newArrayList(EnumFacing.VALUES);
				Collections.shuffle(es);
				List<BlockPos> poss = es.stream().map(f -> current.offset(f)).collect(Collectors.toList());
				poss.addAll(Lists.newArrayList(BlockPos.getAllInBox(current.north().east().up(), current.south().west().down())));
				poss = poss.stream().distinct().collect(Collectors.toList());
				for (BlockPos searchPos : poss) {
					if (!player.world.isBlockLoaded(searchPos))
						continue;
					if (player.world.getBlockState(searchPos).getBlock() == orig) {
						if (!done.contains(searchPos)) {
							done.add(searchPos);
							research.add(searchPos);
							if (done.size() >= 100)
								break main;
						}
					}
				}
			}
			done.add(pos);
			NonNullList<ItemStack> drops = NonNullList.create();
			for (BlockPos p : done) {
				breaK(player, tool, p, drops, silk, fortune);
			}
			handleItems(player, pos, drops, magnet, fire);
			return true;
		} else if (isUpgrade(tool, Upgrade.AUTOMINE) && !player.isCreative()) {
			Set<BlockPos> done = Sets.newHashSet();
			main: for (int i = 0; i < pos.getY(); i++) {
				for (BlockPos bl : BlockPos.getAllInBox(pos.north(2).west(2).down(i), pos.south(2).east(2).down(i))) {
					if (BlockHelper.isOre(player.world, bl)) {
						done.add(bl);
						break main;
					}
				}
			}
			NonNullList<ItemStack> drops = NonNullList.create();
			for (BlockPos p : done) {
				breaK(player, tool, p, drops, silk, fortune);
			}
			handleItems(player, pos, drops, true, fire);
			System.out.println("emp: "+done.stream().map(p->player.world.getBlockState(p)).collect(Collectors.toList()));
			if (!done.isEmpty())
				return true;
		}
		handleItems(player, pos, silk ? BlockHelper.breakBlockWithSilk(player.world, pos, player, false, true, true) : BlockHelper.breakBlockWithFortune(player.world, pos, fortune ? 3 : 0, player, false, true), magnet, fire);
		return true;
	}

	private void breaK(EntityPlayer player, ItemStack tool, BlockPos p, NonNullList<ItemStack> drops, boolean silk, boolean fortune) {
		System.out.println("can: "+ForgeHooks.canToolHarvestBlock(player.world, p, tool)+);
		if (tool.isEmpty() || !ForgeHooks.isToolEffective(player.world, p, tool) || !ForgeHooks.canToolHarvestBlock(player.world, p, tool))
			return;
		tool.damageItem(1, player);
		for (ItemStack s : silk ? BlockHelper.breakBlockWithSilk(player.world, p, player, false, true, true) : BlockHelper.breakBlockWithFortune(player.world, p, fortune ? 3 : 0, player, false, true))
			StackHelper.addStack(drops, s);
	}

	protected void handleItems(EntityPlayer player, BlockPos pos, List<ItemStack> stacks, boolean magnet, boolean fire) {
		if (player.world.isRemote)
			return;
		ItemStack tool = player.inventory.getCurrentItem();
		if (isUpgrade(tool, Upgrade.MAGNET) || magnet)
			handleItemsDefault(player, pos, stacks, true, fire);
		else if (isUpgrade(tool, Upgrade.TELE) && NBTStackHelper.hasTag(tool, "gpos")) {
			GlobalBlockPos gpos = GlobalBlockPos.loadGlobalPosFromNBT(NBTStackHelper.getTag(tool, "gpos"));
			World dim = gpos.getWorld();
			IItemHandler inv = InvHelper.getItemHandler(dim, gpos.getPos(), null);
			if (inv == null) {
				handleItemsDefault(player, pos, stacks, magnet, fire);
				player.sendMessage(new TextComponentString("Inventory was removed"));
				return;
			}
			List<ItemStack> set = NonNullList.create();
			for (ItemStack s : stacks) {
				set.add(ItemHandlerHelper.insertItem(inv, s.copy(), false));
			}
			handleItemsDefault(player, pos, set, magnet, fire);
		} else
			handleItemsDefault(player, pos, stacks, magnet, fire);
	}

	private final void handleItemsDefault(EntityPlayer player, BlockPos pos, List<ItemStack> stacks, boolean magnet, boolean fire) {
		while (!stacks.isEmpty()) {
			ItemStack s = stacks.remove(0);
			if (fire) {
				ItemStack burned = FurnaceRecipes.instance().getSmeltingResult(s).copy();
				if (!burned.isEmpty()) {
					if (!player.inventory.getCurrentItem().isEmpty()) {
						stacks.addAll(StackWrapper.toStackList(Lists.newArrayList(new StackWrapper(burned, burned.getCount() * s.getCount()))));
						s = ItemStack.EMPTY;
						player.inventory.getCurrentItem().damageItem(1, player);
					}
				}
			}
			if (magnet)
				InventoryHelper.spawnItemStack(player.world, player.posX, player.posY + .2, player.posZ, s);
			else
				Block.spawnAsEntity(player.world, pos, s);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		playerIn.openGui(Tools.instance, ID.TOOL.ordinal(), worldIn, 0, 0, 0);
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote)
			System.out.println("effe: " + ForgeHooks.isToolEffective(worldIn, pos, new ItemStack(Items.DIAMOND_PICKAXE)));
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

	public static Set<Upgrade> getUpgrades(ItemStack stack) {
		if (!(stack.getItem() instanceof GenericItemTool))
			return Collections.EMPTY_SET;
		return NBTStackHelper.getItemStackList(stack, "items").stream().map(s -> s.isEmpty() ? null : Upgrade.values()[s.getItemDamage()]).filter(u -> u != null).collect(Collectors.toSet());
	}

	public static boolean isUpgrade(ItemStack stack, Upgrade upgrade) {
		return getUpgrades(stack).stream().anyMatch(u -> u == upgrade);
	}

}

package mrriegel.tools.item;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import mrriegel.limelib.helper.BlockHelper;
import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.helper.StackHelper;
import mrriegel.limelib.item.CommonItemTool;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.limelib.util.StackWrapper;
import mrriegel.tools.ToolHelper;
import mrriegel.tools.Tools;
import mrriegel.tools.handler.GuiHandler.ID;
import mrriegel.tools.item.ItemToolUpgrade.Upgrade;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class GenericItemTool extends CommonItemTool {

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
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	protected float getDigSpeed(ItemStack stack, float efficiencyOnProperMaterial) {
		float s = super.getDigSpeed(stack, efficiencyOnProperMaterial);
		for (Upgrade u : ToolHelper.getUpgrades(stack))
			s *= u.speedMultiplier;
		return s;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack tool, BlockPos pos, EntityPlayer player) {
		if (player.isCreative())
			return false;
		boolean magnet = ToolHelper.isUpgrade(tool, Upgrade.MAGNET);
		boolean silk = ToolHelper.isUpgrade(tool, Upgrade.SILK);
		boolean fortune = ToolHelper.isUpgrade(tool, Upgrade.LUCK);
		boolean fire = ToolHelper.isUpgrade(tool, Upgrade.FIRE);
		IBlockState state = player.world.getBlockState(pos);
		if (state.getBlock().getHarvestLevel(state) > toolMaterial.getHarvestLevel())
			return false;
		if (!BlockHelper.isToolEffective(tool, player.world, pos) || player.isSneaking()) {
			tool.damageItem(1, player);
			handleItems(player, pos, silk ? Lists.newArrayList(BlockHelper.breakBlockWithSilk(player.world, pos, player, false, true,true)) : BlockHelper.breakBlockWithFortune(player.world, pos, fortune ? 3 : 0, player, false, true), magnet, fire);
			return true;
		}
		if ((ToolHelper.isUpgrade(tool, Upgrade.ExE) || ToolHelper.isUpgrade(tool, Upgrade.SxS)) && player.world.getTileEntity(pos) == null) {
			int radius = ToolHelper.isUpgrade(tool, Upgrade.ExE) ? 1 : 2;
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
			if (radius == 2)
				player.getFoodStats().setFoodLevel(Math.max(player.getFoodStats().getFoodLevel() - 1, 0));
			handleItems(player, pos, drops, magnet, fire);
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
				poss.addAll(Lists.newArrayList(BlockPos.getAllInBox(current.north().east().up(), current.south().west().down())));
				poss = poss.stream().distinct().collect(Collectors.toList());
				for (BlockPos searchPos : poss) {
					if (!player.world.isBlockLoaded(searchPos))
						continue;
					if (player.world.getBlockState(searchPos).getBlock() == orig) {
						if (!done.contains(searchPos)) {
							done.add(searchPos);
							research.add(searchPos);
							if (done.size() >= 50)
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
		} else if (ToolHelper.isUpgrade(tool, Upgrade.AUTOMINE) && !player.isCreative()) {
			Set<BlockPos> done = Sets.newHashSet();
			main: for (int i = 0; i < pos.getY(); i++) {
				for (BlockPos bl : BlockPos.getAllInBox(pos.north(2).west(2).down(i), pos.south(2).east(2).down(i))) {
					if (BlockHelper.isOre(player.world, bl) && BlockHelper.isToolEffective(tool, player.world, bl) && player.world.getBlockState(bl).getBlock().getHarvestLevel(player.world.getBlockState(bl)) <= toolMaterial.getHarvestLevel()) {
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
			if (!done.isEmpty())
				return true;
		}
		tool.damageItem(1, player);
		handleItems(player, pos, silk ? Lists.newArrayList(BlockHelper.breakBlockWithSilk(player.world, pos, player, false, true,true)) : BlockHelper.breakBlockWithFortune(player.world, pos, fortune ? 3 : 0, player, false, true), magnet, fire);
		return true;
	}

	private void breaK(EntityPlayer player, ItemStack tool, BlockPos p, NonNullList<ItemStack> drops, boolean silk, boolean fortune) {
		if (player.world.isRemote || tool.isEmpty() || player.world.isAirBlock(p) || !BlockHelper.isToolEffective(tool, player.world, p) || player.world.getBlockState(p).getBlock().getHarvestLevel(player.world.getBlockState(p)) > toolMaterial.getHarvestLevel())
			return;
		tool.damageItem(1, player);
		for (ItemStack s : silk ? Lists.newArrayList(BlockHelper.breakBlockWithSilk(player.world, p, player, false, true,true)) : BlockHelper.breakBlockWithFortune(player.world, p, fortune ? 3 : 0, player, false, true))
			StackHelper.addStack(drops, s);
	}

	protected void handleItems(EntityPlayer player, BlockPos pos, List<ItemStack> stacks, boolean magnet, boolean fire) {
		if (player.world.isRemote)
			return;
		ItemStack tool = player.getHeldItemMainhand();
		if (ToolHelper.isUpgrade(tool, Upgrade.MAGNET) || magnet)
			handleItemsDefault(player, pos, stacks, true, fire);
		else if (ToolHelper.isUpgrade(tool, Upgrade.TELE) && NBTStackHelper.hasTag(tool, "gpos")) {
			GlobalBlockPos gpos = GlobalBlockPos.loadGlobalPosFromNBT(NBTStackHelper.getTag(tool, "gpos"));
			IItemHandler inv = InvHelper.getItemHandler(gpos.getWorld(), gpos.getPos(), null);
			if (inv == null) {
				handleItemsDefault(player, pos, stacks, magnet, fire);
				player.sendMessage(new TextComponentString("Inventory was removed"));
				return;
			}
			List<ItemStack> set = NonNullList.create();
			for (ItemStack s : stacks)
				set.add(ItemHandlerHelper.insertItem(inv, s.copy(), false));
			handleItemsDefault(player, pos, set, magnet, fire);
		} else
			handleItemsDefault(player, pos, stacks, magnet, fire);
	}

	private final void handleItemsDefault(EntityPlayer player, BlockPos pos, List<ItemStack> stacks, boolean magnet, boolean fire) {
		while (!stacks.isEmpty()) {
			ItemStack s = stacks.remove(0);
			if (fire) {
				ItemStack burned = FurnaceRecipes.instance().getSmeltingResult(s).copy();
				if (!burned.isEmpty() && !player.getHeldItemMainhand().isEmpty()) {
					stacks.addAll(StackWrapper.toStackList(Lists.newArrayList(new StackWrapper(burned, burned.getCount() * s.getCount()))));
					s = ItemStack.EMPTY;
					player.inventory.getCurrentItem().damageItem(1, player);
					continue;
				}
			}
			EntityItem ei = new EntityItem(player.world, pos.getX() + .5, pos.getY() + .3, pos.getZ() + .5, s.copy());
			player.world.spawnEntity(ei);
			Vec3d vec = new Vec3d(player.posX - ei.posX, player.posY + .5 - ei.posY, player.posZ - ei.posZ).normalize().scale(0.9);
			if (magnet) {
				ei.motionX = vec.xCoord;
				ei.motionY = vec.yCoord;
				ei.motionZ = vec.zCoord;
			} else
				ei.motionY = .2;
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		playerIn.openGui(Tools.instance, ID.TOOL.ordinal(), worldIn, handIn.ordinal(), 0, 0);
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

}

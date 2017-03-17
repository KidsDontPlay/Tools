package mrriegel.flexibletools.item;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import mrriegel.flexibletools.ToolHelper;
import mrriegel.flexibletools.handler.CTab;
import mrriegel.limelib.LimeLib;
import mrriegel.limelib.datapart.DataPartRegistry;
import mrriegel.limelib.datapart.DataPartWorker;
import mrriegel.limelib.helper.BlockHelper;
import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.helper.ParticleHelper;
import mrriegel.limelib.helper.StackHelper;
import mrriegel.limelib.helper.WorldHelper;
import mrriegel.limelib.item.CommonSubtypeItem;
import mrriegel.limelib.particle.CommonParticle;
import mrriegel.limelib.util.GlobalBlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ItemToolUpgrade extends CommonSubtypeItem {

	public static enum Upgrade {
		ExE("area", .7f, 1, 1, 1, "Mines 3x3 and increases attack range", tools(true)), //
		SxS("area", .4f, 1, 2, 2, "Mines 5x5 and increases attack range", tools(true)), //
		VEIN("area", .3f, 1, 0, 0, "Mines a vein of similiar blocks", tools(false)), //
		AUTOMINE("area", .2f, 1, 0, 2, "Mines ores from the deep", "pickaxe"), //
		MAGNET("transport", 1, 0, 0, "Dropped items head to player", tools(true)), //
		TELE("transport", 1, 1, 1, "Dropped items will be transferd into bound inventory. Shift right click an inventory with your tool to bind", tools(true)), //
		POISON("effect", 1, 1, 0, "Poisons mobs", tools(true)), //
		FIRE("effect", 1, 1, 0, "Sets mobs on fire and smelts dropped items", tools(true)), //
		SLOW("effect", 1, 1, 0, "Slows mobs down", tools(true)), //
		WITHER("effect", 1, 2, 0, "Withers mobs", tools(true)), //
		HEAL("effect", 1, 1, 0, "Heals the player when dealing damage", tools(true)), //
		DAMAGE("support", 3, 1, 0, "Increases attack damage", tools(true)), //
		SPEED("support", 1.6f, 3, 0, 1, "Increases dig speed", tools(false)), //
		LUCK("support", 3, 1, 1, "Increases looting and fortune", tools(true)), //
		SILK("support", 1, 0, 1, "Silk touch", tools(false)), //
		XP("support", 3, 1, 0, "Increases XP from mobs", "sword"), //
		REPAIR("support", 3, 0, 0, "Repairs your tool frequently (doesn't require XP)", tools(true)), //
		REACH("support", 1, 0, 0, "Increases reach", tools(false)), //
		GUI("skill", 1, 0, 0, "Opens tool GUI", tools(true)), //
		TORCH("skill", 1, 0, 0, "Places a torch from your inventory (or a temporary torch) far away", tools(true)), //
		PORT("skill", 1, 0, 0, "Teleports the player forward", tools(true)), //
		BAG("skill", 1, 0, 0, "An ordinary backpack", tools(true)), //
		CHUNKMINER("skill", 1, 0, 0, "Mines a whole chunk. The tool can be repaired and refueled with items around on the ground or in adjacent inventories", "axpickvel");

		public final String category, tooltip;
		private final List<String> toolClasses;
		public final float speedMultiplier;
		public final int max, additionalDamageAttack, additionalDamageBreak;

		Upgrade(String category, float speedMultiplier, int max, int additionalDamageAttack, int additionalDamageBreak, String tooltip, String... toolClasses) {
			this.category = category;
			this.toolClasses = Lists.newArrayList(toolClasses);
			this.speedMultiplier = speedMultiplier;
			this.max = max;
			this.tooltip = tooltip;
			this.additionalDamageAttack = additionalDamageAttack;
			this.additionalDamageBreak = additionalDamageBreak;
		}

		Upgrade(String category, int max, int additionalDamageAttack, int additionalDamageBreak, String tooltip, String... toolClasses) {
			this(category, 1f, max, additionalDamageAttack, additionalDamageBreak, tooltip, toolClasses);
		}

		public boolean isValid(ItemStack tool) {
			if (this == CHUNKMINER)
				return (tool.getItem().getToolClasses(tool).size() == 3);
			return toolClasses.stream().anyMatch(tool.getItem().getToolClasses(tool)::contains);
		}

		private static String[] tools(boolean sword) {
			String[] ar = new String[] { "pickaxe", "axe", "shovel" };
			if (sword)
				return ArrayUtils.add(ar, "sword");
			else
				return ar;
		}

		private static Map<String, List<Upgrade>> cache = Maps.newHashMap();

		public static List<Upgrade> getListForCategory(String category) {
			if (!cache.containsKey(category))
				cache.put(category, Lists.newArrayList(Upgrade.values()).stream().filter(u -> u.category.equalsIgnoreCase(category)).collect(Collectors.toList()));
			return cache.get(category);
		}
	}

	private String category;

	public ItemToolUpgrade(String category) {
		super("tool_upgrade_" + category, Upgrade.getListForCategory(category).size());
		setCreativeTab(CTab.TAB);
		this.category = category;
	}

	public static Upgrade getUpgrade(ItemStack upgrade) {
		if (upgrade.getItem() instanceof ItemToolUpgrade) {
			return Upgrade.getListForCategory(((ItemToolUpgrade) upgrade.getItem()).category).get(upgrade.getItemDamage());
		}
		return null;
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return getUpgrade(stack).max;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		for (String s : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(I18n.format(getUpgrade(stack).tooltip), 200))
			tooltip.add(TextFormatting.YELLOW + s);
		tooltip.add("Tools: " + Joiner.on(", ").join(getUpgrade(stack).toolClasses.stream().map(WordUtils::capitalize).collect(Collectors.toList())));
		tooltip.add("Max: " + getUpgrade(stack).max);
	}

	@Override
	public void registerItem() {
		super.registerItem();
		DataPartRegistry.register("torch_part", TorchPart.class);
		DataPartRegistry.register("quarry_part", QuarryPart.class);
	}

	public static class TorchPart extends DataPartWorker {
		public BlockPos torch;
		int ticks = 0;
		boolean done = false;

		@Override
		protected boolean workDone(World world, Side side) {
			return done;
		}

		@Override
		protected boolean canWork(World world, Side side) {
			return true;
		}

		@Override
		protected void work(World world, Side side) {
			ticks++;
			if (ticks >= 100) {
				done = true;
				if (world.getBlockState(torch).getBlock() == Blocks.TORCH)
					world.setBlockToAir(torch);
			}
		}

		@Override
		public void readFromNBT(NBTTagCompound compound) {
			torch = BlockPos.fromLong(NBTHelper.getLong(compound, "torch"));
			ticks = NBTHelper.getInt(compound, "ticks");
			done = NBTHelper.getBoolean(compound, "done");
			super.readFromNBT(compound);
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound compound) {
			NBTHelper.setLong(compound, "torch", torch.toLong());
			NBTHelper.setInt(compound, "ticks", ticks);
			NBTHelper.setBoolean(compound, "done", done);
			return super.writeToNBT(compound);
		}
	}

	public static class QuarryPart extends DataPartWorker {

		private LinkedList<BlockPos> posList = null;
		private NonNullList<ItemStack> buffer = NonNullList.create();
		private ItemStack tool;
		private int fuel, currentHeight = 1000, left;

		@Override
		public void readFromNBT(NBTTagCompound compound) {
			buffer = NBTHelper.getItemStackList(compound, "buffer");
			tool = NBTHelper.getItemStack(compound, "tool");
			fuel = compound.getInteger("fuel");
			currentHeight = compound.getInteger("currentHeight");
			left = compound.getInteger("left");
			super.readFromNBT(compound);
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound compound) {
			NBTHelper.setItemStackList(compound, "buffer", buffer);
			NBTHelper.setItemStack(compound, "tool", tool);
			compound.setInteger("fuel", fuel);
			compound.setInteger("currentHeight", currentHeight);
			compound.setInteger("left", posList != null ? posList.size() : -1);
			return super.writeToNBT(compound);
		}

		public void setTool(ItemStack tool) {
			this.tool = tool;
		}

		public ItemStack getTool() {
			return tool;
		}

		public void setFuel(int fuel) {
			this.fuel = fuel;
		}

		public int getFuel() {
			return fuel;
		}

		public int getLeft() {
			return left;
		}

		@Override
		public void onRightClicked(EntityPlayer player, EnumHand hand) {
			fuel += 20;
			if (world.isRemote) {
				for (Vec3d vec : ParticleHelper.getVecsForBlock(pos, 10))
					LimeLib.proxy.renderParticle(new CommonParticle(vec.xCoord, vec.yCoord, vec.zCoord, 0, 0.02, 0).setScale(.3f).setFlouncing(.009));
			}
		}

		@Override
		public void onLeftClicked(EntityPlayer player, EnumHand hand) {
			if (!world.isRemote) {
				EntityItem ei = new EntityItem(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, NBTStackHelper.setInt(tool, "fuel", fuel));
				world.spawnEntity(ei);
				ei.setPositionAndUpdate(player.posX, player.posY + .3, player.posZ);
			}
			getRegistry().removeDataPart(pos);
		}

		@Override
		protected boolean workDone(World world, Side side) {
			return false;
		}

		@Override
		protected boolean canWork(World world, Side side) {
			return getItemhandler() != null && tool != null;
		}

		private IItemHandler getItemhandler() {
			GlobalBlockPos gpos = GlobalBlockPos.loadGlobalPosFromNBT(NBTStackHelper.getTag(tool, "gpos"));
			IItemHandler inv = InvHelper.getItemHandler(gpos.getWorld(), gpos.getPos(), null);
			return inv;
		}

		@Override
		public void updateServer(World world) {
			super.updateServer(world);
			Vec3d posvec = new Vec3d(pos).addVector(.5, .5, .5);
			if (world.getTotalWorldTime() % 13 == 0) {
				for (EntityItem ei : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(posvec.addVector(-2, -2, -2), posvec.addVector(2, 2, 2)))) {
					if (ei.isDead)
						continue;
					ItemStack s = ei.getEntityItem();
					if (TileEntityFurnace.isItemFuel(s) && !posList.isEmpty()) {
						fuel += TileEntityFurnace.getItemBurnTime(s) * s.getCount();
						ItemStack container = s.getItem().getContainerItem(s);
						if (container.isEmpty())
							ei.setDead();
						else
							ei.setEntityItemStack(container);
						getRegistry().sync(pos);
					} else if (ToolHelper.repairMap.containsKey(s.getItem())) {
						int value = ToolHelper.repairMap.get(s.getItem());
						int i = 0;
						for (; i < s.getCount(); i++)
							if (value < tool.getItemDamage()) {
								ToolHelper.damageItem(-value, null, tool, null);
							} else
								break;
						ei.getEntityItem().shrink(i);
						if (ei.getEntityItem().getCount() == 0)
							ei.setDead();
					}
				}
			}
			if (ticksExisted > 40 && world.getTotalWorldTime() % 4 == 0) {
				List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(posvec.addVector(-.8, -.8, -.8), posvec.addVector(.8, .8, .8)));
				if (!players.isEmpty()) {
					onLeftClicked(players.get(0), EnumHand.MAIN_HAND);
				}
			}
			if (world.getTotalWorldTime() % 5 == 0)
				for (IItemHandler handler : Lists.newArrayList(EnumFacing.VALUES).stream().map(f -> InvHelper.hasItemHandler(world, pos.offset(f), f.getOpposite()) ? InvHelper.getItemHandler(world, pos.offset(f), f.getOpposite()) : null).filter(i -> i != null).collect(Collectors.toList())) {
					if (fuel <= 1500 && !posList.isEmpty()) {
						ItemStack s = InvHelper.extractItem(handler, st -> TileEntityFurnace.isItemFuel(st), 1, false);
						if (!s.isEmpty()) {
							fuel += TileEntityFurnace.getItemBurnTime(s) * s.getCount();
							ItemStack container = s.getItem().getContainerItem(s);
							if (!container.isEmpty())
								ItemHandlerHelper.insertItemStacked(handler, container, false);
						}

					}
					for (Item rep : ToolHelper.repairMap.keySet()) {
						ItemStack s = InvHelper.extractItem(handler, st -> st.getItem() == rep, 1, true);
						if (!s.isEmpty() && tool.getItemDamage() > ToolHelper.repairMap.get(rep)) {
							if (InvHelper.extractItem(handler, st -> st.getItem() == rep, 1, false).isEmpty())
								continue;
							ToolHelper.damageItem(-ToolHelper.repairMap.get(rep), null, tool, null);
							break;
						}
					}
				}
		}

		@Override
		protected int everyXtick(Side side) {
			return 4;
		}

		@Override
		public AxisAlignedBB getHighlightBox() {
			return super.getHighlightBox().contract(.1);
		}

		public int fuelPerBlock() {
			return 50;
		}

		@Override
		protected void work(World world, Side side) {
			if (side.isServer()) {
				if (posList == null) {
					posList = Lists.newLinkedList(WorldHelper.getChunk(world, getPos()));
					Iterables.removeIf(posList, p -> p.getY() > currentHeight || !BlockHelper.isBlockBreakable(world, p) || Sets.newHashSet(BlockPos.getAllInBox(pos.add(2, 2, 2), pos.add(-2, -2, -2))).contains(p));
					getRegistry().sync(pos);
				}
				IItemHandler handler = getItemhandler();
				for (int i = 0; i < 3; i++)
					if (!posList.isEmpty() && buffer.isEmpty() && handler != null && fuel >= fuelPerBlock() && tool.getItemDamage() + 1 < tool.getMaxDamage()) {
						BlockPos pos = posList.poll();
						while (!BlockHelper.isBlockBreakable(world, pos) && !posList.isEmpty()) {
							pos = posList.poll();
						}
						IBlockState state = world.getBlockState(pos);
						NonNullList<ItemStack> drops = state.getBlock().getHarvestLevel(state) <= ((GenericItemTool) tool.getItem()).getToolMaterial().getHarvestLevel() ? BlockHelper.breakBlock(world, pos, world.getBlockState(pos), null, false, 0, false, true) : NonNullList.create();
						for (ItemStack drop : drops) {
							ItemStack rest = ItemHandlerHelper.insertItemStacked(handler, drop.copy(), false);
							if (!rest.isEmpty())
								StackHelper.addStack(buffer, rest);
						}
						fuel -= fuelPerBlock();
						ToolHelper.damageItem(1, null, tool, null);
						currentHeight = pos.getY();
					}
				getRegistry().sync(pos);
				if (!buffer.isEmpty() && handler != null) {
					ListIterator<ItemStack> it = buffer.listIterator();
					while (it.hasNext()) {
						ItemStack rest = ItemHandlerHelper.insertItemStacked(handler, it.next().copy(), false);
						if (rest.isEmpty())
							it.remove();
						else
							it.set(rest);
					}
				}
			}

		}
	}

}

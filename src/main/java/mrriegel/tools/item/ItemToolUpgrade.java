package mrriegel.tools.item;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import mrriegel.limelib.datapart.DataPartRegistry;
import mrriegel.limelib.datapart.DataPartWorker;
import mrriegel.limelib.helper.BlockHelper;
import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.helper.StackHelper;
import mrriegel.limelib.helper.WorldHelper;
import mrriegel.limelib.item.CommonSubtypeItem;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.limelib.util.Utils;
import mrriegel.tools.handler.CTab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ItemToolUpgrade extends CommonSubtypeItem {

	public static enum Upgrade {
		ExE("area", .7f, 1, tools(true)), //
		SxS("area", .4f, 1, tools(true)), //
		VEIN("area", .3f, 1, tools(false)), //
		AUTOMINE("area", .2f, 1, "pickaxe"), //
		MAGNET("transport", 1, tools(true)), //
		TELE("transport", 1, tools(true)), //
		POISON("effect", 1, tools(true)), //
		FIRE("effect", 1, tools(true)), //
		SLOW("effect", 1, tools(true)), //
		WITHER("effect", 1, tools(true)), //
		HEAL("effect", 1, tools(true)), //
		DAMAGE("support", 4, tools(true)), //
		SPEED("support", 1.7f, 3, tools(false)), //
		LUCK("support", 3, tools(true)), //
		SILK("support", 1, tools(false)), //
		XP("support", 3, "sword"), //
		REPAIR("support", 1, tools(true)), //
		GUI("skill", 1, tools(true)), //
		TORCH("skill", 1, tools(true)), //
		PORT("skill", 1, tools(true)), //
		BAG("skill", 1, tools(true)), //
		CHUNKMINER("skill", 1, "axpickvel");

		public final String category;
		private final List<String> toolClasses;
		public final float speedMultiplier;
		public final int max;

		Upgrade(String category, float speedMultiplier, int max, String... toolClasses) {
			this.category = category;
			this.toolClasses = Lists.newArrayList(toolClasses);
			this.speedMultiplier = speedMultiplier;
			this.max = max;
		}

		Upgrade(String category, int max, String... toolClasses) {
			this(category, 1f, max, toolClasses);
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
	public String getUnlocalizedName(ItemStack stack) {
		if (!"".isEmpty())
			return super.getUnlocalizedName(stack);
		return getUpgrade(stack).name();
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		String s = null;
		switch (getUpgrade(stack)) {
		case AUTOMINE:
			s = "Mines ores from the deep";
			break;
		case BAG:
			s = "An ordinary backpack";
			break;
		case DAMAGE:
			s = "Increases attack damage";
			break;
		case ExE:
			s = "Mines 3x3";
			break;
		case FIRE:
			s = "Sets mobs on fire and smelts dropped items";
			break;
		case GUI:
			s = "Opens tool GUI";
			break;
		case HEAL:
			s = "Heals the player when dealing damage";
			break;
		case LUCK:
			s = "Increases luck and fortune";
			break;
		case MAGNET:
			s = "Dropped items head to player";
			break;
		case POISON:
			s = "Poisons mobs";
			break;
		case PORT:
			s = "Teleports the player forward";
			break;
		case REPAIR:
			s = "Repairs your tool frequently";
			break;
		case SILK:
			s = "Silk touch";
			break;
		case SLOW:
			s = "Slows mobs down";
			break;
		case SPEED:
			s = "Increases dig speed";
			break;
		case SxS:
			s = "Mines 5x5";
			break;
		case TELE:
			s = "Dropped items will be transferd into bound inventory. Shift right click an inventory with your tool to bind";
			break;
		case TORCH:
			s = "Places a torch from your inventory (or a temporary torch) far away";
			break;
		case VEIN:
			s = "Mines a vein of similiar blocks";
			break;
		case WITHER:
			s = "Withers mobs";
			break;
		case XP:
			s = "Increases xp from mobs";
			break;
		default:
			break;

		}
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

		private LinkedList<BlockPos> posList = Lists.newLinkedList();
		private NonNullList<ItemStack> buffer = NonNullList.create();
		private boolean started = false;
		private ItemStack tool;

		@Override
		public void readFromNBT(NBTTagCompound compound) {
			posList = Lists.newLinkedList(Utils.getBlockPosList(NBTHelper.getLongList(compound, "poss")));
			started = compound.getBoolean("started");
			buffer = NBTHelper.getItemStackList(compound, "buffer");
			tool = NBTHelper.getItemStack(compound, "tool");
			super.readFromNBT(compound);
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound compound) {
			NBTHelper.setLongList(compound, "poss", Utils.getLongList(posList));
			compound.setBoolean("started", started);
			NBTHelper.setItemStackList(compound, "buffer", buffer);
			NBTHelper.setItemStack(compound, "tool", tool);
			return super.writeToNBT(compound);
		}

		public void setTool(ItemStack tool) {
			this.tool = tool;
		}

		public ItemStack getTool() {
			return tool;
		}

		@Override
		protected boolean workDone(World world, Side side) {
			return posList.isEmpty() && buffer.isEmpty() && started;
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
			if (world.getTotalWorldTime() % 8 == 0) {
				List<EntityItem> entities = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(posvec.addVector(-2, -2, -2), posvec.addVector(2, 2, 2)));
				for (EntityItem i : entities)
					if (!i.isDead)
						i.setDead();
			}
			if (world.getTotalWorldTime() % 4 == 0) {
				List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(posvec.addVector(-.8, -.8, -.8), posvec.addVector(.8, .8, .8)));
				for (EntityPlayer i : players) {
					EntityItem ei = new EntityItem(world, i.posX, i.posY, i.posZ, tool);
					world.spawnEntity(ei);
					getRegistry().removeDataPart(pos);
					break;
				}
			}
		}

		@Override
		protected int everyXtick(Side side) {
			return 4;
		}

		@Override
		protected void work(World world, Side side) {
			if (side.isServer()) {
				if (!started) {
					started = true;
					posList = Lists.newLinkedList(WorldHelper.getChunk(world, getPos()));
				}
				IItemHandler handler = getItemhandler();
				for (int i = 0; i < 3; i++)
					if (!posList.isEmpty() && buffer.isEmpty() && handler != null) {
						BlockPos pos = posList.poll();
						while (!BlockHelper.isBlockBreakable(world, pos) && !posList.isEmpty()) {
							pos = posList.poll();
						}
						IBlockState state = world.getBlockState(pos);
						NonNullList<ItemStack> drops = state.getBlock().getHarvestLevel(state) <= ((GenericItemTool) tool.getItem()).getToolMaterial().getHarvestLevel() ? BlockHelper.breakBlock(world, pos, world.getBlockState(pos), null, false, 0, false, true) : NonNullList.create();
						for (ItemStack drop : drops) {
							ItemStack rest = ItemHandlerHelper.insertItem(handler, drop.copy(), false);
							if (!rest.isEmpty())
								StackHelper.addStack(buffer, rest);
						}
					}
				if (!buffer.isEmpty() && handler != null) {
					ListIterator<ItemStack> it = buffer.listIterator();
					while (it.hasNext()) {
						ItemStack rest = ItemHandlerHelper.insertItem(handler, it.next().copy(), false);
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

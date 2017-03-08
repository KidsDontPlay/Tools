package mrriegel.tools.item;

import java.util.List;
import java.util.stream.Collectors;

import mrriegel.limelib.datapart.DataPartRegistry;
import mrriegel.limelib.datapart.DataPartWorker;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.item.CommonSubtypeItem;
import mrriegel.tools.handler.CTab;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class ItemToolUpgrade extends CommonSubtypeItem {

	public enum Upgrade {
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
		BAG("skill", 1, tools(true));

		public final String category;
		public List<String> toolClasses;
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

		private static String[] tools(boolean sword) {
			String[] ar = new String[] { "pickaxe", "axe", "shovel" };
			if (sword)
				return ArrayUtils.add(ar, "sword");
			else
				return ar;
		}

		public static List<Upgrade> getListForCategory(String category) {
			return Lists.newArrayList(Upgrade.values()).stream().filter(u -> u.category.equalsIgnoreCase(category)).collect(Collectors.toList());
		}
	}

	private String category;

	public ItemToolUpgrade(String category) {
		super("tool_upgrade_" + category, Upgrade.getListForCategory(category).size());
		setCreativeTab(CTab.TAB);
		this.category = category;
	}

	public Upgrade getUpgrade(ItemStack upgrade) {
		if (upgrade.getItem() instanceof ItemToolUpgrade) {
			return Upgrade.getListForCategory(category).get(upgrade.getItemDamage());
		}
		return null;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (!"".isEmpty())
			return super.getUnlocalizedName(stack);
		return Upgrade.getListForCategory(category).get(stack.getItemDamage()).name();
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add("Tools: " + Joiner.on(", ").join(Upgrade.getListForCategory(category).get(stack.getItemDamage()).toolClasses.stream().map(WordUtils::capitalize).collect(Collectors.toList())));
		tooltip.add("Max: " + Upgrade.getListForCategory(category).get(stack.getItemDamage()).max);
	}

	@Override
	public void registerItem() {
		super.registerItem();
		DataPartRegistry.register("torch_part", TorchPart.class);
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
}

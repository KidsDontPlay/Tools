package mrriegel.flexibletools.item;

import java.util.List;

import cofh.redstoneflux.api.IEnergyContainerItem;
import mrriegel.flexibletools.ToolHelper;
import mrriegel.flexibletools.item.ItemToolUpgrade.Upgrade;
import mrriegel.limelib.LimeLib;
import mrriegel.limelib.helper.EnergyHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.util.GlobalBlockPos;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "cofh.redstoneflux.api.IEnergyContainerItem", modid = "redstoneflux")
public interface ITool extends IEnergyContainerItem {

	final int maxReceive = 1000;
	final int maxExtract = 1000;

	@Override
	default int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
		if (container.getTagCompound() == null || !container.getTagCompound().hasKey("Energy")) {
			return 0;
		}
		int energy = container.getTagCompound().getInteger("Energy");
		int energyExtracted = Math.min(energy, Math.min(ITool.maxExtract, maxExtract));

		if (!simulate) {
			energy -= energyExtracted;
			container.getTagCompound().setInteger("Energy", energy);
		}
		return energyExtracted;
	}

	@Override
	default int getEnergyStored(ItemStack container) {
		if (container.getTagCompound() == null || !container.getTagCompound().hasKey("Energy")) {
			return 0;
		}
		return container.getTagCompound().getInteger("Energy");
	}

	@Override
	default int getMaxEnergyStored(ItemStack container) {
		return container.getMaxDamage() * 100;
	}

	@Override
	default int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
		if (!container.hasTagCompound()) {
			container.setTagCompound(new NBTTagCompound());
		}
		int energy = container.getTagCompound().getInteger("Energy");
		int energyReceived = Math.min(getMaxEnergyStored(container) - energy, Math.min(ITool.maxReceive, maxReceive));

		if (!simulate) {
			energy += energyReceived;
			container.getTagCompound().setInteger("Energy", energy);
		}
		return energyReceived;
	}

	default void addInfo(ItemStack stack, List<String> tooltip) {
		if (ToolHelper.isUpgrade(stack, Upgrade.ENERGY))
			tooltip.add(TextFormatting.BLUE.toString() + getEnergyStored(stack) + "/" + getMaxEnergyStored(stack) + " " + EnergyHelper.isEnergyContainer(stack, null).unit);
		if (!GuiScreen.isShiftKeyDown())
			tooltip.add(TextFormatting.ITALIC + "Hold SHIFT to see upgrades");
		else
			for (Upgrade u : Upgrade.values()) {
				int count = ToolHelper.getUpgradeCount(stack, u);
				if (count > 0) {
					String s = ItemToolUpgrade.upgradeMap.get(u).getDisplayName().replaceFirst("(?i)upgrade", "").trim();
					tooltip.add(TextFormatting.BLUE.toString() + s + ": " + count);
				}
			}
		if (NBTStackHelper.hasTag(stack, "gpos")) {
			GlobalBlockPos gpos = GlobalBlockPos.loadGlobalPosFromNBT(NBTStackHelper.get(stack, "gpos", NBTTagCompound.class));
			if (gpos != null)
				tooltip.add(TextFormatting.AQUA + "Bound to " + String.format("x:%d, y:%d, z:%d", gpos.getPos().getX(), gpos.getPos().getY(), gpos.getPos().getZ()));
		}
	}

	static class CP implements ICapabilityProvider {
		ItemStack stack;

		public CP(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			if (!ToolHelper.isUpgrade(stack, Upgrade.ENERGY))
				return false;
			return capability == CapabilityEnergy.ENERGY || (LimeLib.teslaLoaded && (capability == TeslaCapabilities.CAPABILITY_HOLDER || capability == TeslaCapabilities.CAPABILITY_CONSUMER || capability == TeslaCapabilities.CAPABILITY_PRODUCER));
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (hasCapability(capability, facing))
				return (T) new EW(stack);
			return null;
		}

		@Optional.InterfaceList(value = { @Optional.Interface(iface = "net.darkhax.tesla.api.ITeslaHolder", modid = "tesla"), @Optional.Interface(iface = "net.darkhax.tesla.api.ITeslaConsumer", modid = "tesla"), @Optional.Interface(iface = "net.darkhax.tesla.api.ITeslaProducer", modid = "tesla") })
		static class EW implements IEnergyStorage, ITeslaHolder, ITeslaConsumer, ITeslaProducer {
			ItemStack stack;

			public EW(ItemStack stack) {
				this.stack = stack;
			}

			@Override
			public int receiveEnergy(int maxReceive, boolean simulate) {
				return ((ITool) stack.getItem()).receiveEnergy(stack, maxReceive, simulate);
			}

			@Override
			public int extractEnergy(int maxExtract, boolean simulate) {
				return ((ITool) stack.getItem()).extractEnergy(stack, maxExtract, simulate);
			}

			@Override
			public int getEnergyStored() {
				return ((ITool) stack.getItem()).getEnergyStored(stack);
			}

			@Override
			public int getMaxEnergyStored() {
				return ((ITool) stack.getItem()).getMaxEnergyStored(stack);
			}

			@Override
			public boolean canExtract() {
				return true;
			}

			@Override
			public boolean canReceive() {
				return true;
			}

			@Override
			public long takePower(long power, boolean simulated) {
				return extractEnergy((int) (power % Integer.MAX_VALUE), simulated);
			}

			@Override
			public long givePower(long power, boolean simulated) {
				return receiveEnergy((int) (power % Integer.MAX_VALUE), simulated);
			}

			@Override
			public long getStoredPower() {
				return getEnergyStored();
			}

			@Override
			public long getCapacity() {
				return getMaxEnergyStored();
			}

		}
	}
}

package mrriegel.flexibletools.item;

import java.util.List;

import cofh.redstoneflux.api.IEnergyContainerItem;
import mrriegel.flexibletools.ToolHelper;
import mrriegel.flexibletools.item.ItemToolUpgrade.Upgrade;
import mrriegel.limelib.LimeLib;
import mrriegel.limelib.helper.EnergyHelper;
import mrriegel.limelib.helper.EnergyHelper.ItemEnergyWrapper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.util.GlobalBlockPos;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
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
			GlobalBlockPos gpos = GlobalBlockPos.loadGlobalPosFromNBT(NBTStackHelper.get(stack, "gpos",NBTTagCompound.class));
			if (gpos != null)
				tooltip.add(TextFormatting.AQUA + "Bound to " + String.format("x:%d, y:%d, z:%d", gpos.getPos().getX(), gpos.getPos().getY(), gpos.getPos().getZ()));
		}
	}

	static class CP implements ICapabilityProvider {
		ItemStack stack;

		public CP(ItemStack stack) {
			super();
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
				return (T) new ItemEnergyWrapper(stack);
			return null;
		}
	}
}

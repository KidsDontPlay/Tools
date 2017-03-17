package mrriegel.flexibletools.handler;

import mrriegel.flexibletools.gui.ContainerBag;
import mrriegel.flexibletools.gui.ContainerTool;
import mrriegel.flexibletools.gui.GuiBag;
import mrriegel.flexibletools.gui.GuiTool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	public enum ID {
		BAG, //
		TOOL;
	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID.values()[id]) {
		case BAG:
			return new ContainerBag(player.inventory, EnumHand.values()[x], y != 0);
		case TOOL:
			return new ContainerTool(player.inventory, EnumHand.values()[x]);
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID.values()[id]) {
		case BAG:
			return new GuiBag(new ContainerBag(player.inventory, EnumHand.values()[x], y != 0));
		case TOOL:
			return new GuiTool(new ContainerTool(player.inventory, EnumHand.values()[x]));
		}
		return null;
	}

}

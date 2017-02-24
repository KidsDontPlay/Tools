package mrriegel.tools.handler;

import mrriegel.tools.gui.ContainerFoodBag;
import mrriegel.tools.gui.ContainerTool;
import mrriegel.tools.gui.GuiFoodBag;
import mrriegel.tools.gui.GuiTool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	public enum ID {
		FOODBAG, //
		TOOL;
	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID.values()[id]) {
		case FOODBAG:
			return new ContainerFoodBag(player.inventory, EnumHand.values()[x]);
		case TOOL:
			return new ContainerTool(player.inventory, EnumHand.values()[x]);
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID.values()[id]) {
		case FOODBAG:
			return new GuiFoodBag(new ContainerFoodBag(player.inventory, EnumHand.values()[x]));
		case TOOL:
			return new GuiTool(new ContainerTool(player.inventory, EnumHand.values()[x]));
		}
		return null;
	}

}

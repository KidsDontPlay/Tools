package mrriegel.flexibletools.gui;

import mrriegel.limelib.gui.CommonGuiContainer;
import net.minecraft.inventory.Container;

public class GuiBag extends CommonGuiContainer {

	public GuiBag(Container inventorySlotsIn) {
		super(inventorySlotsIn);
		ySize = 156;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		drawer.drawBackgroundTexture();
		drawer.drawPlayerSlots(7, 73);
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		drawer.drawSlots(7, 12, 9, 3);
	}

}

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
		drawDefaultBackground();
		drawer.drawBackgroundTexture();
		drawer.drawPlayerSlots(7, 73);
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		drawer.drawSlots(7, 12, 9, 3);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

}

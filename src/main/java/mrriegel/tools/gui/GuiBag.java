package mrriegel.tools.gui;

import java.io.IOException;

import mrriegel.limelib.gui.CommonGuiContainer;
import mrriegel.limelib.gui.button.GuiButtonSimple;
import mrriegel.limelib.network.PacketHandler;
import mrriegel.tools.network.MessageButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;

import com.google.common.collect.Lists;

public class GuiBag extends CommonGuiContainer {

	GuiButton impor;

	public GuiBag(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		drawer.drawBackgroundTexture();
		drawer.drawPlayerSlots(7, 83);
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(impor = new GuiButtonSimple(0, guiLeft + 8, guiTop + 10, 40, 14, "Import", Lists.newArrayList("Import food into the bag")));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button.id == 0) {
			PacketHandler.sendToServer(new MessageButton(0));
		}
	}

}

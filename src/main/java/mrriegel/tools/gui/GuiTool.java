package mrriegel.tools.gui;

import java.awt.Color;

import mrriegel.limelib.gui.CommonGuiContainer;
import mrriegel.limelib.helper.ColorHelper;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import org.apache.commons.lang3.tuple.Pair;

public class GuiTool extends CommonGuiContainer {

	public GuiTool(Container inventorySlotsIn) {
		super(inventorySlotsIn);
		ySize = 150;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		drawer.drawBackgroundTexture();
		drawer.drawPlayerSlots(7, 83 - 16);
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		fontRenderer.drawString(mc.player.getHeldItemMainhand().getDisplayName(), guiLeft + 7, guiTop + 7, Color.DARK_GRAY.getRGB());
		drawer.drawSlot(7, 17);
		drawer.drawColoredRectangle(8, 18, 16, 16, ColorHelper.getRGB(0x5e1f1f, 150));
		drawer.drawSlot(27, 17);
		drawer.drawColoredRectangle(28, 18, 16, 16, ColorHelper.getRGB(0x325b1e, 150));
		drawer.drawSlot(47, 17);
		drawer.drawColoredRectangle(48, 18, 16, 16, ColorHelper.getRGB(0x634315, 150));
		drawer.drawSlots(67, 17, 2, 2);
		for (Pair<Integer, Integer> p : ar) {
			drawer.drawColoredRectangle(p.getLeft(), p.getRight(), 16, 16, ColorHelper.getRGB(0x1d4d58, 150));
		}
		drawer.drawSlot(107, 17);
		drawer.drawSlot(127, 17);
		for (Pair<Integer, Integer> p : new Pair[] { Pair.of(108, 18), Pair.of(128, 18) }) {
			drawer.drawColoredRectangle(p.getLeft(), p.getRight(), 16, 16, ColorHelper.getRGB(0x31236a, 150));
		}
	}

	private Pair<Integer, Integer>[] ar = new Pair[] { Pair.of(68, 18), Pair.of(86, 18), Pair.of(68, 36), Pair.of(86, 36) };

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		Slot skill1 = inventorySlots.inventorySlots.get(7);
		if (!skill1.getHasStack() && isPointInRegion(skill1.xPos, skill1.yPos, 16, 16, mouseX, mouseY))
			drawHoveringText("Right Click", mouseX - guiLeft, mouseY - guiTop);
		Slot skill2 = inventorySlots.inventorySlots.get(8);
		if (!skill2.getHasStack() && isPointInRegion(skill2.xPos, skill2.yPos, 16, 16, mouseX, mouseY))
			drawHoveringText("Shift Right Click", mouseX - guiLeft, mouseY - guiTop);
	}

}

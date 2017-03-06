package mrriegel.tools;

import mrriegel.limelib.block.CommonBlock;
import mrriegel.tools.block.BlockGlowSand;

public class ModBlocks {

	public static final CommonBlock glowsand = new BlockGlowSand();

	public static void init() {
		glowsand.registerBlock();
	}

	public static void initClient() {
		glowsand.initModel();
	}
}

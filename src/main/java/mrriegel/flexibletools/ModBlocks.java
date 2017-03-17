package mrriegel.flexibletools;

import mrriegel.flexibletools.block.BlockGlowSand;
import mrriegel.limelib.block.CommonBlock;

public class ModBlocks {

	public static final CommonBlock glowsand = new BlockGlowSand();

	public static void init() {
		//		glowsand.registerBlock();
	}

	public static void initClient() {
		glowsand.initModel();
	}
}

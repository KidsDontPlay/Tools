package mrriegel.flexibletools.handler;

import java.io.File;
import java.util.EnumMap;

import mrriegel.flexibletools.item.ItemToolUpgrade.Upgrade;
import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {

	public static Configuration config;
	public static EnumMap<Upgrade, Boolean> upgrades = new EnumMap<Upgrade, Boolean>(Upgrade.class);
	public static boolean noTile;

	public static void refreshConfig(File file) {
		config = new Configuration(file);
		for (Upgrade u : Upgrade.values()) {
			upgrades.put(u, config.getBoolean(u.name(), "upgrades", true, "Enable " + u.name().toLowerCase() + " upgrade"));
		}
		noTile = config.getBoolean("noTile", Configuration.CATEGORY_GENERAL, true, "Some tile entities from other mods are incompatible with tools from this mod." + Configuration.NEW_LINE + "With this option you can disable the special functions for tile entities." + Configuration.NEW_LINE);

		if (config.hasChanged())
			config.save();
	}

}

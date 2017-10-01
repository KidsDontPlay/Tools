package mrriegel.flexibletools.handler;

import java.io.File;
import java.util.EnumMap;

import mrriegel.flexibletools.item.ItemToolUpgrade.Upgrade;
import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {

	public static Configuration config;
	public static EnumMap<Upgrade, Boolean> upgrades = new EnumMap<Upgrade, Boolean>(Upgrade.class);

	public static void refreshConfig(File file) {
		config = new Configuration(file);
		for (Upgrade u : Upgrade.values()) {
			upgrades.put(u, config.getBoolean(u.name(), "upgrades", true, "Enable " + u.name().toLowerCase() + " upgrade"));
		}

		if (config.hasChanged())
			config.save();
	}

}

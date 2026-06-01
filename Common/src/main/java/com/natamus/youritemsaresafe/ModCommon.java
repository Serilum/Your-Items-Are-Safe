package com.natamus.youritemsaresafe;

import com.natamus.collective_common_fabric.features.PlayerHeadCacheFeature;
import com.natamus.collective_common_fabric.services.Services;
import com.natamus.youritemsaresafe.config.ConfigHandler;
import com.natamus.youritemsaresafe.data.Constants;

public class ModCommon {

	public static void init() {
		ConfigHandler.initConfig();
		load();
	}

	private static void load() {
		PlayerHeadCacheFeature.enableHeadCaching();

		Constants.inventoryTotemModIsLoaded = Services.MODLOADER.isModLoaded("inventory-totem");
	}
}
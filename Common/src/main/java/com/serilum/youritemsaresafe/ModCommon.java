package com.serilum.youritemsaresafe;

import com.natamus.collective.features.PlayerHeadCacheFeature;
import com.natamus.collective.services.Services;
import com.serilum.youritemsaresafe.config.ConfigHandler;
import com.serilum.youritemsaresafe.data.Constants;

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
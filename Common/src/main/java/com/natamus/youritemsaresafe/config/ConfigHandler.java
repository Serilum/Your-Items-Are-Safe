package com.natamus.youritemsaresafe.config;

import com.natamus.collective.config.DuskConfig;
import com.natamus.youritemsaresafe.util.Reference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigHandler extends DuskConfig {
	public static HashMap<String, List<String>> configMetaData = new HashMap<String, List<String>>();

	@Entry public static boolean mustHaveItemsInInventoryForCreation = true;
	@Entry public static boolean addPlayerHeadToArmorStand = true;
	@Entry public static boolean createArmorStand = true;
	@Entry public static boolean createSignWithPlayerName = true;
	@Entry public static boolean needChestMaterials = true;
	@Entry public static boolean needArmorStandMaterials = true;
	@Entry public static boolean needSignMaterials = false;
	@Entry public static boolean ignoreStoneMaterialNeed = true;
	@Entry public static boolean createChestAboveVoid = true;
	@Entry public static boolean createVoidPlatform = true;
	@Entry public static boolean sendMessageOnCreationFailure = true;
	@Entry public static boolean sendMessageOnCreationSuccess = true;
	@Entry public static boolean sendDeathCoordinatesInChat = true;
	@Entry public static String creationFailureMessage = "Your items are not safe due to having insufficient materials. Missing: %plankamount% planks.";
	@Entry public static String creationSuccessMessage = "Your items are safe at your death location.";

	public static void initConfig() {
		configMetaData.put("mustHaveItemsInInventoryForCreation", Arrays.asList(
			"When enabled and a player dies without any items in their inventory, no chest or armor stand is generated."
		));
		configMetaData.put("addPlayerHeadToArmorStand", Arrays.asList(
			"If a player head should be added to the armor stand. If a helmet is worn, this will be placed into the chest."
		));
		configMetaData.put("createArmorStand", Arrays.asList(
			"Whether an armor stand should be created on death. If disabled, the player's gear will be placed inside the chest."
		));
		configMetaData.put("createSignWithPlayerName", Arrays.asList(
			"Whether a sign should be placed on the chest with the name of the player who died there."
		));
		configMetaData.put("needChestMaterials", Arrays.asList(
			"Whether materials are needed for the chest which spawns on death. This can be the actual chest or the costs in raw materials."
		));
		configMetaData.put("needArmorStandMaterials", Arrays.asList(
			"Whether materials are needed for the armor stand to spawn on death. This can be the actual armor stand or the costs in raw materials."
		));
		configMetaData.put("needSignMaterials", Arrays.asList(
			"Whether materials are needed for the creation of the sign when 'createSignWithPlayerName' is enabled."
		));
		configMetaData.put("ignoreStoneMaterialNeed", Arrays.asList(
			"Only relevant if 'needChestAndArmorStandMaterials' is enabled. An armor stand needs 1 stone slab to be created, but I think it's alright to ignore that requirement. If enabled, no stone is needed in the inventory on death."
		));
		configMetaData.put("createChestAboveVoid", Arrays.asList(
			"If a chest should be placed right above the minimum build height when a player dies in the void."
		));
		configMetaData.put("createVoidPlatform", Arrays.asList(
			"If a 3x3 platform should be created below the chest above the void. 'createChestAboveVoid' must be enabled."
		));
		configMetaData.put("sendMessageOnCreationFailure", Arrays.asList(
			"If a message should be sent if the chest or armor stand can't be created due to missing materials."
		));
		configMetaData.put("sendMessageOnCreationSuccess", Arrays.asList(
			"If a message should be sent on successful creation of the chest(s) and armor stand."
		));
		configMetaData.put("sendDeathCoordinatesInChat", Arrays.asList(
			"If the player's death coordinates should be sent in the chat below the 'sendMessageOnCreationFailure'/'sendMessageOnCreationSuccess' message."
		));
		configMetaData.put("creationFailureMessage", Arrays.asList(
			"The message sent on creation failure with 'sendMessageOnCreationFailure' enabled. Possible replacement values: %plankamount%, %stoneamount%."
		));
		configMetaData.put("creationSuccessMessage", Arrays.asList(
			"The message sent on creation success with 'sendMessageOnCreationSuccess' enabled."
		));

		DuskConfig.init(Reference.NAME, Reference.MOD_ID, ConfigHandler.class);
	}
}
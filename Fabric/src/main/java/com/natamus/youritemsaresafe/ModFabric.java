package com.natamus.youritemsaresafe;

import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.fabric.data.GlobalFabricObjects;
import com.natamus.youritemsaresafe.data.Constants;
import com.natamus.youritemsaresafe.events.DeathEvent;
import com.natamus.youritemsaresafe.util.Reference;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class ModFabric implements ModInitializer {
	
	@Override
	public void onInitialize() {
		setGlobalConstants();
		ModCommon.init();

		loadEvents();

		RegisterMod.register(Reference.NAME, Reference.MOD_ID, Reference.VERSION, Reference.ACCEPTED_VERSIONS);
	}

	private void loadEvents() {
		ServerLivingEntityEvents.ALLOW_DEATH.register((LivingEntity livingEntity, DamageSource damageSource, float damageAmount) -> {
			if (livingEntity instanceof ServerPlayer) {
				DeathEvent.onPlayerDeath((ServerPlayer)livingEntity, damageSource, damageAmount);
			}
			return true;
		});
	}

	private static void setGlobalConstants() {
		Constants.inventoryTotemLoaded = GlobalFabricObjects.fabricLoader.isModLoaded("inventory-totem");
	}
}

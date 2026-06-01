package com.natamus.youritemsaresafe;

import com.natamus.collective_common_fabric.check.RegisterMod;
import com.natamus.collective_common_fabric.check.ShouldLoadCheck;
import com.natamus.collective_common_fabric.services.Services;
import com.natamus.youritemsaresafe.data.Constants;
import com.natamus.youritemsaresafe.events.DeathEvent;
import com.natamus.youritemsaresafe.fabric.integration.TravelersBackpackCollector;
import com.natamus.youritemsaresafe.fabric.integration.TrinketsCollector;
import com.natamus.youritemsaresafe.integration.Integrations;
import com.natamus.youritemsaresafe.util.Reference;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class ModFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		if (!ShouldLoadCheck.shouldLoad(Reference.MOD_ID)) {
			return;
		}

		ModCommon.init();

		loadIntegrations();
		loadEvents();

		RegisterMod.register(Reference.NAME, Reference.MOD_ID, Reference.VERSION, Reference.ACCEPTED_VERSIONS);
	}

	private void loadIntegrations() {
		Constants.travelersBackpackModIsLoaded = Services.MODLOADER.isModLoaded("travelersbackpack");
		if (Constants.travelersBackpackModIsLoaded) {
			Integrations.register(TravelersBackpackCollector::collect);
		}

		Constants.trinketsModIsLoaded = Services.MODLOADER.isModLoaded("trinkets_updated");
		if (Constants.trinketsModIsLoaded) {
			Integrations.register(TrinketsCollector::collect);
		}
	}

	private void loadEvents() {
		// Register in a phase that runs before DEFAULT_PHASE — ensures YIAS clears
		// worn slots (TB, Trinkets, etc.) before any other mod's ALLOW_DEATH listener.
		Identifier prePhase = Identifier.parse("youritemsaresafe:pre_default");
		ServerLivingEntityEvents.ALLOW_DEATH.addPhaseOrdering(prePhase, Event.DEFAULT_PHASE);
		ServerLivingEntityEvents.ALLOW_DEATH.register(prePhase, (LivingEntity livingEntity, DamageSource damageSource, float damageAmount) -> {
			if (livingEntity instanceof ServerPlayer) {
				DeathEvent.onPlayerDeath((ServerPlayer)livingEntity, damageSource, damageAmount);
			}
			return true;
		});
	}
}

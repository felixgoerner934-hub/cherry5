package com.spawnerbeacon;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public class SpawnerBeaconClient implements ClientModInitializer {
	public static final String MOD_ID = "spawnerbeacon";

	private static KeyMapping openMenuKey;
	private static KeyMapping toggleKey;

	@Override
	public void onInitializeClient() {
		BeaconConfig.load();
		SpawnerTracker.register();
		BeamRenderer.register();

		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath(MOD_ID, "main"));

		// Auf deutscher Tastatur ist die Ue-Taste die Taste an der US-Position "[" (GLFW: LEFT_BRACKET).
		openMenuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.spawnerbeacon.open_menu",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_LBRACKET,
				category));

		// Schnell-Schalter: standardmaessig nicht belegt (-1), im Steuerungsmenue frei waehlbar.
		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.spawnerbeacon.toggle",
				InputConstants.Type.KEYSYM,
				-1,
				category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openMenuKey.consumeClick()) {
				if (client.screen == null && client.level != null) {
					client.setScreen(new ConfigScreen());
				}
			}
			while (toggleKey.consumeClick()) {
				BeaconConfig cfg = BeaconConfig.get();
				cfg.enabled = !cfg.enabled;
				cfg.save();
				if (client.player != null) {
					client.player.sendSystemMessage(Component.translatable(cfg.enabled ? "sb.msg.on" : "sb.msg.off"));
				}
			}
		});
	}
}

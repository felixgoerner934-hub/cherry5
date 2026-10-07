package com.spawnerbeacon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/** Einstellungen der Mod, gespeichert in config/spawnerbeacon.json */
public final class BeaconConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Pattern HEX = Pattern.compile("^#?([0-9a-fA-F]{6})$");

	/** Spawner-Arten, die im Menue einstellbar sind. */
	public static final List<String> TYPES = List.of(
			"zombie", "skeleton", "spider", "cave_spider", "blaze", "silverfish",
			"magma_cube", "husk", "stray", "slime", "creeper", "other");

	private static final Map<String, String> DEFAULT_COLORS = Map.ofEntries(
			Map.entry("zombie", "55FF55"),
			Map.entry("skeleton", "FFFFFF"),
			Map.entry("spider", "FF2D55"),
			Map.entry("cave_spider", "00C8FF"),
			Map.entry("blaze", "FF9500"),
			Map.entry("silverfish", "AAAAAA"),
			Map.entry("magma_cube", "FF5500"),
			Map.entry("husk", "D2B48C"),
			Map.entry("stray", "99CCFF"),
			Map.entry("slime", "A0FF00"),
			Map.entry("creeper", "00AA00"),
			Map.entry("other", "FF7A93"));

	// Grundeinstellungen
	public boolean enabled = true;
	public double thickness = 0.5;
	public double opacity = 0.55;
	public int maxY = 300;
	public int maxDistance = 256;

	// Effekte
	public boolean showCore = true;
	public boolean pulse = false;
	public boolean distanceFade = true;
	public boolean blockHighlight = true;
	public boolean rainbow = false;
	public boolean notifyChat = true;
	public boolean notifySound = true;

	// Farben
	public String defaultColor = "FFFFFF";
	public Map<String, String> typeColors = new LinkedHashMap<>();
	public Map<String, Boolean> typeEnabled = new LinkedHashMap<>();

	private static BeaconConfig instance;

	public static BeaconConfig get() {
		if (instance == null) {
			instance = load();
		}
		return instance;
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("spawnerbeacon.json");
	}

	public static BeaconConfig load() {
		BeaconConfig cfg = null;
		try {
			Path f = file();
			if (Files.exists(f)) {
				cfg = GSON.fromJson(Files.readString(f), BeaconConfig.class);
			}
		} catch (Exception e) {
			System.err.println("[SpawnerBeacon] Konfiguration konnte nicht gelesen werden, nutze Standardwerte: " + e);
		}
		if (cfg == null) {
			cfg = new BeaconConfig();
		}
		cfg.sanitize();
		instance = cfg;
		return cfg;
	}

	public void save() {
		try {
			Files.createDirectories(file().getParent());
			Files.writeString(file(), GSON.toJson(this));
		} catch (IOException e) {
			System.err.println("[SpawnerBeacon] Konfiguration konnte nicht gespeichert werden: " + e);
		}
	}

	public void resetToDefaults() {
		BeaconConfig d = new BeaconConfig();
		d.sanitize();
		this.enabled = d.enabled;
		this.thickness = d.thickness;
		this.opacity = d.opacity;
		this.maxY = d.maxY;
		this.maxDistance = d.maxDistance;
		this.showCore = d.showCore;
		this.pulse = d.pulse;
		this.distanceFade = d.distanceFade;
		this.blockHighlight = d.blockHighlight;
		this.rainbow = d.rainbow;
		this.notifyChat = d.notifyChat;
		this.notifySound = d.notifySound;
		this.defaultColor = d.defaultColor;
		this.typeColors = d.typeColors;
		this.typeEnabled = d.typeEnabled;
	}

	/** 0 = Dezent, 1 = Auffaellig, 2 = Neon */
	public void applyPreset(int preset) {
		switch (preset) {
			case 0 -> {
				thickness = 0.3;
				opacity = 0.35;
				showCore = false;
				pulse = false;
				distanceFade = true;
				blockHighlight = false;
			}
			case 1 -> {
				thickness = 0.8;
				opacity = 0.7;
				showCore = true;
				pulse = false;
				distanceFade = true;
				blockHighlight = true;
			}
			default -> {
				thickness = 1.0;
				opacity = 0.85;
				showCore = true;
				pulse = true;
				distanceFade = false;
				blockHighlight = true;
			}
		}
	}

	private void sanitize() {
		if (typeColors == null) {
			typeColors = new LinkedHashMap<>();
		}
		if (typeEnabled == null) {
			typeEnabled = new LinkedHashMap<>();
		}
		for (String t : TYPES) {
			String v = normalizeHex(typeColors.get(t));
			typeColors.put(t, v != null ? v : DEFAULT_COLORS.get(t));
			typeEnabled.putIfAbsent(t, Boolean.TRUE);
		}
		String d = normalizeHex(defaultColor);
		defaultColor = d != null ? d : "FFFFFF";
		thickness = Math.max(0.1, Math.min(5.0, thickness));
		opacity = Math.max(0.05, Math.min(1.0, opacity));
		maxY = Math.max(64, Math.min(512, maxY));
		maxDistance = Math.max(32, Math.min(1024, maxDistance));
	}

	/** Gibt "RRGGBB" (gross) zurueck oder null, wenn der Text kein gueltiger Hex-Wert ist. */
	public static String normalizeHex(String s) {
		if (s == null) {
			return null;
		}
		var m = HEX.matcher(s.trim());
		return m.matches() ? m.group(1).toUpperCase() : null;
	}

	public boolean isTypeEnabled(String type) {
		Boolean b = typeEnabled.get(type);
		return b == null || b;
	}

	public void setTypeEnabled(String type, boolean value) {
		typeEnabled.put(type, value);
	}

	public void setTypeColor(String type, int rgb) {
		typeColors.put(type, String.format("%06X", rgb & 0xFFFFFF));
	}

	/** Farbe (0xRRGGBB) fuer eine Spawner-Art, z. B. "zombie". */
	public int colorFor(String type) {
		String hex = typeColors.get(type);
		if (hex == null) {
			hex = defaultColor;
		}
		try {
			return Integer.parseInt(hex, 16);
		} catch (NumberFormatException e) {
			return 0xFFFFFF;
		}
	}
}

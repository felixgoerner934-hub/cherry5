package com.spawnerbeacon;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Einstellungsmenue (Taste Ue) im Kirschbaum-Design mit Tabs und Farbwaehler. */
public class ConfigScreen extends Screen {
	private enum Tab {
		GENERAL("sb.tab.general"),
		COLORS("sb.tab.colors"),
		EFFECTS("sb.tab.effects"),
		FOUND("sb.tab.found");

		final String key;

		Tab(String key) {
			this.key = key;
		}
	}

	private static final int[] PALETTE = {
			0xFF2D55, 0xC8203C, 0x8B1A2B, 0xFF7A93, 0xFFB7C5, 0xFF9500, 0xFFD60A, 0x55FF55,
			0x00C2A8, 0x00C8FF, 0x3B82F6, 0xA855F7, 0xFFFFFF, 0xAAAAAA, 0x555555, 0x000000
	};

	private static Tab tab = Tab.GENERAL;
	private static String selectedType = "zombie";

	private final BeaconConfig cfg = BeaconConfig.get();
	private final ColorState picker = new ColorState();

	private int px;
	private int py;
	private int pw;
	private int ph;
	private int cx0;
	private int cy0;
	private int cw;

	public ConfigScreen() {
		super(Component.translatable("sb.title"));
	}

	private static String tr(String key, Object... args) {
		return Component.translatable(key, args).getString();
	}

	@Override
	protected void init() {
		pw = Math.min(this.width - 16, 360);
		ph = Math.min(this.height - 16, 250);
		px = (this.width - pw) / 2;
		py = (this.height - ph) / 2;
		cx0 = px + 14;
		cy0 = py + 50;
		cw = pw - 28;

		// 1) Hintergrund-Panel (muss zuerst hinzugefuegt werden, damit alles darueber liegt)
		this.addRenderableWidget(new PanelWidget(this.width, this.height, px, py, pw, ph, tr("sb.title")));

		// 2) Tabs
		Tab[] tabs = Tab.values();
		int gap = 4;
		int tw = (pw - 24 - gap * (tabs.length - 1)) / tabs.length;
		int tx = px + 12;
		for (Tab t : tabs) {
			ThemedButton b = new ThemedButton(tx, py + 25, tw, 16, ThemedButton.Kind.TAB,
					() -> tr(t.key), () -> {
						tab = t;
						this.rebuildWidgets();
					}).selected(() -> tab == t);
			this.addRenderableWidget(b);
			tx += tw + gap;
		}

		// 3) Inhalt je nach Tab
		switch (tab) {
			case GENERAL -> buildGeneral();
			case COLORS -> buildColors();
			case EFFECTS -> buildEffects();
			case FOUND -> {
			}
		}

		// 4) Fusszeile
		int fy = py + ph - 24;
		this.addRenderableWidget(new ThemedButton(px + 14, fy, 90, 16, ThemedButton.Kind.NORMAL,
				() -> tr("sb.footer.reset"), () -> {
					cfg.resetToDefaults();
					this.rebuildWidgets();
				}));
		this.addRenderableWidget(new ThemedButton(px + pw - 14 - 90, fy, 90, 16, ThemedButton.Kind.ACCENT,
				() -> tr("sb.footer.done"), this::onClose));
	}

	private void toggleRow(int y, String labelKey, java.util.function.BooleanSupplier get, Runnable flip) {
		this.addRenderableWidget(new ThemedButton(cx0, y, Math.min(cw, 210), 16, ThemedButton.Kind.NORMAL,
				() -> tr(labelKey), flip).toggle(get).left());
	}

	private void buildGeneral() {
		int colW = Math.min(cw, 210);
		toggleRow(cy0, "sb.general.enabled", () -> cfg.enabled, () -> cfg.enabled = !cfg.enabled);

		int y = cy0 + 24;
		this.addRenderableWidget(new ThemedSlider(cx0, y, colW, 22, tr("sb.general.thickness"),
				0.1, 5.0, cfg.thickness, "%.1f", v -> cfg.thickness = v));
		y += 28;
		this.addRenderableWidget(new ThemedSlider(cx0, y, colW, 22, tr("sb.general.opacity"),
				0.05, 1.0, cfg.opacity, "%.2f", v -> cfg.opacity = v));
		y += 28;
		this.addRenderableWidget(new ThemedSlider(cx0, y, colW, 22, tr("sb.general.height"),
				64, 512, cfg.maxY, "%.0f", v -> cfg.maxY = (int) Math.round(v)));
		y += 28;
		this.addRenderableWidget(new ThemedSlider(cx0, y, colW, 22, tr("sb.general.range"),
				32, 1024, cfg.maxDistance, "%.0f m", v -> cfg.maxDistance = (int) Math.round(v)));

		int rx = cx0 + 226;
		int rw = cw - 226;
		if (rw >= 60) {
			String[] keys = { "sb.preset.subtle", "sb.preset.bold", "sb.preset.neon" };
			for (int i = 0; i < 3; i++) {
				final int preset = i;
				final String key = keys[i];
				this.addRenderableWidget(new ThemedButton(rx, cy0 + 18 + i * 20, rw, 16, ThemedButton.Kind.NORMAL,
						() -> tr(key), () -> {
							cfg.applyPreset(preset);
							this.rebuildWidgets();
						}));
			}
		}
	}

	private void buildEffects() {
		toggleRow(cy0, "sb.fx.core", () -> cfg.showCore, () -> cfg.showCore = !cfg.showCore);
		toggleRow(cy0 + 20, "sb.fx.pulse", () -> cfg.pulse, () -> cfg.pulse = !cfg.pulse);
		toggleRow(cy0 + 40, "sb.fx.fade", () -> cfg.distanceFade, () -> cfg.distanceFade = !cfg.distanceFade);
		toggleRow(cy0 + 60, "sb.fx.highlight", () -> cfg.blockHighlight, () -> cfg.blockHighlight = !cfg.blockHighlight);
		toggleRow(cy0 + 80, "sb.fx.chat", () -> cfg.notifyChat, () -> cfg.notifyChat = !cfg.notifyChat);
		toggleRow(cy0 + 100, "sb.fx.sound", () -> cfg.notifySound, () -> cfg.notifySound = !cfg.notifySound);
	}

	private void pickerChanged() {
		cfg.setTypeColor(selectedType, picker.rgb());
	}

	private void buildColors() {
		if (!BeaconConfig.TYPES.contains(selectedType)) {
			selectedType = BeaconConfig.TYPES.get(0);
		}
		picker.setRgb(cfg.colorFor(selectedType));

		// Liste der Spawner-Arten links
		for (int i = 0; i < BeaconConfig.TYPES.size(); i++) {
			final String type = BeaconConfig.TYPES.get(i);
			int y = cy0 + i * 13;
			this.addRenderableWidget(new ThemedButton(cx0, y, 104, 12, ThemedButton.Kind.NORMAL,
					() -> SpawnerTracker.typeName(type), () -> {
						selectedType = type;
						this.rebuildWidgets();
					}).swatch(() -> cfg.colorFor(type)).selected(() -> type.equals(selectedType)).left());
			this.addRenderableWidget(new ThemedButton(cx0 + 106, y, 24, 12, ThemedButton.Kind.NORMAL,
					() -> "", () -> cfg.setTypeEnabled(type, !cfg.isTypeEnabled(type)))
					.toggle(() -> cfg.isTypeEnabled(type)));
		}

		// Farbwaehler rechts
		int rx = cx0 + 142;
		this.addRenderableWidget(new SvSquare(rx, cy0, 80, 80, picker, this::pickerChanged));
		this.addRenderableWidget(new HueBar(rx + 88, cy0, 10, 80, picker, this::pickerChanged));

		// Farbpalette (zwei Reihen)
		for (int i = 0; i < PALETTE.length; i++) {
			final int color = PALETTE[i];
			int x = rx + (i % 8) * 14;
			int y = cy0 + 92 + (i / 8) * 14;
			this.addRenderableWidget(new ThemedButton(x, y, 11, 11, ThemedButton.Kind.SWATCH,
					() -> "", () -> {
						picker.setRgb(color);
						pickerChanged();
					}).swatch(() -> color).selected(() -> picker.rgb() == color));
		}

		// Regenbogen-Schalter
		this.addRenderableWidget(new ThemedButton(rx, cy0 + 124, Math.min(150, cw - 142), 14,
				ThemedButton.Kind.NORMAL, () -> tr("sb.colors.rainbow"),
				() -> cfg.rainbow = !cfg.rainbow).toggle(() -> cfg.rainbow).left());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		switch (tab) {
			case GENERAL -> {
				int rx = cx0 + 226;
				if (cw - 226 >= 60) {
					graphics.text(this.font, tr("sb.preset.title"), rx, cy0 + 4, Theme.PINK, false);
				}
			}
			case COLORS -> drawColorsExtras(graphics);
			case EFFECTS -> graphics.text(this.font, tr("sb.fx.hint"), cx0, cy0 + 124, Theme.TEXT_DIM, false);
			case FOUND -> drawFound(graphics);
		}
	}

	private void drawColorsExtras(GuiGraphicsExtractor graphics) {
		int rx = cx0 + 142;
		int vx = rx + 106;

		// Vorschau der gewaehlten Farbe
		graphics.fill(vx - 1, cy0 - 1, vx + 45, cy0 + 29, 0xFF1A0A0E);
		graphics.fill(vx, cy0, vx + 44, cy0 + 28, 0xFF000000 | picker.rgb());

		graphics.text(this.font, SpawnerTracker.typeName(selectedType), vx, cy0 + 34,
				0xFF000000 | picker.rgb(), true);
		graphics.text(this.font, String.format("#%06X", picker.rgb()), vx, cy0 + 46, Theme.TEXT_DIM, false);
		if (cfg.rainbow) {
			graphics.text(this.font, tr("sb.colors.rainbow_on"), vx, cy0 + 60, Theme.CHERRY_LIGHT, false);
		}
	}

	private void drawFound(GuiGraphicsExtractor graphics) {
		List<SpawnerTracker.SpawnerInfo> list = SpawnerTracker.found();
		graphics.text(this.font, tr("sb.found.title", list.size()), cx0, cy0, Theme.PINK, true);
		if (list.isEmpty()) {
			graphics.text(this.font, tr("sb.found.none"), cx0, cy0 + 16, Theme.TEXT_DIM, false);
			return;
		}
		int shown = Math.min(list.size(), 11);
		for (int i = 0; i < shown; i++) {
			SpawnerTracker.SpawnerInfo info = list.get(i);
			int y = cy0 + 14 + i * 12;
			int color = 0xFF000000 | cfg.colorFor(info.type());
			graphics.fill(cx0, y + 1, cx0 + 5, y + 8, color);
			graphics.text(this.font, SpawnerTracker.typeName(info.type()), cx0 + 9, y, color, false);
			graphics.text(this.font, info.x() + " " + info.y() + " " + info.z(), cx0 + 110, y, Theme.TEXT, false);
			String dist = Math.round(info.distance()) + " m";
			graphics.text(this.font, dist, cx0 + cw - this.font.width(dist), y, Theme.TEXT_DIM, false);
		}
	}

	@Override
	public void onClose() {
		cfg.save();
		super.onClose();
	}
}

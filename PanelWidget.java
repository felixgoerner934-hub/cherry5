package com.spawnerbeacon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

/** Zeichnet Hintergrund, Panel, Kirschast und fallende Blueten. Ist nicht anklickbar. */
public class PanelWidget extends AbstractWidget {
	private final int px;
	private final int py;
	private final int pw;
	private final int ph;
	private final String title;

	public PanelWidget(int screenW, int screenH, int px, int py, int pw, int ph, String title) {
		super(0, 0, screenW, screenH, Component.empty());
		this.px = px;
		this.py = py;
		this.pw = pw;
		this.ph = ph;
		this.title = title;
		this.active = false;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		// dunkler Weinrot-Schleier ueber der Welt
		g.fillGradient(0, 0, this.width, this.height, 0xA0180509, 0xD00C0206);

		// Schatten + Rahmen (Rinde) + Panel
		g.fill(px + 3, py + 4, px + pw + 3, py + ph + 4, 0x66000000);
		Theme.roundRect(g, px, py, pw, ph, Theme.BARK);
		Theme.roundRect(g, px + 2, py + 2, pw - 4, ph - 4, Theme.PANEL);

		// Kopfzeile + Kirsch-Linie
		g.fillGradient(px + 2, py + 2, px + pw - 2, py + 20, 0xFF3A1520, 0xFF2A1018);
		g.fill(px + 2, py + 20, px + pw - 2, py + 21, Theme.CHERRY);

		// fallende Blueten
		long t = System.currentTimeMillis();
		for (int i = 0; i < 14; i++) {
			float speed = 0.010f + (i % 4) * 0.005f;
			float fy = (t * speed + i * 53f) % (ph - 6);
			float fx = ((i * 61) % (pw - 14)) + (float) Math.sin(t * 0.0015 + i * 1.7) * 6f;
			int x = px + 4 + Math.max(0, Math.min(pw - 14, (int) fx));
			int y = py + 3 + (int) fy;
			int color = switch (i % 3) {
				case 0 -> 0xB0FF9DB2;
				case 1 -> 0xA0E8456B;
				default -> 0x90FFD1DC;
			};
			g.fill(x, y, x + 3, y + 2, color);
		}

		// Ast + Titel
		Theme.branch(g, px + pw - 8, py + 9);
		Theme.flower(g, px + 10, py + 6, 2);
		g.text(Minecraft.getInstance().font, title, px + 24, py + 7, Theme.BLOSSOM, true);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
	}
}

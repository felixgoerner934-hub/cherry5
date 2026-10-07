package com.spawnerbeacon;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Senkrechter Farbton-Streifen (Regenbogen). */
public class HueBar extends AbstractWidget {
	private final ColorState state;
	private final Runnable onChange;

	public HueBar(int x, int y, int w, int h, ColorState state, Runnable onChange) {
		super(x, y, w, h, Component.empty());
		this.state = state;
		this.onChange = onChange;
	}

	private void set(double my) {
		double v = (my - getY()) / (this.height - 1);
		state.h = (float) Math.max(0, Math.min(0.9999, v));
		onChange.run();
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		set(event.y());
	}

	@Override
	public void onDrag(MouseButtonEvent event, double dragX, double dragY) {
		set(event.y());
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		int x = getX();
		int y = getY();
		int w = this.width;
		int h = this.height;

		g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF1A0A0E);
		for (int k = 0; k < 6; k++) {
			int y0 = y + (k * h) / 6;
			int y1 = y + ((k + 1) * h) / 6;
			int c0 = 0xFF000000 | ColorState.hsvToRgb(k / 6f, 1f, 1f);
			int c1 = 0xFF000000 | ColorState.hsvToRgb((k + 1) / 6f, 1f, 1f);
			g.fillGradient(x, y0, x + w, y1, c0, c1);
		}

		int my = y + Math.round(state.h * (h - 1));
		g.fill(x - 2, my - 2, x + w + 2, my + 3, 0xFF000000);
		g.fill(x - 1, my - 1, x + w + 1, my + 2, 0xFFFFFFFF);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
	}
}

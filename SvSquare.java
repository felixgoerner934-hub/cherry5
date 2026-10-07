package com.spawnerbeacon;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Farbfeld: links nach rechts Saettigung, oben nach unten Helligkeit. */
public class SvSquare extends AbstractWidget {
	private final ColorState state;
	private final Runnable onChange;

	public SvSquare(int x, int y, int w, int h, ColorState state, Runnable onChange) {
		super(x, y, w, h, Component.empty());
		this.state = state;
		this.onChange = onChange;
	}

	private void set(double mx, double my) {
		state.s = (float) Math.max(0, Math.min(1, (mx - getX()) / (this.width - 1)));
		state.v = (float) Math.max(0, Math.min(1, 1 - (my - getY()) / (this.height - 1)));
		onChange.run();
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		set(event.x(), event.y());
	}

	@Override
	public void onDrag(MouseButtonEvent event, double dragX, double dragY) {
		set(event.x(), event.y());
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		int x = getX();
		int y = getY();
		int w = this.width;
		int h = this.height;

		g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF1A0A0E);
		int cols = w / 2;
		for (int c = 0; c < cols; c++) {
			float s = c / (float) (cols - 1);
			int top = 0xFF000000 | ColorState.hsvToRgb(state.h, s, 1f);
			g.fillGradient(x + c * 2, y, x + c * 2 + 2, y + h, top, 0xFF000000);
		}

		int mx = x + Math.round(state.s * (w - 1));
		int my = y + Math.round((1f - state.v) * (h - 1));
		g.fill(mx - 3, my - 3, mx + 4, my + 4, 0xFF000000);
		g.fill(mx - 2, my - 2, mx + 3, my + 3, 0xFFFFFFFF);
		g.fill(mx - 1, my - 1, mx + 2, my + 2, 0xFF000000 | state.rgb());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
	}
}

package com.spawnerbeacon;

import java.util.Locale;
import java.util.function.DoubleConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Schmaler Slider im Kirschbaum-Design: Beschriftung + Wert oben, Leiste unten. */
public class ThemedSlider extends AbstractWidget {
	private final String label;
	private final double min;
	private final double max;
	private final String format;
	private final DoubleConsumer setter;
	private double value01;

	public ThemedSlider(int x, int y, int w, int h, String label, double min, double max,
			double current, String format, DoubleConsumer setter) {
		super(x, y, w, h, Component.empty());
		this.label = label;
		this.min = min;
		this.max = max;
		this.format = format;
		this.setter = setter;
		this.value01 = Math.max(0, Math.min(1, (current - min) / (max - min)));
	}

	private double actual() {
		return min + (max - min) * value01;
	}

	private void setFromMouse(double mouseX) {
		double v = (mouseX - (getX() + 3)) / (this.width - 6);
		this.value01 = Math.max(0, Math.min(1, v));
		setter.accept(actual());
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		setFromMouse(event.x());
	}

	@Override
	public void onDrag(MouseButtonEvent event, double dragX, double dragY) {
		setFromMouse(event.x());
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		Font font = Minecraft.getInstance().font;
		int x = getX();
		int y = getY();
		int w = this.width;
		int h = this.height;

		String valueText = String.format(Locale.ROOT, format, actual());
		g.text(font, label, x, y + 1, Theme.TEXT, false);
		g.text(font, valueText, x + w - font.width(valueText), y + 1, Theme.PINK, false);

		int trackY = y + h - 7;
		Theme.roundRect(g, x, trackY, w, 4, 0xFF3A2026);
		int fillW = (int) Math.round((w - 6) * value01) + 3;
		Theme.roundRect(g, x, trackY, fillW, 4, this.isHovered() ? Theme.CHERRY_LIGHT : Theme.CHERRY);

		int kx = x + 3 + (int) Math.round((w - 6) * value01);
		g.fill(kx - 3, trackY - 3, kx + 3, trackY + 7, 0xFF1A0A0E);
		g.fill(kx - 2, trackY - 2, kx + 2, trackY + 6, Theme.BLOSSOM);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
	}
}

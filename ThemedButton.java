package com.spawnerbeacon;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Flacher Button im Kirschbaum-Design (Normal, Tab, Akzent, Farbfeld). Optional mit Schalter und Farbmuster. */
public class ThemedButton extends AbstractWidget {
	public enum Kind { NORMAL, TAB, ACCENT, SWATCH }

	private final Kind kind;
	private final Supplier<String> label;
	private final Runnable action;
	private BooleanSupplier selected = () -> false;
	private BooleanSupplier toggle = null;
	private IntSupplier swatch = null;
	private boolean leftAligned = false;

	public ThemedButton(int x, int y, int w, int h, Kind kind, Supplier<String> label, Runnable action) {
		super(x, y, w, h, Component.empty());
		this.kind = kind;
		this.label = label;
		this.action = action;
	}

	public ThemedButton selected(BooleanSupplier s) {
		this.selected = s;
		return this;
	}

	public ThemedButton toggle(BooleanSupplier t) {
		this.toggle = t;
		return this;
	}

	public ThemedButton swatch(IntSupplier s) {
		this.swatch = s;
		return this;
	}

	public ThemedButton left() {
		this.leftAligned = true;
		return this;
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		action.run();
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		int x = getX();
		int y = getY();
		int w = this.width;
		int h = this.height;
		boolean hov = this.isHovered();
		boolean sel = selected.getAsBoolean();
		Font font = Minecraft.getInstance().font;

		if (kind == Kind.SWATCH) {
			int outline = sel ? 0xFFFFFFFF : (hov ? Theme.PINK : 0xFF1A0A0E);
			g.fill(x - 1, y - 1, x + w + 1, y + h + 1, outline);
			g.fill(x, y, x + w, y + h, 0xFF000000 | swatch.getAsInt());
			return;
		}

		int bg;
		switch (kind) {
			case TAB -> bg = sel ? 0xFF6E1F33 : (hov ? 0xFF3B1B23 : 0xFF2A1318);
			case ACCENT -> bg = hov ? 0xFFD62F45 : 0xFFB3202F;
			default -> bg = sel ? 0xFF5A1F30 : (hov ? 0xFF4A222B : 0xFF34191F);
		}
		Theme.roundRect(g, x, y, w, h, bg);

		if (kind == Kind.TAB && sel) {
			g.fill(x + 2, y + h - 2, x + w - 2, y + h, Theme.PINK);
		} else if (kind == Kind.NORMAL && sel) {
			g.fill(x, y + 1, x + 2, y + h - 1, Theme.PINK);
		}

		int textColor = this.active ? Theme.TEXT : Theme.TEXT_OFF;
		String text = label.get();
		int textY = y + (h - 8) / 2;
		int textX;
		if (leftAligned) {
			textX = x + (swatch != null ? 15 : 6);
		} else {
			textX = x + (w - font.width(text)) / 2;
		}

		if (swatch != null) {
			int sy = y + (h - 7) / 2;
			g.fill(x + 4, sy, x + 11, sy + 7, 0xFF1A0A0E);
			g.fill(x + 5, sy + 1, x + 10, sy + 6, 0xFF000000 | swatch.getAsInt());
		}
		if (!text.isEmpty()) {
			g.text(font, text, textX, textY, textColor, false);
		}

		if (toggle != null) {
			boolean on = toggle.getAsBoolean();
			int pw = 16;
			int ph = 8;
			int tx = text.isEmpty() ? x + (w - pw) / 2 : x + w - pw - 4;
			int ty = y + (h - ph) / 2;
			Theme.roundRect(g, tx, ty, pw, ph, on ? Theme.CHERRY : 0xFF4A3A3E);
			int kx = on ? tx + pw - 7 : tx + 1;
			g.fill(kx, ty + 1, kx + 6, ty + ph - 1, on ? Theme.BLOSSOM : 0xFFB89AA0);
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
	}
}

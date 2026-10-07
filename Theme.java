package com.spawnerbeacon;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Farben und Zeichenhilfen fuer das "Kirschbaum"-Design (dunkle Rinde, rote Kirschbluete). */
public final class Theme {
	public static final int PANEL = 0xF0241014;
	public static final int BARK = 0xFF4A2A20;
	public static final int BARK_LIGHT = 0xFF7A4632;
	public static final int CHERRY = 0xFFC8203C;
	public static final int CHERRY_LIGHT = 0xFFE8456B;
	public static final int PINK = 0xFFFF9DB2;
	public static final int BLOSSOM = 0xFFFFD1DC;
	public static final int TEXT = 0xFFF5E6E8;
	public static final int TEXT_DIM = 0xFFB89AA0;
	public static final int TEXT_OFF = 0xFF8A7075;

	private Theme() {
	}

	/** Rechteck mit leicht abgerundeten Ecken (1 Pixel). */
	public static void roundRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
		g.fill(x + 1, y, x + w - 1, y + h, color);
		g.fill(x, y + 1, x + w, y + h - 1, color);
	}

	/** Kleine Kirschbluete aus Pixeln (Groesse 3s x 3s). */
	public static void flower(GuiGraphicsExtractor g, int x, int y, int s) {
		g.fill(x + s, y, x + 2 * s, y + s, PINK);
		g.fill(x + s, y + 2 * s, x + 2 * s, y + 3 * s, PINK);
		g.fill(x, y + s, x + s, y + 2 * s, PINK);
		g.fill(x + 2 * s, y + s, x + 3 * s, y + 2 * s, PINK);
		g.fill(x + s, y + s, x + 2 * s, y + 2 * s, CHERRY);
	}

	/** Ast, der von rechts oben ins Bild ragt, mit Bluetenzweigen. */
	public static void branch(GuiGraphicsExtractor g, int x0, int y0) {
		for (int i = 0; i <= 34; i++) {
			int bx = x0 - i * 2;
			int by = y0 + (i * 8) / 34;
			g.fill(bx, by, bx + 3, by + 2, BARK_LIGHT);
			if (i < 10) {
				g.fill(bx, by + 2, bx + 3, by + 3, BARK);
			}
		}
		twig(g, x0 - 20, y0 + 2, 6);
		twig(g, x0 - 44, y0 + 5, 5);
		flower(g, x0 - 72, y0 + 5, 2);
		flower(g, x0 - 28, y0 - 8, 2);
		flower(g, x0 - 50, y0 - 4, 2);
		flower(g, x0 - 8, y0 + 4, 2);
	}

	private static void twig(GuiGraphicsExtractor g, int x, int y, int len) {
		for (int j = 0; j < len; j++) {
			g.fill(x - j, y - j, x - j + 2, y - j + 1, BARK_LIGHT);
		}
	}
}

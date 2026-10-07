package com.spawnerbeacon;

/** Farbe im HSV-Modell (Farbton, Saettigung, Helligkeit), jeweils 0..1. */
public final class ColorState {
	public float h = 0f;
	public float s = 1f;
	public float v = 1f;

	public int rgb() {
		return hsvToRgb(h, s, v);
	}

	public void setRgb(int rgb) {
		float r = ((rgb >> 16) & 0xFF) / 255f;
		float g = ((rgb >> 8) & 0xFF) / 255f;
		float b = (rgb & 0xFF) / 255f;
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float d = max - min;
		float hue;
		if (d == 0f) {
			hue = this.h; // Grau: Farbton behalten
		} else if (max == r) {
			hue = ((g - b) / d) % 6f;
		} else if (max == g) {
			hue = (b - r) / d + 2f;
		} else {
			hue = (r - g) / d + 4f;
		}
		hue /= 6f;
		if (hue < 0f) {
			hue += 1f;
		}
		this.h = Math.min(hue, 0.9999f);
		this.s = max == 0f ? 0f : d / max;
		this.v = max;
	}

	public static int hsvToRgb(float h, float s, float v) {
		float hh = (h - (float) Math.floor(h)) * 6f;
		int i = (int) hh;
		float f = hh - i;
		float p = v * (1f - s);
		float q = v * (1f - s * f);
		float t = v * (1f - s * (1f - f));
		float r;
		float g;
		float b;
		switch (i % 6) {
			case 0 -> { r = v; g = t; b = p; }
			case 1 -> { r = q; g = v; b = p; }
			case 2 -> { r = p; g = v; b = t; }
			case 3 -> { r = p; g = q; b = v; }
			case 4 -> { r = t; g = p; b = v; }
			default -> { r = v; g = p; b = q; }
		}
		return (Math.round(r * 255) << 16) | (Math.round(g * 255) << 8) | Math.round(b * 255);
	}
}

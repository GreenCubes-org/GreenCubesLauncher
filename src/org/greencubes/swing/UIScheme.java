package org.greencubes.swing;

import java.awt.Color;

public class UIScheme {
	
	public static final Color BACKGROUND = getColor(0x0B1818);
	public static final Color BIG_BUTTON = getColor(0x649494);
	public static final Color EMPTY = getColor(0, 0);
	public static final Color TEXT_COLOR = getColor(0xE5FFFF);
	public static final Color TEXT_COLOR_DISABLED = getColor(0x0c1313);
	
	public static final String TEXT_FONT = "Clear Sans Light";
	public static final String TITLE_FONT = "Lato";
	
	public static final Color MAIN_MENU_BG = getColor(0x649494);
	public static final Color TITLE_COLOR = new Color(192, 228, 232, 255);
	public static final Color MAIN_MENU_BG_SEL = getColor(0x89C2C2);
	public static final Color TITLE_COLOR_SEL = new Color(236, 255, 255, 255);
	
	public static final Color PROGRESSBAR_BG = getColor(0x3A5352);
	public static final Color PROGRESSBAR_BAR = getColor(0xB0E6EE);
	public static final Color PROGRESSBAR_BORDER = getColor(0x628589);
	
	public static final Color TOP_PANEL_BG = getColor(0x3F5856);
	
	public static final Color MENU_BG = getColor(0x1A2B2B);
	public static final Color MENU_DD_BG = getColor(0x3D5654);
	public static final Color MENU_BORDER = getColor(0x283C3D);
	public static final Color MENU_DD_BG_SEL = getColor(0x649494);
	
	public static final Color INPUT_BG = getColor(0x395352);
	public static final Color INPUT_BORDER = getColor(0x628589);
	
	public static final Color getColor(int rgb, int alpha) {
		return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha);
	}
	
	public static final Color getColor(int rgb) {
		return getColor(rgb, 255);
	}
}

package org.greencubes.swing;

import java.awt.Color;

public class UIScheme {
	
	public static final Color BACKGROUND = getColor(0x171e1e);
	public static final Color BIG_BUTTON = getColor(0x749393);
	public static final Color BIG_BUTTON_ACTIVE = getColor(0xc0e5ed);
	public static final Color EMPTY = getColor(0, 0);
	public static final Color TEXT_COLOR = getColor(0xecffff);
	
	public static final String TEXT_FONT = "Lato";
	public static final String TITLE_FONT = "Lato";
	public static final String LONG_TEXT_FONG = "Clear Sans Light";
	
	public static final Color MAIN_MENU_BG = getColor(0x739292);
	public static final Color TITLE_COLOR = getColor(0xc0e5ed);
	public static final Color MAIN_MENU_BG_SEL = getColor(0x9bc1c1);
	public static final Color TITLE_COLOR_SEL = TEXT_COLOR;
	
	public static final Color PROGRESSBAR_BG = getColor(0x3A5352);
	public static final Color PROGRESSBAR_BAR = getColor(0xB0E6EE);
	public static final Color PROGRESSBAR_BORDER = getColor(0x628589);
	
	public static final Color TOP_PANEL_BG = getColor(0x495957);
	public static final Color TOP_PANEL_BG_LOGO = getColor(0x475755);
	
	public static final Color MENU_BG = getColor(0x252f2f);
	public static final Color MENU_DD_BG = getColor(0x475755);
	public static final Color MENU_BORDER = getColor(0x333f40);
	public static final Color MENU_DD_BG_SEL = getColor(0x739292);
	
	public static final Color INPUT_BG = getColor(0x252f2f);
	public static final Color INPUT_BORDER = getColor(0x333f40);
	
	public static final Color getColor(int rgb, int alpha) {
		return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha);
	}
	
	public static final Color getColor(int rgb) {
		return getColor(rgb, 255);
	}
}

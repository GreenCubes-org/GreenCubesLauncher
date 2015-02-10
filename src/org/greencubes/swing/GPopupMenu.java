package org.greencubes.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicMenuItemUI;

public class GPopupMenu extends JPopupMenu {
	
	private boolean up;
	private Color background;
	private Color foreground;
	private Color selectedBackground;
	private Color selectedForeground;
	private Font font;
	private Dimension size;
	private int menuPadding = 0;
	private Border menuBorder = BorderFactory.createEmptyBorder();
	
	public GPopupMenu(boolean up) {
		this.up = up;
	}
	
	public JMenuItem addItem(String title, String iconPath) {
		JMenuItem item = new JMenuItem(title, iconPath == null ? null : new ImageIcon(GPopupMenu.class.getResource(iconPath))) {{
			setBackground(GPopupMenu.this.background);
			setForeground(GPopupMenu.this.foreground);
			setFont(GPopupMenu.this.font);
			setUI(new BasicMenuItemUI() {{
				selectionBackground = GPopupMenu.this.selectedBackground;
				selectionForeground = GPopupMenu.this.selectedForeground;
			}});
			if(GPopupMenu.this.size != null) {
				setPreferredSize(GPopupMenu.this.size);
				setMinimumSize(GPopupMenu.this.size);
				setMaximumSize(new Dimension(GPopupMenu.this.size.width, 9999));
			}
			setBorder(menuBorder);
		}};
		add(item);
		return item;
	}
	
	public void setMenuBorder(Border border) {
		this.menuBorder = border;
	}
	
	public void setMenuFont(Font font) {
		this.font = font;
	}
	
	public void setMenuSize(Dimension dimension) {
		this.size = dimension;
	}
	
	public void setMenuColors(Color background, Color foreground, Color selectedBackground, Color selectedForeground) {
		this.background = background;
		this.foreground = foreground;
		this.selectedBackground = selectedBackground;
		this.selectedForeground = selectedForeground;
	}
	
	public void show(JComponent parent, boolean overlay) {
		realShow(parent, overlay);
		if(up) { // Repeat show for purpose!
			realShow(parent, overlay);
		}
	}
	
	private void realShow(JComponent parent, boolean overlay) {
		int y;
		if(overlay) {
			if(up) {
				y = -getHeight() + parent.getHeight() - menuPadding;
			} else {
				y = menuPadding;
			}
		} else {
			if(up) {
				y = -getHeight() - menuPadding;
			} else {
				y = parent.getHeight() + menuPadding;
			}
		}
		this.show(parent, 0, y);
	}
}

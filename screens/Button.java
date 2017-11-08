package screens;

import javax.swing.JButton;

public class Button extends JButton {
	private int xLoc;
	private int yLoc;
	private int width;
	private int height;
	
	public Button(String name, int x, int y, int width, int height) {
		super(name);
		this.xLoc = x;
		this.yLoc = y;
		this.width = width;
		this.height = height;
		
		this.setBounds(this.xLoc, this.yLoc, this.width, this.height);
	}
}

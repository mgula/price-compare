package screens;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;

public class Label extends JLabel {
	private int xLoc;
	private int yLoc;
	private int width;
	private int height;
	
	public Label(String name, int x, int y, int width, int height) {
		super(name);
		this.xLoc = x;
		this.yLoc = y;
		this.width = width;
		this.height = height;
		
		this.setBounds(this.xLoc, this.yLoc, this.width, this.height);
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.setFont(new Font("GillSansUltraBold", Font.PLAIN, 12));
	}
}

package screens;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

public abstract class NonMenuScreen extends JPanel {
	
	protected Label title;
	
	private Button backButton;
	
	private int screenRatioX = 10;
	private int screenRatioY = 17;
	
	public NonMenuScreen(int w, int h) {
		this.setBackground(Color.LIGHT_GRAY);
		
		/*Initialize back button*/
		this.backButton = new Button("Back to Menu",
				0,
				0,
				w / this.screenRatioX,
				h / this.screenRatioY);
		
		/*Initialize title*/
		this.title = new Label("default",
				(w / 2) - (w / 4),
				h / 30,
				w / 2,
				h / 25);
		this.title.setFont(new Font("GillSansUltraBold", Font.BOLD, 20)); // make a bit larger
	}
	
	public Label getTitle() {
		return this.title;
	}
	
	public Button getBackButton() {
		return this.backButton;
	}
	
	public int getScreenRatioX() {
		return this.screenRatioX;
	}
	
	public int getScreenRatioY() {
		return this.screenRatioY;
	}
}

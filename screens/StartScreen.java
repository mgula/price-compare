package screens;

import java.awt.Color;

import javax.swing.JPanel;

public class StartScreen extends JPanel {
	
	private Button startButton;
	
	public StartScreen(int w, int h) {
		this.setBackground(Color.LIGHT_GRAY);
		
		/*Calculate button positioning*/
		int startButtonWidth = w / 10;
		int startButtonHeight = h / 17;
		int startButtonXLoc = (w / 2) - (startButtonWidth / 2);
		int startButtonYLoc = (h / 2) - (startButtonHeight / 2);
		
		/*Initialize start button*/
		this.startButton = new Button("Start Shopping",
				startButtonXLoc,
				startButtonYLoc,
				startButtonWidth,
				startButtonHeight);
	}
	
	public Button getStartButton() {
		return this.startButton;
	}
}

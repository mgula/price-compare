package screens;

import java.awt.Color;

import javax.swing.JPanel;

public class MenuScreen extends JPanel {

	private Button editCartButton;
	private Button mapButton;
	private Button exitButton;
	
	public MenuScreen(int w, int h) {
		this.setBackground(Color.LIGHT_GRAY);
		
		/*Calculate button positioning*/
		int buttonWidth = w / 10; // same for all buttons
		int buttonHeight = h / 17; // same for all buttons
		int buttonXLoc = (w / 2) - (buttonWidth / 2); // same for all buttons
		int buttonYLoc = h / 7; // baseline for all buttons
		
		/*Initialize buttons*/
		this.editCartButton = new Button("Edit Cart",
				buttonXLoc,
				2 * buttonYLoc,
				buttonWidth,
				buttonHeight);
		
		this.mapButton = new Button("Map",
				buttonXLoc,
				3 * buttonYLoc,
				buttonWidth,
				buttonHeight);
		
		this.exitButton = new Button("Exit",
				buttonXLoc,
				4 * buttonYLoc,
				buttonWidth,
				buttonHeight);
	}
	
	public Button getEditCartButton() {
		return this.editCartButton;
	}
	
	public Button getMapButton() {
		return this.mapButton;
	}
	
	public Button getExitButton() {
		return this.exitButton;
	}
}

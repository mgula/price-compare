package screens;

import classes.Store;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import java.util.ArrayList;

public class MapScreen extends NonMenuScreen {
	
	int screenHeight; // paint will use these
	int screenWidth;
	
	private Button cartButton;
	
	/*Some deception here: this "map" is just a static image of 
	 *Google maps, not interactive in any way. To get an interactive
	 *map, would use Java FX (or something other than Java in general).*/
	private final String mapPath = "imgs/map.png";
	
	private BufferedImage map;
	
	private int mapXLoc;
	private int mapYLoc;
	private int mapWidth;
	private int mapHeight;
	
	private int mapBorderThickness = 2;
	
	private int userXLoc;
	private int userYLoc;
	
	private boolean hoverUser = false;
	
	private ArrayList<Store> stores;
	private Store hoveredStore = null;
	private boolean hovered;
	private int pointDimensions;
	
	private Label closestStoreTitleField;
	private Label closestStoreInfoField;
	private Label closestStoreDistanceField;
	
	private Label selectedStoreTitleField;
	private Label selectedStoreNameField;
	private Label selectedStoreDistanceField;
	private Label selectedStoreCartTotalField;
	private Label selectedStoreMissingItemsField;
	
	private Label cartLabel;
	private Label itemInfoLabel;
	
	private JScrollPane cartListPane;
	private DefaultListModel<String> cartModel;
	private JList<String> cartList;
	
	public MapScreen(int w, int h, int userX, int userY, ArrayList<Store> s, int dim) {
		super(w, h);
		
		this.screenWidth = w;
		this.screenHeight = h;
		this.userXLoc = userX;
		this.userYLoc = userY;
		this.stores = s;
		this.pointDimensions = dim;
		
		/*Update title*/
		this.title.setText("Pick your store");
		
		/*Calculate cart button positioning*/
		int cartButtonWidth = w / this.getScreenRatioX();
		int cartButtonHeight = h / this.getScreenRatioY();
		int cartButtonXLoc = w - cartButtonWidth;
		int cartButtonYLoc = 0;
		
		/*Initialize cart button*/
		this.cartButton = new Button("Cart",
				cartButtonXLoc,
				cartButtonYLoc,
				cartButtonWidth,
				cartButtonHeight);
		
		/*Calculate map dimensions*/
		this.mapXLoc = w / 2;
		this.mapYLoc = h / 3;
		this.mapWidth = w / 2;
		this.mapHeight = h / 2;
		
		/*Set map image*/
		try {
			this.map = ImageIO.read(new File(this.mapPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*Calculate closest store label positioning*/
		int closestStoreFieldsXLoc = this.mapXLoc - (this.mapWidth / 2) + (w / 150);
		int closestStoreFieldsBaseYLoc = this.mapYLoc - (this.mapHeight / 2);
		int closestStoreFieldsWidth = w / 15;
		int closestStoreFieldsHeight = h / 30;
		
		int closestStoreTitleFieldYLoc = closestStoreFieldsBaseYLoc;
		int closestStoreInfoFieldYLoc = closestStoreTitleFieldYLoc + (closestStoreFieldsHeight / 2);
		int closestStoreDistanceFieldYLoc = closestStoreInfoFieldYLoc + (closestStoreFieldsHeight / 2);
		
		/*Initialize closest store labels*/
		this.closestStoreTitleField = new Label("Closest store:",
				closestStoreFieldsXLoc,
				closestStoreTitleFieldYLoc,
				closestStoreFieldsWidth,
				closestStoreFieldsHeight);
		
		this.closestStoreInfoField = new Label("",
				closestStoreFieldsXLoc,
				closestStoreInfoFieldYLoc,
				closestStoreFieldsWidth,
				closestStoreFieldsHeight);
		
		this.closestStoreDistanceField = new Label("",
				closestStoreFieldsXLoc,
				closestStoreDistanceFieldYLoc,
				closestStoreFieldsWidth,
				closestStoreFieldsHeight);
		
		/*Calculate cart pane positioning*/
		int cartListWidth = w / 5;
		int cartListHeight = h / 8;
		int cartListXLoc = w / 4;
		int cartListYLoc = 2 * h / 3;

		/*Initialize cart*/
		this.cartModel = new DefaultListModel<String>();
		this.cartList = new JList<String>(this.cartModel);
		this.cartList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		this.cartListPane = new JScrollPane(this.cartList);
		this.cartListPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.cartListPane.setBounds(cartListXLoc, cartListYLoc, cartListWidth, cartListHeight);
		
		/*Calculate cart label positioning*/
		int cartLabelWidth = w / 10;
		int cartLabelHeight = h / 20;
		int cartLabelXLoc = cartListXLoc + (cartListWidth / 2) - (cartLabelWidth / 2);
		int cartLabelYLoc = cartListYLoc - cartLabelHeight;
		
		/*Initialize cart label*/
		this.cartLabel = new Label("Your cart",
				cartLabelXLoc,
				cartLabelYLoc,
				cartLabelWidth,
				cartLabelHeight);
		this.cartLabel.setFont(new Font("GillSansUltraBold", Font.PLAIN, 15));
		
		/*Calculate item info label positioning*/
		int itemInfoLabelWidth = w / 2;
		int itemInfoLabelHeight = h / 20;
		int itemInfoLabelXLoc = cartListXLoc + (cartListWidth / 2) - (itemInfoLabelWidth / 2);
		int itemInfoLabelYLoc = cartListYLoc + cartListHeight;
		
		/*Initialize item info label*/
		this.itemInfoLabel = new Label("Cart is empty",
				itemInfoLabelXLoc,
				itemInfoLabelYLoc,
				itemInfoLabelWidth,
				itemInfoLabelHeight);
		this.itemInfoLabel.setFont(new Font("GillSansUltraBold", Font.PLAIN, 12));
		
		/*Calculate selected store label positioning*/
		int storeLabelWidth = w / 2;
		int storeLabelHeight = h / 40;
		int storeLabelXLoc = w / 3;
		int storeLabelBaseYLoc = 2 * h / 3;
		
		int titleYLoc = storeLabelBaseYLoc;
		int nameYLoc = titleYLoc + storeLabelHeight;
		int distanceYLoc = nameYLoc + storeLabelHeight;
		int cartTotalYLoc = distanceYLoc + storeLabelHeight;
		int missingItemsYLoc = cartTotalYLoc + storeLabelHeight;
		
		/*Initialize selected store labels*/
		this.selectedStoreTitleField = new Label("Selected store",
				storeLabelXLoc,
				titleYLoc,
				storeLabelWidth,
				storeLabelHeight);
		this.selectedStoreTitleField.setFont(new Font("GillSansUltraBold", Font.PLAIN, 15));
		this.selectedStoreNameField = new Label("Name: -",
				storeLabelXLoc,
				nameYLoc,
				storeLabelWidth,
				storeLabelHeight);
		this.selectedStoreDistanceField = new Label("Distance: -",
				storeLabelXLoc,
				distanceYLoc,
				storeLabelWidth,
				storeLabelHeight);
		this.selectedStoreCartTotalField = new Label("Cart Total: -",
				storeLabelXLoc,
				cartTotalYLoc,
				storeLabelWidth,
				storeLabelHeight);
		this.selectedStoreCartTotalField.setFont(new Font("GillSansUltraBold", Font.BOLD, 14));
		this.selectedStoreMissingItemsField = new Label("Missing Items: -",
				storeLabelXLoc,
				missingItemsYLoc,
				storeLabelWidth,
				storeLabelHeight);
	}
	
	public Button getCartButton() {
		return this.cartButton;
	}
	
	public int getMapXLoc() {
		return this.mapXLoc;
	}
	
	public int getMapYLoc() {
		return this.mapYLoc;
	}
	
	public int getMapWidth() {
		return this.mapWidth;
	}
	
	public int getMapHeight() {
		return this.mapHeight;
	}
	
	public Label getClosestStoreTitleField() {
		return this.closestStoreTitleField;
	}
	
	public Label getClosestStoreInfoField() {
		return this.closestStoreInfoField;
	}
	
	public Label getClosestStoreDistanceField() {
		return this.closestStoreDistanceField;
	}
	
	public Label getSelectedStoreTitleField() {
		return this.selectedStoreTitleField;
	}
	
	public Label getSelectedStoreNameField() {
		return this.selectedStoreNameField;
	}
	
	public Label getSelectedStoreDistanceField() {
		return this.selectedStoreDistanceField;
	}
	
	public Label getSelectedStoreCartTotalField() {
		return this.selectedStoreCartTotalField;
	}
	
	public Label getSelectedStoreMissingItemsField() {
		return this.selectedStoreMissingItemsField;
	}
	
	public JScrollPane getCartListPane() {
		return this.cartListPane;
	}
	
	public DefaultListModel<String> getCartModel() {
		return this.cartModel;
	}
	
	public JList<String> getCartList() {
		return this.cartList;
	}
	
	public Label getCartLabel() {
		return this.cartLabel;
	}
	
	public Label getItemInfoLabel() {
		return this.itemInfoLabel;
	}
	
	public void paint(Graphics g) {
		/*Draw map*/
		g.drawImage(this.map, this.mapXLoc - (this.mapWidth / 2), this.mapYLoc - (this.mapHeight / 2), this.mapWidth, this.mapHeight, null);
		
		/*Draw map border box*/
		for (int i = 0; i < this.mapBorderThickness; i++) {
			g.setColor(Color.BLACK);
			g.drawRect(this.mapXLoc - (this.mapWidth / 2) - i, this.mapYLoc - (this.mapHeight / 2) - i, this.mapWidth + (i * 2), this.mapHeight + (i * 2));
		}
		
		/*Draw border around distance info*/
		g.drawRect(this.mapXLoc - (this.mapWidth / 2), this.mapYLoc - (this.mapHeight / 2), this.screenWidth / 13, this.screenHeight / 17);
		g.setColor(Color.WHITE);
		g.fillRect(this.mapXLoc - (this.mapWidth / 2), this.mapYLoc - (this.mapHeight / 2), this.screenWidth / 13, this.screenHeight / 17);
		
		/*Draw point at user location*/
		g.setColor(Color.RED);
		g.fillRect(this.userXLoc - (this.pointDimensions / 2), this.userYLoc - (this.pointDimensions / 2), this.pointDimensions, this.pointDimensions);
		
		/*Draw store markers*/
		for (Store s : this.stores) {
			g.setColor(Color.BLUE);
			g.fillRect(s.getXLoc() - (this.pointDimensions / 2), s.getYLoc() - (this.pointDimensions / 2), this.pointDimensions, this.pointDimensions);
		}
		
		/*Draw user loc info if hovering over user*/
		g.setColor(Color.BLACK);
		g.setFont(new Font("SansSerif", Font.BOLD, 14));
		if (this.hoverUser) {
			g.drawString("Your location", this.userXLoc, this.userYLoc + (this.screenHeight / 50));
		}
		
		/*Draw hovered store info, if applicable*/
		if (this.hovered) {
			g.drawString(this.hoveredStore.getName() + " (" + this.hoveredStore.getDistanceTo() + " ft)", this.hoveredStore.getXLoc(), this.hoveredStore.getYLoc() - (this.screenHeight / 75));
		}
	}
	
	/*It's easier to just pass the store since the hovering text will be drawn to
	 *the screen with Graphics (as opposed to it being some JComponent that we would
	 *set the text of)*/
	public void setHovered(Boolean b, Store s) {
		this.hovered = b;
		if (this.hovered) {
			this.hoveredStore = s;
		}
	}
	
	public void setUserLoc(int x, int y) {
		this.userXLoc = x;
		this.userYLoc = y;
	}
	
	public void setUserHover(boolean b) {
		this.hoverUser = b;
	}
}

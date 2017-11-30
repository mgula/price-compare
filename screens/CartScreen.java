package screens;

import classes.Product;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import java.awt.Font;
import java.util.ArrayList;

public class CartScreen extends NonMenuScreen {
	
	private Button mapButton;
	private Button addButton;
	private Button removeButton;
	
	private Label cartLabel;
	
	private JScrollPane allProductsPane;
	private DefaultListModel<String> productsModel;
	private JList<String> allProductsList;
	
	private JScrollPane cartListPane;
	private DefaultListModel<String> cartModel;
	private JList<String> cartList;
	
	private ButtonGroup modeToggle;
	private JRadioButton selectToggle;
	private JRadioButton closestToggle;
	private JRadioButton cheapestToggle;
	
	private Label toggleLabel;
	
	/*The passed ArrayList of products will be used to populate productsModel*/
	public CartScreen(int w, int h, ArrayList<Product> p) {
		super(w, h);
		
		/*Update title*/
		this.title.setText("Pick your groceries");
		
		/*Calculate map button positioning*/
		int mapButtonWidth = w / this.getScreenRatioX();
		int mapButtonHeight = h / this.getScreenRatioY();
		int mapButtonXLoc = w - mapButtonWidth;
		int mapButtonYLoc = 0;
		
		/*Initialize map button*/
		this.mapButton = new Button("Map",
				mapButtonXLoc,
				mapButtonYLoc,
				mapButtonWidth,
				mapButtonHeight);
		
		/*Calculate list positioning*/
		int listWidth = w / 5;
		int listHeight = h / 4;
		int listBaseXLoc = w / 5;
		int listYLoc = (h / 2) - (listHeight / 2);
		
		int allProductsListXLoc = listBaseXLoc;
		
		int cartListXLoc = 3 * listBaseXLoc;
		
		/*Initialize list of products*/
		this.productsModel = new DefaultListModel<String>();
		for (Product pr : p) {
			this.productsModel.addElement(pr.getName());
		}
		this.allProductsList = new JList<String>(this.productsModel);
		this.allProductsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		this.allProductsPane = new JScrollPane(this.allProductsList);
		this.allProductsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.allProductsPane.setBounds(allProductsListXLoc, listYLoc, listWidth, listHeight);
		
		/*Initialize cart*/
		this.cartModel = new DefaultListModel<String>();
		this.cartList = new JList<String>(this.cartModel);
		this.cartList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		this.cartListPane = new JScrollPane(this.cartList);
		this.cartListPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.cartListPane.setBounds(cartListXLoc, listYLoc, listWidth, listHeight);
		
		/*Calculate add & remove button positioning*/
		int otherButtonWidth = w / 15;
		int otherButtonHeight = h / 17;
		int otherButtonYLoc = listYLoc + listHeight;
		
		int addButtonXLoc = allProductsListXLoc - (otherButtonWidth / 2) + (listWidth / 2);
		int removeButtonXLoc = cartListXLoc - (otherButtonWidth / 2) + (listWidth / 2);
		
		/*Initialize add & remove buttons*/
		this.addButton = new Button("Add",
				addButtonXLoc,
				otherButtonYLoc,
				otherButtonWidth,
				otherButtonHeight);
		
		this.removeButton = new Button("Remove",
				removeButtonXLoc,
				otherButtonYLoc,
				otherButtonWidth,
				otherButtonHeight);
		
		/*Calculate cart label positioning*/
		int cartLabelWidth = w / 10;
		int cartLabelHeight = h / 20;
		int cartLabelXLoc = cartListXLoc + (listWidth / 2) - (cartLabelWidth / 2);
		int cartLabelYLoc = listYLoc - cartLabelHeight;
		
		/*Initialize cart label*/
		this.cartLabel = new Label("Your cart",
				cartLabelXLoc,
				cartLabelYLoc,
				cartLabelWidth,
				cartLabelHeight);
		this.cartLabel.setFont(new Font("GillSansUltraBold", Font.PLAIN, 15));
		
		/*Calculate mode toggle positioning*/
		int toggleWidth = w / 8;
		int toggleHeight = h / 30;
		int toggleXLoc = w / 2 - (toggleWidth / 2);
		
		int selectToggleYLoc = 5 * h / 7;
		int closestToggleYLoc = selectToggleYLoc + toggleHeight;
		int cheapestToggleYLoc = closestToggleYLoc + toggleHeight;
		
		/*Initialize mode toggle button group*/
		this.selectToggle = new JRadioButton("Selected store info");
		this.selectToggle.setBounds(toggleXLoc, 
				selectToggleYLoc, 
				toggleWidth, 
				toggleHeight);
		
		this.closestToggle = new JRadioButton("Closest store info");
		this.closestToggle.setBounds(toggleXLoc, 
				closestToggleYLoc, 
				toggleWidth, 
				toggleHeight);
		
		this.cheapestToggle = new JRadioButton("Cheapest store info");
		this.cheapestToggle.setBounds(toggleXLoc, 
				cheapestToggleYLoc, 
				toggleWidth, 
				toggleHeight);
		
		this.modeToggle = new ButtonGroup();
		this.modeToggle.add(this.selectToggle);
		this.modeToggle.add(this.closestToggle);
		this.modeToggle.add(this.cheapestToggle);
		
		/*Start with selected toggle on*/
		this.selectToggle.setSelected(true);
		
		/*Calculate toggle label positioning*/
		int toggleLabelWidth = w / 10;
		int toggleLabelHeight = h / 25;
		int toggleLabelXLoc = toggleXLoc;
		int toggleLabelYLoc = selectToggleYLoc - toggleLabelHeight;
		
		/*Initialize label*/
		this.toggleLabel = new Label("Show me: ",
				toggleLabelXLoc,
				toggleLabelYLoc,
				toggleLabelWidth,
				toggleLabelHeight);
		this.toggleLabel.setFont(new Font("GillSansUltraBold", Font.PLAIN, 15));
	}
	
	public Button getMapButton() {
		return this.mapButton;
	}
	
	public Button getAddButton() {
		return this.addButton;
	}
	
	public Button getRemoveButton() {
		return this.removeButton;
	}
	
	public Label getCartLabel() {
		return this.cartLabel;
	}
	
	public DefaultListModel<String> getProductModel() {
		return this.productsModel;
	}
	
	public JList<String> getProductList() {
		return this.allProductsList;
	}
	
	public JScrollPane getAllProductPane() {
		return this.allProductsPane;
	}
	
	public DefaultListModel<String> getCartModel() {
		return this.cartModel;
	}
	
	public JList<String> getCartList() {
		return this.cartList;
	}
	
	public JScrollPane getCartPane() {
		return this.cartListPane;
	}
	
	public JRadioButton getSelectToggle() {
		return this.selectToggle;
	}
	
	public JRadioButton getClosestToggle() {
		return this.closestToggle;
	}
	
	public JRadioButton getCheapestToggle() {
		return this.cheapestToggle;
	}
	
	public Label getToggleLabel() {
		return this.toggleLabel;
	}
}

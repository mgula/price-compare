/*Author: Marcus Gula
 * This project is a proof of concept of a mobile app for an entrepreneurship course. 
 * The focus of the course is not on actually making an app, but rather the elements of 
 * building a business from the ground up, the monetizing of potentially profitable ideas,
 * and etc (business-y stuff). I don't care about any of that, though; so I'd rather 
 * spend my time working on the implementation of the app.
 * 
 * That being said, this project is not required/being graded in any way since it's
 * an entrepreneurship course, and not a computer science course. Long story short, I'll
 * be taking a lot of shortcuts since this is just a mock-up, and since the actual mobile 
 * app wouldn't even be developed in Java in the first place (unfortunately I don't know
 * Swift or anything so Java will have to do for now).
 * 
 * The purpose of the app is to compare grocery prices across stores to save the user
 * time and/or money while grocery shopping.
 * 
 * For the scope of the mock-up, there's only a bunch of dummy stores/products to compare.
 * The actual app would need to continuously pull information directly from the stores 
 * in order to provide real-time pricing and availability.
 * 
 * The actual app would also leverage Google Maps API or a similar mapping API to provide 
 * nearby store locations. It would also look a lot less primitive and be optimized for 
 * mobile devices.
 */

package controller;

/*Local classes*/
import classes.*;
import screens.*;
import enums.*;

/*Swing for general user interface (buttons, panels, etc)*/
import javax.swing.JFrame;
import javax.swing.JPanel;

/*AWT for graphics and interaction*/
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/*Utilities*/
import java.util.ArrayList;
import java.math.BigDecimal; //these are slower and uglier than doubles but are far more accurate
import java.text.DecimalFormat;

public class Main implements MouseListener, MouseMotionListener {
	public enum AppState {START, MENU, CART, MAP, EXIT};
	public enum SelectionMode {SELECTED, CLOSEST, CHEAPEST};
	
	private ArrayList<Product> allProducts;
	private ArrayList<Product> groceryCart;
	
	private ArrayList<Store> stores;
	
	private Product selectedProduct = null;
	private Store selectedStore = null;
	
	private Store closestStore = null;
	private Store cheapestStore = null;
	
	private Store hoveredStore = null;
	
	private String cartTotal;
	private String selectedProductInfo = "Cart is empty";
	private String missingItemsString;
	
	private int userXLoc;
	private int userYLoc;
	
	private boolean userDragged = false;
	private boolean userHovered = false;
	
	private int pointDimensions = 6; // in pixels
	private final int pixelsToFeetConstant = 11;
	private final int bigNumber = 1000000000;
	
	private int screenHeight;
	private int screenWidth;
	
	private int mapXLoc;
	private int mapYLoc;
	private int mapWidth;
	private int mapHeight;
	
	private JFrame frame;
	private StartScreen startScreen;
	private MenuScreen menuScreen;
	private CartScreen cartScreen;
	private MapScreen mapScreen;
	
	private boolean running = true;
	
	DecimalFormat df;
	
	private AppState currState = AppState.START;
	private AppState nextState = AppState.START;
	
	private SelectionMode mode = SelectionMode.SELECTED;
	
	private static int sleepTime = 16; // 60 fps!! /s
	
	public static void main(String[] args) {
		Main m = new Main();
		while (m.isRunning()) {
			m.tick();
			try {
    				Thread.sleep(sleepTime); // sleep time is in milliseconds
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
		}
		m.cleanup();
	}
	
	public Main() {
		/*Get screen info*/
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.screenWidth = (int)d.getWidth();
		this.screenHeight = (int)d.getHeight();
		
		/*Set initial user location*/
		this.userXLoc = 52 * this.screenWidth / 100;
		this.userYLoc = 29 * this.screenHeight / 100;
		
		/*Initialize variables*/
		this.initProducts();
		this.groceryCart = new ArrayList<Product>();
		this.initStores();
		
		/*Calculate initial distances*/
		this.calculateStoreDistances();
		
		/*Initialize the closest store*/
		this.calculateClosestStore();
		
		/*Initialize decimal formatter*/
		this.df = new DecimalFormat("#.00");
		
		/*Initialize screens*/
		this.startScreen = new StartScreen(this.screenWidth, this.screenHeight);
		this.menuScreen = new MenuScreen(this.screenWidth, this.screenHeight);
		this.cartScreen = new CartScreen(this.screenWidth, this.screenHeight, this.allProducts);
		this.mapScreen = new MapScreen(this.screenWidth, this.screenHeight, this.userXLoc, this.userYLoc, this.stores, this.pointDimensions);
		this.mapScreen.addMouseListener(this); // add listeners for map clicks and events
		this.mapScreen.addMouseMotionListener(this);
		
		/*Update map dimensions.*/
		this.mapXLoc = this.mapScreen.getMapXLoc();
		this.mapYLoc = this.mapScreen.getMapYLoc();
		this.mapWidth = this.mapScreen.getMapWidth();
		this.mapHeight = this.mapScreen.getMapHeight();
		
		/*Start with start screen in the frame*/
		this.frame = new JFrame();
		this.frame.setBackground(Color.LIGHT_GRAY);
		this.frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.frame.setUndecorated(true);
		this.frame.add(this.startScreen.getStartButton());
		this.addViewToFrame(this.startScreen);
		
		/*Set button listeners*/
		this.setActionListeners();
	}
	
	/*Appears only in main - return the running boolean*/
	public boolean isRunning() {
		return this.running;
	}
	
	/*Appears only in the main while loop - a cycle consists of updating the 
	 *state of the application, updating label text, and repainting the frame*/
	void tick() {
		this.updateAppState();
		this.updateLabels();
		this.frame.repaint();
	}
	
	/*Appears only in main - dispose of the frame when we're finished*/
	public void cleanup() {
		this.frame.dispose();
	}
	
	/*Initialize the ArrayList of all products to hold some random items. Prices are from
	 *walmart and target*/
	public void initProducts() {
		/*Initialize list of products.*/
		this.allProducts = new ArrayList<Product>();
		this.allProducts.add(new Product("Apples (Red)", ProductType.FRUIT, false, BigDecimal.valueOf(.80))); // populate list with some random items
		this.allProducts.add(new Product("Apples (Green)", ProductType.FRUIT, false, BigDecimal.valueOf(.81)));
		this.allProducts.add(new Product("Bananas", ProductType.FRUIT, false, BigDecimal.valueOf(.57)));
		this.allProducts.add(new Product("Bread", ProductType.WHEAT, false, BigDecimal.valueOf(2.30)));
		this.allProducts.add(new Product("Grapes (Green)", ProductType.FRUIT, false, BigDecimal.valueOf(1.57)));
		this.allProducts.add(new Product("Chips", ProductType.SNACK, false, BigDecimal.valueOf(3.00)));
		this.allProducts.add(new Product("Crisps", ProductType.SNACK, false, BigDecimal.valueOf(2.90)));
		this.allProducts.add(new Product("Chisps", ProductType.SNACK, false, BigDecimal.valueOf(3.25)));
		this.allProducts.add(new Product("Beef (1 lb)", ProductType.MEAT, false, BigDecimal.valueOf(3.86)));
		this.allProducts.add(new Product("Chicken (1 lb)", ProductType.MEAT, false, BigDecimal.valueOf(3.24)));
		this.allProducts.add(new Product("Laundry Detergent", ProductType.UTIL, true, BigDecimal.valueOf(10.42)));
		this.allProducts.add(new Product("Paper Towels", ProductType.UTIL, false, BigDecimal.valueOf(6.02)));
		this.allProducts.add(new Product("Batteries (AA)", ProductType.UTIL, true, BigDecimal.valueOf(14.99)));
		this.allProducts.add(new Product("Batteries (AAA)", ProductType.UTIL, true, BigDecimal.valueOf(12.99)));
		this.allProducts.add(new Product("Cereal", ProductType.WHEAT, false, BigDecimal.valueOf(2.99)));
		this.allProducts.add(new Product("Milk (Skim)", ProductType.DAIRY, false, BigDecimal.valueOf(2.99)));
		this.allProducts.add(new Product("Milk (.5%)", ProductType.DAIRY, false, BigDecimal.valueOf(3.55)));
		this.allProducts.add(new Product("Milk (1%)", ProductType.DAIRY, false, BigDecimal.valueOf(3.98)));
		this.allProducts.add(new Product("Milk (2%)", ProductType.DAIRY, false, BigDecimal.valueOf(3.98)));
		this.allProducts.add(new Product("Cheese", ProductType.DAIRY, false, BigDecimal.valueOf(4.19)));
		this.allProducts.add(new Product("Water (2L)", ProductType.BEVERAGE, false, BigDecimal.valueOf(1.20)));
		this.allProducts.add(new Product("Coca Cola (2L)", ProductType.BEVERAGE, true, BigDecimal.valueOf(1.79)));
		this.allProducts.add(new Product("Pepsi (2L)", ProductType.BEVERAGE, true, BigDecimal.valueOf(1.79)));
		this.allProducts.add(new Product("Sparkling Water", ProductType.BEVERAGE, false, BigDecimal.valueOf(1.68)));
		this.allProducts.add(new Product("Ice Cream", ProductType.DAIRY, false, BigDecimal.valueOf(5.25)));
	}
	
	/*Initialize the ArrayList of all stores to hold some random stores with random inventories*/
	public void initStores() {
		/*Initialize stores.*/
		this.stores = new ArrayList<Store>();
		
		/*Set some random inventories*/
		ArrayList<Product> storeAInv = new ArrayList<Product>();
		storeAInv.addAll(this.allProducts);
		storeAInv.remove(2);
		storeAInv.remove(4);
		storeAInv.remove(5);
		
		ArrayList<Product> storeBInv = new ArrayList<Product>();
		storeBInv.addAll(this.allProducts);
		storeBInv.remove(7);
		storeBInv.remove(1);
		storeBInv.remove(9);
		
		ArrayList<Product> storeCInv = new ArrayList<Product>();
		storeCInv.addAll(this.allProducts);
		storeCInv.remove(11);
		storeCInv.remove(12);
		storeCInv.remove(1);
		
		this.stores.add(new Store("Store A", storeAInv, 40 * this.screenWidth / 100, 32 * this.screenHeight / 100)); // populate list with some random stores
		this.stores.add(new Store("Store B", storeBInv, 49 * this.screenWidth / 100, 29 * this.screenHeight / 100));
		this.stores.add(new Store("Store C", storeCInv, 55 * this.screenWidth / 100, 25 * this.screenHeight / 100));
		this.stores.add(new Store("Store D", this.allProducts, 63 * this.screenWidth / 100, 32 * this.screenHeight / 100));
	}
	
	/*Set action/focus listeners for every element in the application*/
	public void setActionListeners() {
		/*Start screen button*/
		this.startScreen.getStartButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextState = AppState.MENU;
			}
    	});
		/*Menu screen buttons*/
		this.menuScreen.getEditCartButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextState = AppState.CART;
			}
    	});
		this.menuScreen.getMapButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextState = AppState.MAP;
			}
    	});
		this.menuScreen.getExitButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextState = AppState.EXIT;
			}
    	});
		/*Cart screen elements*/
		this.cartScreen.getBackButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextState = AppState.MENU;
			}
    	});
		this.cartScreen.getMapButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextState = AppState.MAP;
			}
    	});
		this.cartScreen.getAddButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = cartScreen.getProductList().getSelectedIndex();
				if (index == -1) { // make sure item is selected
					return;
				}
				String productString = cartScreen.getProductModel().getElementAt(index);
				for (Product p : allProducts) {
					if (p.getName() == productString) {
						groceryCart.add(p);
						cartScreen.getCartModel().addElement(productString);
						mapScreen.getCartModel().addElement(productString);
						break;
					}
				}
				
				updateStrings();
			}
    	});
		this.cartScreen.getRemoveButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!groceryCart.isEmpty()) {
					int index = cartScreen.getCartList().getSelectedIndex();
					if (index == -1) { // make sure item is selected
						return;
					}
					String productString = cartScreen.getCartModel().getElementAt(index);
					cartScreen.getCartModel().remove(index);
					mapScreen.getCartModel().remove(index);
					Product match = null;
					for (Product p : groceryCart) {
						if (p.getName() == productString) {
							match = p;
							break;
						}
					}
					groceryCart.remove(match); // remove after to avoid concurrent modification exception
					
					if (selectedProduct != null) {
						if (match.getName() == selectedProduct.getName()) {
							selectedProduct = null; // set to null so the string can properly update
						}
					}
					
					updateStrings();
				}
			}
    	});
		this.cartScreen.getProductList().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				cartScreen.getCartList().clearSelection();
			}
			@Override
			public void focusLost(FocusEvent e) {}
		});
		this.cartScreen.getCartList().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				cartScreen.getProductList().clearSelection();
			}
			@Override
			public void focusLost(FocusEvent e) {}
		});
		this.cartScreen.getSelectToggle().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mode = SelectionMode.SELECTED;
				
				mapScreen.getSelectedStoreTitleField().setText("Selected Store");
				selectedStore = null;
				updateStrings();
			}
    	});
		this.cartScreen.getClosestToggle().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mode = SelectionMode.CLOSEST;
				
				mapScreen.getSelectedStoreTitleField().setText("Closest Store");
				updateStrings();
				
				mapScreen.setSelectedStoreBool(false);
			}
    	});
		this.cartScreen.getCheapestToggle().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mode = SelectionMode.CHEAPEST;
				
				mapScreen.getSelectedStoreTitleField().setText("Cheapest Store");
				updateStrings();
				
				mapScreen.setSelectedStoreBool(false);
			}
    	});
		/*Map screen elements*/
		this.mapScreen.getBackButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextState = AppState.MENU;
			}
    	});
		this.mapScreen.getCartButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextState = AppState.CART;
			}
    	});
		this.mapScreen.getCartList().addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int index = mapScreen.getCartList().getSelectedIndex();
				if (index == -1) {
					selectedProductInfo = "Select a product";
					return;
				}
				String productString = mapScreen.getCartModel().getElementAt(index);
				Product match = null;
				for (Product p : groceryCart) {
					if (p.getName() == productString) {
						match = p;
						break;
					}
				}
				selectedProduct = match;
				
				updateSelectedProductInfoString();
			}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
		});
	}
	
	/*Update the state of the application, if applicable - when the state changes,
	 *remove old components from the frame and add the correct ones*/
	public void updateAppState() {
		if (this.currState != this.nextState) {
			this.frame.getContentPane().removeAll();
			switch (this.nextState) {
				case MENU:
					this.frame.add(this.menuScreen.getEditCartButton());
					this.frame.add(this.menuScreen.getMapButton());
					this.frame.add(this.menuScreen.getExitButton());
					this.addViewToFrame(this.menuScreen);
					break;
				case CART:
					this.frame.add(this.cartScreen.getTitle());
					this.frame.add(this.cartScreen.getBackButton());
					this.frame.add(this.cartScreen.getMapButton());
					this.frame.add(this.cartScreen.getAddButton());
					this.frame.add(this.cartScreen.getRemoveButton());
					this.frame.add(this.cartScreen.getCartLabel());
					this.frame.add(this.cartScreen.getAllProductPane());
					this.frame.add(this.cartScreen.getCartPane());
					this.frame.add(this.cartScreen.getSelectToggle());
					this.frame.add(this.cartScreen.getClosestToggle());
					this.frame.add(this.cartScreen.getCheapestToggle());
					this.frame.add(this.cartScreen.getToggleLabel());
					this.addViewToFrame(this.cartScreen);
					break;
				case MAP:
					this.frame.add(this.mapScreen.getTitle());
					this.frame.add(this.mapScreen.getBackButton());
					this.frame.add(this.mapScreen.getCartButton());
					this.frame.add(this.mapScreen.getClosestStoreTitleField());
					this.frame.add(this.mapScreen.getClosestStoreInfoField());
					this.frame.add(this.mapScreen.getClosestStoreDistanceField());
					this.frame.add(this.mapScreen.getSelectedStoreTitleField());
					this.frame.add(this.mapScreen.getSelectedStoreNameField());
					this.frame.add(this.mapScreen.getSelectedStoreDistanceField());
					this.frame.add(this.mapScreen.getSelectedStoreCartTotalField());
					this.frame.add(this.mapScreen.getSelectedStoreMissingItemsField());
					this.frame.add(this.mapScreen.getCartLabel());
					this.frame.add(this.mapScreen.getItemInfoLabel());
					this.frame.add(this.mapScreen.getCartListPane());
					this.addViewToFrame(this.mapScreen);
					break;
				case EXIT:
					this.running = false;
					break;
				default:
					break;
			}
			this.currState = this.nextState;
		}
	}
	
	/*Warning - this method is inefficient! setText() doesn't need to be called
	 *on every label every single cycle. Ideally, setText() would be called only when
	 *necessary, like once after a click or once for each update during a drag event. I'm
	 *setting the text every cycle only because there's a noticeable flashing that occurs
	 *when setText() is called once. This is either a swing issue or an issue with my old,
	 *slow computer.*/
	public void updateLabels() {
		switch (this.currState) {
			case MAP:
				/*Update selected store labels*/
				if (this.selectedStore != null) {
					this.mapScreen.getSelectedStoreNameField().setText("Name: " + this.selectedStore.getName());
					this.mapScreen.getSelectedStoreDistanceField().setText("Distance: " + this.selectedStore.getDistanceTo() + " ft");
					this.mapScreen.getSelectedStoreCartTotalField().setText("Cart Total: " + this.cartTotal);
					this.mapScreen.getSelectedStoreMissingItemsField().setText("Missing Items: " + this.missingItemsString);
				} else {
					this.mapScreen.getSelectedStoreNameField().setText("Name: -");
					this.mapScreen.getSelectedStoreDistanceField().setText("Distance: -");
					this.mapScreen.getSelectedStoreCartTotalField().setText("Cart Total: -");
					this.mapScreen.getSelectedStoreMissingItemsField().setText("Missing Items: -");
				}
				
				/*Update closest store information and distance labels*/
				this.mapScreen.getClosestStoreInfoField().setText(this.closestStore.getName());
				this.mapScreen.getClosestStoreDistanceField().setText(this.closestStore.getDistanceTo() + " ft");
				
				/*Update selected item info label*/
				this.mapScreen.getItemInfoLabel().setText(this.selectedProductInfo);
				break;
			default:
				break;
		}
	}
	
	/*updateLabels() calls setText() on the map screen components; this method ensures
	 *the strings that updateLabels() is using in the setText() calls are correct - namely,
	 *anything that's not immediately available (selected product info, cart total, and missing
	 *items - these must be calculated)*/
	public void updateStrings() {
		switch (this.mode) {
			case CLOSEST:
				this.calculateClosestStore();
				this.selectedStore = this.closestStore;
				break;
			case CHEAPEST:
				this.calculateCheapestStore();
				this.selectedStore = this.cheapestStore;
				break;
			default:
				break;
		}
		this.updateSelectedProductInfoString();
		this.updateCartTotalString();
		this.updateMissingItemsString();
	}
	
	/*Update the selected product info string*/
	public void updateSelectedProductInfoString() {
		if (this.groceryCart.isEmpty()) {
			this.selectedProductInfo = "Cart is empty";
		} else if (this.selectedProduct == null) {
			this.selectedProductInfo = "Select a product";
		} else if (this.selectedStore == null) {
			this.selectedProductInfo = "Select a store";
		} else if (!this.selectedStore.getInventory().contains(this.selectedProduct)) {
			this.selectedProductInfo = "Item not available at " + this.selectedStore.getName() + ".";
		} else {
			BigDecimal price = this.selectedProduct.getBasePrice().multiply(BigDecimal.valueOf(this.selectedStore.getPriceModifier(this.selectedProduct)));
			this.selectedProductInfo = "Price of " + this.selectedProduct.getName() + " at " + this.selectedStore.getName() + ": $" + this.df.format(price.doubleValue());
		}
	}
	
	/*Calculate the cart total and update the cart total string*/
	public void updateCartTotalString() {
		if (this.groceryCart.isEmpty()) {
			this.cartTotal = "Cart is empty";
			return;
		}
		if (this.selectedStore == null) {
			return;
		}
		
		BigDecimal total = BigDecimal.valueOf(0.00);
		for (Product p : this.groceryCart) {
			if (this.selectedStore.getInventory().contains(p)) {
				BigDecimal result = p.getBasePrice().multiply(BigDecimal.valueOf(this.selectedStore.getPriceModifier(p)));
				total = total.add(result);
			}
		}
		this.cartTotal = "$" + this.df.format(total.doubleValue());
	}
	
	/*Find which items from the cart are missing and update the missing
	 *items string*/
	public void updateMissingItemsString() {
		if (this.groceryCart.isEmpty()) {
			this.missingItemsString = "Cart is empty";
			return;
		}
		if (this.selectedStore == null) {
			return;
		}
		
		ArrayList<Product> missing = new ArrayList<Product>();
		for (Product p : this.groceryCart) {
			boolean match = false;
			for (Product q : this.selectedStore.getInventory()) {
				if (q.getName() == p.getName()) {
					match = true;
				}
			}
			if (!match && !missing.contains(p)) {
				missing.add(p);
			}
		}
		if (missing.isEmpty()) {
			this.missingItemsString = "None";
		} else {
			this.missingItemsString = "";
			for (Product p : missing) {
				this.missingItemsString += p.getName();
				if (missing.indexOf(p) < missing.size() - 1) {
					this.missingItemsString += ", ";
				}
			}
		}
	}
	
	/*Get distance between 2 points (in the context of this project, this "distance" 
	 *will be in pixels)*/
	public int getDistance(int x1, int x2, int y1, int y2) {
		double x = Math.pow(x2 - x1, 2.0);
		double y = Math.pow(y2 - y1, 2.0);
		return (int)Math.sqrt(x + y);
	}
	
	/*Calculate and update the distance between the user and store for all stores*/
	public void calculateStoreDistances() {
		for (Store s : this.stores) {
			int distance = this.getDistance(this.userXLoc, s.getXLoc(), this.userYLoc, s.getYLoc());
			s.setDistanceTo(distance * this.pixelsToFeetConstant); // multiply to make it look like feet
		}
	}
	
	/*Calculate and update the closest store*/
	public void calculateClosestStore() {
		int closest = this.bigNumber; // start with some large number
		for (Store s : this.stores) {
			if (s.getDistanceTo() < closest) {
				closest = s.getDistanceTo();
				this.closestStore = s;
			}
		}
	}
	
	/*Calculate and update the cheapest store*/
	public void calculateCheapestStore() {
		if (this.groceryCart.isEmpty()) {
			this.cheapestStore = null;
			return;
		}
		
		BigDecimal cheapest = BigDecimal.valueOf(this.bigNumber); // start with some large number
		for (Store s : this.stores) {
			BigDecimal total = BigDecimal.valueOf(0.00);
			for (Product p : this.groceryCart) {
				if (s.getInventory().contains(p)) {
					BigDecimal result = p.getBasePrice().multiply(BigDecimal.valueOf(s.getPriceModifier(p)));
					total = total.add(result);
				}
			}
			
			if (total.compareTo(cheapest) == -1 && total.compareTo(BigDecimal.valueOf(0.00)) != 0) {
				cheapest = total;
				this.cheapestStore = s;
			}
		}
	}
	
	/*Add the given JPanel to the frame*/
	public void addViewToFrame(JPanel screen) {
		this.frame.getContentPane().add(screen);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setVisible(true);
	}	
	
	/*Check store clicks*/
	public void manageClick(int clickX, int clickY) {
		if (this.mode == SelectionMode.SELECTED) {
			Store match = null;
			for (Store s : this.stores) {
				if (this.inRegion(s.getXLoc(), s.getYLoc(), clickX, clickY, 2)) {
					match = s;
				}
			}
			if (match != null) {
				this.selectedStore = match;
				this.updateStrings();
			
				this.mapScreen.setSelectedStore(this.selectedStore);
				this.mapScreen.setSelectedStoreBool(true);
			}
		
			/*Map screen updates*/
			if (this.hoveredStore != null && this.hoveredStore != this.selectedStore) {
				this.mapScreen.setHoveredStore(this.hoveredStore);
				this.mapScreen.setHoveredStoreBool(true);
			} else {
				this.mapScreen.setHoveredStoreBool(false);
			}
		} else {
			// don't do anything in other modes - ignore the click
		}
	}
	
	/*If over user location, allow dragging of the point at that location*/
	public void initUserLocDrag(int clickX, int clickY) {
		if (this.inRegion(this.userXLoc, this.userYLoc, clickX, clickY, 3)) {
			this.userDragged = true;
		}
	}
	
	/*Check if hovering over user/stores - if in selected store mode, don't
	 *display info on hover if the hovered store is also the selected store. This
	 *doesn't apply in the other two modes*/
	public void manageMove(int moveX, int moveY) {
		/*Check for store hovers*/
		Store match = null;
		for (Store s : this.stores) {
			if (this.inRegion(s.getXLoc(), s.getYLoc(), moveX, moveY, 2)) {
				match = s;
			}
		}
		
		if (match != null) {
			if (this.mode == SelectionMode.SELECTED) {
				if (match != this.selectedStore) {
					this.hoveredStore = match;
				} else {
					this.hoveredStore = null;
				}
			} else {
				this.hoveredStore = match;
			}
		} else {
			this.hoveredStore = null;
		}
		
		/*Check user loc hovers*/
		if (this.inRegion(this.userXLoc, this.userYLoc, moveX, moveY, 2)) {
			this.userHovered = true;
		} else {
			this.userHovered = false;
		}
		
		/*Map screen updates*/
		this.mapScreen.setUserHovered(this.userHovered);
		if (this.hoveredStore != null) {
			if (this.mode == SelectionMode.SELECTED) {
				if (this.hoveredStore != this.selectedStore) {
					this.mapScreen.setHoveredStore(this.hoveredStore);
					this.mapScreen.setHoveredStoreBool(true);
				} else {
					this.mapScreen.setHoveredStoreBool(false);
				}
			} else {
				this.mapScreen.setHoveredStore(this.hoveredStore);
				this.mapScreen.setHoveredStoreBool(true);
			}
		} else {
			this.mapScreen.setHoveredStoreBool(false);
		}
	}
	
	/*Check user location drags - keep the user location inside the map*/
	public void manageDrag(int dragX, int dragY) {
		if (this.userDragged) {
			int offsetMapX = this.mapXLoc - (this.mapWidth / 2);
			if (dragX > offsetMapX && dragX < offsetMapX + this.mapWidth) {
				this.userXLoc = dragX;
			}
			int offsetMapY = this.mapYLoc - (this.mapHeight / 2);
			if (dragY > offsetMapY && dragY < offsetMapY + this.mapHeight) {
				this.userYLoc = dragY;
			}
			
			this.calculateStoreDistances();
			this.calculateClosestStore();
			
			/*Map screen updates*/
			this.mapScreen.setUserLoc(this.userXLoc, this.userYLoc);
			
			/*If in closest mode, update strings on every drag event*/
			if (this.mode == SelectionMode.CLOSEST) {
				this.updateStrings();
			}
		}
	}
	
	/*Compare mouse event x, y to some other x, y*/
	public boolean inRegion(int xBound, int yBound, int eventX, int eventY, int dimMultiplier) {
		if (eventX <= xBound + (this.pointDimensions * dimMultiplier) && eventX >= xBound - (this.pointDimensions * dimMultiplier)) {
			if (eventY <= yBound + (this.pointDimensions * dimMultiplier) && eventY >= yBound - (this.pointDimensions * dimMultiplier)) {
				return true;
			}
		}
		return false;
	}
	

	/*Mouse listener methods - these don't need to check the app state because
	 *only the map screen has a mouse listener/motion listener associated with it*/
	@Override
	public void mouseClicked(MouseEvent e) {
		this.manageClick(e.getX(), e.getY());
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		this.initUserLocDrag(e.getX(), e.getY());
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		this.userDragged = false;
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {} // won't use
	@Override
	public void mouseExited(MouseEvent e) {} // won't use
	
	/*Mouse motion listener methods*/
	@Override
	public void mouseMoved(MouseEvent e) {
		this.manageMove(e.getX(), e.getY());
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		this.manageMove(e.getX(), e.getY());
		this.manageDrag(e.getX(), e.getY());
	}
}

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


/*TODO:
 * -fix screen ratios so it looks passable on every screen
 * -make product prices slightly more realistic
 * -add indicator for selected store
 * -make launcher script for easy compiling on other computers
 * -missing items text should wrap to prevent overflow into cart area
 */
public class Main implements MouseListener, MouseMotionListener {
	private ArrayList<Product> allProducts;
	private ArrayList<Product> groceryCart;
	
	private String cartTotal;
	
	private Product selectedProduct = null;
	private String selectedProductInfo = "Cart is empty";
	
	private int userXLoc;
	private int userYLoc;
	
	private boolean userDrag = false;
	private boolean userHover = false;
	
	private ArrayList<Store> stores;
	
	private Store selectedStore = null;
	private String missingItemsString;
	
	private Store closestStore = null;
	private Store cheapestStore = null;
	private Store hoveredStore = null;
	
	private int pointDimensions = 6; // in pixels
	private final int pixelsToFeetConstant = 11;
	
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
	
	private static int sleepTime = 16; // 60 fps!! /s
	
	public static void main(String[] args) {
		Main m = new Main();
		m.init();
		while (m.running) {
			m.tick();
			try {
    			Thread.sleep(sleepTime); // sleep time is in milliseconds
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
		}
		m.frame.dispose();
	}
	
	void init() {
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
	
	public void initProducts() {
		/*Initialize list of products.*/
		this.allProducts = new ArrayList<Product>();
		this.allProducts.add(new Product("Apples (Red)", ProductType.FRUIT, false, BigDecimal.valueOf(1.05))); // populate list with some random items
		this.allProducts.add(new Product("Apples (Green)", ProductType.FRUIT, false, BigDecimal.valueOf(1.05)));
		this.allProducts.add(new Product("Bananas", ProductType.FRUIT, false, BigDecimal.valueOf(1.00)));
		this.allProducts.add(new Product("Bread", ProductType.WHEAT, false, BigDecimal.valueOf(2.30)));
		this.allProducts.add(new Product("Grapes (Green)", ProductType.FRUIT, false, BigDecimal.valueOf(.95)));
		this.allProducts.add(new Product("Chips", ProductType.SNACK, false, BigDecimal.valueOf(1.25)));
		this.allProducts.add(new Product("Crisps", ProductType.SNACK, false, BigDecimal.valueOf(1.25)));
		this.allProducts.add(new Product("Chisps", ProductType.SNACK, false, BigDecimal.valueOf(1.25)));
		this.allProducts.add(new Product("Beef (1 lb)", ProductType.MEAT, false, BigDecimal.valueOf(6.00)));
		this.allProducts.add(new Product("Chicken (1 lb)", ProductType.MEAT, false, BigDecimal.valueOf(5.00)));
		this.allProducts.add(new Product("Laundry Detergent", ProductType.UTIL, true, BigDecimal.valueOf(3.00)));
		this.allProducts.add(new Product("Paper Towels", ProductType.UTIL, false, BigDecimal.valueOf(2.00)));
		this.allProducts.add(new Product("Batteries (AA)", ProductType.UTIL, true, BigDecimal.valueOf(4.00)));
		this.allProducts.add(new Product("Batteries (AAA)", ProductType.UTIL, true, BigDecimal.valueOf(2.00)));
		this.allProducts.add(new Product("Cereal", ProductType.WHEAT, false, BigDecimal.valueOf(3.00)));
		this.allProducts.add(new Product("Milk (Skim)", ProductType.DAIRY, false, BigDecimal.valueOf(2.00)));
		this.allProducts.add(new Product("Milk (.5%)", ProductType.DAIRY, false, BigDecimal.valueOf(2.10)));
		this.allProducts.add(new Product("Milk (1%)", ProductType.DAIRY, false, BigDecimal.valueOf(2.20)));
		this.allProducts.add(new Product("Milk (2%)", ProductType.DAIRY, false, BigDecimal.valueOf(2.30)));
		this.allProducts.add(new Product("Cheese", ProductType.DAIRY, false, BigDecimal.valueOf(3.00)));
		this.allProducts.add(new Product("Water (2 L)", ProductType.BEVERAGE, false, BigDecimal.valueOf(2.00)));
		this.allProducts.add(new Product("Coca Cola (2 L)", ProductType.BEVERAGE, true, BigDecimal.valueOf(2.00)));
		this.allProducts.add(new Product("Pepsi (2 L)", ProductType.BEVERAGE, true, BigDecimal.valueOf(2.00)));
		this.allProducts.add(new Product("Sparkling Water", ProductType.BEVERAGE, false, BigDecimal.valueOf(2.00)));
		this.allProducts.add(new Product("Ice Cream", ProductType.DAIRY, false, BigDecimal.valueOf(4.00)));
	}
	
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
		/*Cart screen buttons*/
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
					}
				}
				
				/*Update relevant strings*/
				updateCartTotalString();
				updateMissingItemsString();
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
					
					if (match.getName() == selectedProduct.getName()) {
						selectedProduct = null; // set to null so the string can properly update
					}
					
					/*Update relevant strings*/
					updateCartTotalString();
					updateMissingItemsString();
					updateSelectedProductInfoString();
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
		/*Map screen buttons*/
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
				
				/*Update relevant strings*/
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
	
	void tick() {
		this.updateAppState();
		this.updateLabels();
		this.frame.repaint();
	}
	
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
			case CART:
				
				break;
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
	
	public void updateSelectedProductInfoString() {
		if (this.groceryCart.isEmpty()) {
			this.selectedProductInfo = "Cart is empty";
		} else if (this.selectedProduct == null) {
			this.selectedProductInfo = "Select a product";
		} else if (this.selectedStore == null) {
			this.selectedProductInfo = "Select a store";
		} else if (!this.selectedStore.getInventory().contains(this.selectedProduct)) {
			this.selectedProductInfo = "Item not avaiable at " + this.selectedStore.getName() + ".";
		} else {
			BigDecimal price = this.selectedProduct.getBasePrice().multiply(BigDecimal.valueOf(this.selectedStore.getPriceModifier(this.selectedProduct)));
			this.selectedProductInfo = "Price of " + this.selectedProduct.getName() + " at " + this.selectedStore.getName() + ": $" + this.df.format(price.doubleValue());
		}
	}
	
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
	
	public void calculateStoreDistances() {
		for (Store s : this.stores) {
			int distance = this.getDistance(this.userXLoc, s.getXLoc(), this.userYLoc, s.getYLoc());
			s.setDistanceTo(distance * this.pixelsToFeetConstant); // multiply to make it look like feet
		}
	}
	
	public void calculateClosestStore() {
		int closest = 1000000000; // start with some large number
		for (Store s : this.stores) {
			if (s.getDistanceTo() < closest) {
				closest = s.getDistanceTo();
				this.closestStore = s;
			}
		}
	}
	
	public void addViewToFrame(JPanel screen) {
		this.frame.getContentPane().add(screen);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setVisible(true);
	}	
	
	/*Check store clicks*/
	public void manageClick(int clickX, int clickY) {
		Store match = null;
		for (Store s : this.stores) {
			if (this.inRegion(s.getXLoc(), s.getYLoc(), clickX, clickY, 2)) {
				match = s;
			}
		}
		if (match != null) {
			this.selectedStore = match;
			
			/*Update the relevant strings*/
			this.updateCartTotalString();
			this.updateMissingItemsString();
			this.updateSelectedProductInfoString();
		}
		
		/*Map screen updates*/
		if (this.hoveredStore != null && this.hoveredStore != this.selectedStore) {
			this.mapScreen.setHovered(true, this.hoveredStore);
		} else {
			this.mapScreen.setHovered(false, null);
		}
	}
	
	/*If over user location, allow dragging of the point at that location*/
	public void initUserLocDrag(int clickX, int clickY) {
		if (this.inRegion(this.userXLoc, this.userYLoc, clickX, clickY, 3)) {
			this.userDrag = true;
		}
	}
	
	/*Check if hovering over user/stores*/
	public void manageMove(int moveX, int moveY) {
		/*Check for store hovers*/
		Store match = null;
		for (Store s : this.stores) {
			if (this.inRegion(s.getXLoc(), s.getYLoc(), moveX, moveY, 2)) {
				match = s;
			}
		}
		if (match != null && match != this.selectedStore) {
			this.hoveredStore = match;
		} else {
			this.hoveredStore = null;
		}
		
		/*Check user loc hovers*/
		if (this.inRegion(this.userXLoc, this.userYLoc, moveX, moveY, 2)) {
			this.userHover = true;
		} else {
			this.userHover = false;
		}
		
		/*Map screen updates*/
		this.mapScreen.setUserHover(this.userHover);
		if (this.hoveredStore != null && this.hoveredStore != this.selectedStore) {
			this.mapScreen.setHovered(true, this.hoveredStore);
		} else {
			this.mapScreen.setHovered(false, null);
		}
	}
	
	/*Check user location drags - keep the user location inside the map*/
	public void manageDrag(int dragX, int dragY) {
		if (this.userDrag) {
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
		this.userDrag = false;
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

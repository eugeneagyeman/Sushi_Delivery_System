package comp1206.sushi.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User extends Model implements Serializable {
	public static final long serialVersionUID = 2348722584363340903L;

	private String name;
	private String password;
	private String address;
	private Postcode postcode;
	private Map<Dish,Number> basket;
	private final ArrayList<Order> orders;


	public User(String username, String password, String address, Postcode postcode) {
		this.name = username;
		this.password = password;
		this.address = address;
		this.postcode = postcode;
		this.basket = new HashMap<>();
		orders = new ArrayList<>();

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getDistance() {
		return postcode.getDistance();
	}

	public Postcode getPostcode() {
		return this.postcode;
	}
	
	public void setPostcode(Postcode postcode) {
		this.postcode = postcode;
	}

	public Map<Dish, Number> getBasket() {
		return basket;
	}

	public void setBasket(Map<Dish, Number> basket) {
		this.basket = basket;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public ArrayList<Order> getOrders() {
		return orders;
	}
}

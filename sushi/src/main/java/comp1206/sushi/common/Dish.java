package comp1206.sushi.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class Dish extends Model implements Serializable {
	public static final long serialVersionUID = -8934360472092307508L;
	private String name;
	private String description;
	private Number price;
	private volatile Map <Ingredient,Number> recipe;
	private Number restockThreshold;
	private Number restockAmount;



	public Dish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.restockThreshold = restockThreshold;
		this.restockAmount = restockAmount;
		this.recipe = new HashMap<Ingredient,Number>();
	}

	public synchronized String getName() {
		return name;
	}

	public synchronized void setName(String name) {
		this.name = name;
	}

	public synchronized String getDescription() {
		return description;
	}

	public synchronized void setDescription(String description) {
		this.description = description;
	}

	public synchronized Number getPrice() {
		return price;
	}

	public synchronized void setPrice(Number price) {
		this.price = price;
	}

	public synchronized Map <Ingredient,Number> getRecipe() {
		return recipe;
	}

	public synchronized void setRecipe(Map <Ingredient,Number> recipe) {
		this.recipe = recipe;
	}

	public synchronized void setRestockThreshold(Number restockThreshold) {
		this.restockThreshold = restockThreshold;
	}
	
	public synchronized void setRestockAmount(Number restockAmount) {
		this.restockAmount = restockAmount;
	}

	public synchronized Number getRestockThreshold() {
		return restockThreshold;
	}

	public synchronized Number getRestockAmount() {
		return this.restockAmount;
	}




}

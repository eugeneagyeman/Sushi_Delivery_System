package comp1206.sushi.common;

import comp1206.sushi.common.Supplier;

public class Ingredient extends Model {

	private String name;
	private String unit;
	private Supplier supplier;
	private Number restockThreshold;
	private Number restockAmount;
	private Number weight;

	public Ingredient(String name, String unit, Supplier supplier, Number restockThreshold,
			Number restockAmount, Number weight) {
		this.setName(name);
		this.setUnit(unit);
		this.setSupplier(supplier);
		this.setRestockThreshold(restockThreshold);
		this.setRestockAmount(restockAmount);
		this.setWeight(weight);
	}

	public synchronized String getName() {
		return name;
	}

	public synchronized void setName(String name) {
		this.name = name;
	}

	public synchronized String getUnit() {
		return unit;
	}

	public synchronized void setUnit(String unit) {
		this.unit = unit;
	}

	public synchronized Supplier getSupplier() {
		return supplier;
	}

	public synchronized void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	public synchronized Number getRestockThreshold() {
		return restockThreshold;
	}

	public synchronized void setRestockThreshold(Number restockThreshold) {
		this.restockThreshold = restockThreshold;
	}

	public synchronized Number getRestockAmount() {
		return restockAmount;
	}

	public synchronized void setRestockAmount(Number restockAmount) {
		this.restockAmount = restockAmount;
	}

	public synchronized Number getWeight() {
		return weight;
	}

	public synchronized void setWeight(Number weight) {
		this.weight = weight;
	}
}

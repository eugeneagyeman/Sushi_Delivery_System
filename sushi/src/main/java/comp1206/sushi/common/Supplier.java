package comp1206.sushi.common;

import java.io.Serializable;

public class Supplier extends Model implements Serializable {
	public static final long serialVersionUID = 1424375336027477290L;
	private String name;
	private Postcode postcode;
	private Number distance;

	public Supplier(String name, Postcode postcode) {
		this.name = name;
		this.postcode = postcode;
	}

	public Supplier() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Postcode getPostcode() {
		return this.postcode;
	}
	
	public void setPostcode(Postcode postcode) {
		this.postcode = postcode;
	}

	public Number getDistance() {
		return postcode.getDistance();
	}

	@Override
	public String toString() {
		return String.format("SUPPLIER:%s:%s:%s", name, postcode, distance);
	}
}

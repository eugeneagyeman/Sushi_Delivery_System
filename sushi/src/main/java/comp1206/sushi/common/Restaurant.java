package comp1206.sushi.common;

import java.io.Serializable;

public class Restaurant implements Serializable {
	public static final long serialVersionUID = -5962361060877696289L;
	private String name;
	private Postcode location;

	public Restaurant(String name, Postcode location) {
		this.name = name;
		this.location = location;
	}

	public Restaurant() {

	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Postcode getLocation() {
		return location;
	}

	public void setLocation(Postcode location) {
		this.location = location;
	}


}

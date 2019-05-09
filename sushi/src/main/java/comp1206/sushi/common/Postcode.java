package comp1206.sushi.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import comp1206.sushi.common.Postcode;

public class Postcode extends Model implements Serializable {
	public static final long serialVersionUID = -4456027988244365526L;
	private String name;
	private Map<String,Double> latLong;
	private Number distance;

	public Postcode(String code) {
		this.name = code;
		calculateLatLong();
		this.distance = Integer.valueOf(0);
	}
	
	public Postcode(String code, Restaurant restaurant) {
		this.name = code;
		calculateLatLong();
		calculateDistance(restaurant);
	}

	public Postcode() {

	}
	
	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Number getDistance() {
		return this.distance;
	}

	public Map<String,Double> getLatLong() {
		return this.latLong;
	}
	
	private void calculateDistance(Restaurant restaurant) {
		final double radius = 6372.8;
		Postcode restaurantLocation = restaurant.getLocation();
		Double restaurantLat = restaurantLocation.getLatLong().get("lat");
		Double restaurantLong = restaurantLocation.getLatLong().get("lon");

		Double postcodeLat = this.getLatLong().get("lat");
		Double postcodeLong = this.getLatLong().get("lon");
		double dlat = Math.toRadians(postcodeLat-restaurantLat);
		double dlon = Math.toRadians(postcodeLong-restaurantLong);

		double a =  Math.pow(Math.sin(dlat / 2),2) + Math.pow(Math.sin(dlon / 2),2) * Math.cos(restaurantLat) * Math.cos(postcodeLat);
		double c = 2 * Math.asin(Math.sqrt(a));
		double answer = radius*c*1000;
		this.distance = Math.round(answer);

	}
	
	private void calculateLatLong() {
		try {

			String postcodeToGet = this.name.replaceAll("\\s+","");
			URL url = new URL("https://www.southampton.ac.uk/~ob1a12/postcode/postcode.php?postcode="+postcodeToGet);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

			BufferedReader in = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));

			String inputLine;

			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			//print in String
			//System.out.println(response.toString());
			String[] late = response.toString().substring(1).split(",");
			/*for(String test: late)
			{
				System.out.println(test);
			}*/
			String[] latArray = late[1].split(":");
			String[] longArray = late[2].split(":");

			Double lat = Double.valueOf(latArray[1].replaceAll("\"", ""));
			Double longValue = Double.valueOf(longArray[1].substring(0,longArray[1].length()-1).replaceAll("\"", ""));

			/*System.out.println(lat);
			System.out.println(longValue);
			System.out.println();*/

			this.latLong = new HashMap<String, Double>();
			latLong.put("lat", lat);
			latLong.put("lon", longValue);
		} catch (IOException e) {
			System.out.println("Error");
		}
	}
	
}

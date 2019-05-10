package comp1206.sushi.common.DataPersistence;

import comp1206.sushi.common.Drone;
import comp1206.sushi.common.Postcode;

public class DronePersistence {
    private Number speed;
    private Number progress;
    private Number capacity;
    private Number battery;
    private String status;
    private Postcode base;
    private Postcode source;
    private Postcode destination;

    public DronePersistence(Drone drone) {
        this.setSpeed(drone.getSpeed());
        this.setCapacity(drone.getCapacity());
        this.setBattery(drone.getBattery());
        this.setBase(drone.getBase());

    }

    public Number getSpeed() {
        return speed;
    }

    public void setSpeed(Number speed) {
        this.speed = speed;
    }

    public Number getProgress() {
        return progress;
    }

    public void setProgress(Number progress) {
        this.progress = progress;
    }

    public Number getCapacity() {
        return capacity;
    }

    public void setCapacity(Number capacity) {
        this.capacity = capacity;
    }

    public Number getBattery() {
        return battery;
    }

    public void setBattery(Number battery) {
        this.battery = battery;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Postcode getBase() {
        return base;
    }

    public void setBase(Postcode base) {
        this.base = base;
    }

    public Postcode getSource() {
        return source;
    }

    public void setSource(Postcode source) {
        this.source = source;
    }

    public Postcode getDestination() {
        return destination;
    }

    public void setDestination(Postcode destination) {
        this.destination = destination;
    }
}

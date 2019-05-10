package comp1206.sushi.common.DataPersistence;

import comp1206.sushi.common.Staff;

public class StaffPersistence {


    private String name;
    private String status;
    private Number fatigue;

    public StaffPersistence(Staff staff) {
        this.name = staff.getName();
        this.status = "Idle";
        this.fatigue = staff.getFatigue();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Number getFatigue() {
        return fatigue;
    }

    public void setFatigue(Number fatigue) {
        this.fatigue = fatigue;
    }
}

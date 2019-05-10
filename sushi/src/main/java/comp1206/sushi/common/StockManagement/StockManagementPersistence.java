package comp1206.sushi.common.StockManagement;

import java.io.Serializable;

public class StockManagementPersistence implements Serializable {
    StockManagement stockManagement;

    public StockManagementPersistence(StockManagement stockManagement) {
        this.stockManagement = stockManagement;
    }
}

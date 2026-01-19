package dss;


import dss.cadeiaRestaurantesLN.CadeiaRestaurantesLNFacade;
import dss.cadeiaRestaurantesLN.ICadeiaRestaurantesLN;
import dss.cadeiaRestaurantesUI.CadeiaRestaurantesController;
import dss.cadeiaRestaurantesUI.CadeiaRestaurantesView;

/**
 * Main application entry point for the Restaurant Chain Management System.
 * Sets up the MVC architecture by instantiating the model, controller, and view components.
 */
public class Application {
    /**
     * Main method that initializes and starts the application.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        ICadeiaRestaurantesLN model = new CadeiaRestaurantesLNFacade();
        CadeiaRestaurantesController controller = new CadeiaRestaurantesController(model);
        CadeiaRestaurantesView view = new CadeiaRestaurantesView(controller);

        view.run();
    }
}
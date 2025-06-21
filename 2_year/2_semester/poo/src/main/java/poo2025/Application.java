package poo2025;

import poo2025.mvc.SpotifUM;
import poo2025.mvc.SpotifUMController;
import poo2025.mvc.SpotifUMView;

/**
 * Represents the main class for the application. This class is responsible for initializing
 * the application's core components and starting the program.
 */
public class Application {
    /**
     * The entry point of the application. Initializes the model, controller, and view,
     * and starts the application by invoking the view's run method.
     *
     * @param args the command-line arguments passed to the program
     */
    public static void main(String[] args) {
        SpotifUM model = new SpotifUM();
        SpotifUMController controller = new SpotifUMController(model);
        SpotifUMView view = new SpotifUMView(controller);
        view.run();
    }
}
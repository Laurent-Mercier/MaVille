package ca.umontreal.restApi;
import ca.umontreal.restApi.controllers.CityController;
import ca.umontreal.restApi.controllers.RequestController;
import ca.umontreal.restApi.controllers.UserController;
import io.javalin.Javalin;


public class RestApi {
    private  Javalin app;


    public RestApi() {
        this.app = Javalin.create();
    }

    private void setRoutes(){
       new CityController(this.app);
       new UserController(this.app);
       new RequestController(this.app);
    }
    /**
     * Arrête le serveur REST API si en cours d'exécution.
     */
    public void stop() {
        if (this.app != null) {
            this.app.stop();
            System.out.println("Rest API server stopped.");
        }
    }

    /**
     * Démarre le serveur REST API
     */
    public void start() {
        this.app=this.app.start(7070);
        setRoutes();
    }
}

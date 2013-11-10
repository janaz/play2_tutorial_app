package controllers.clustrino.secured;


import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

public class Dashboard extends Secured {
    public static Result dashboard() {
        return ok(views.html.clustrino.dashboard.render());
    }

}

package controllers.clustrino.secured;


import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import controllers.Application;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

@Restrict(@Group(Application.USER_ROLE))
public class Dashboard extends Controller {
    public static Result dashboard() {
        return ok(views.html.clustrino.dashboard.render());
    }

}

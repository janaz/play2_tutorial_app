package controllers.neutrino.secured;


import play.mvc.Result;

public class Dashboard extends Secured {
    public static Result dashboard() {
        return ok(views.html.neutrino.dashboard.render());
    }

}

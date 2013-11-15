package controllers.clustrino.secured;


import play.mvc.Result;

public class Dashboard extends Secured {
    public static Result dashboard() {
        return ok(views.html.clustrino.dashboard.render());
    }

}

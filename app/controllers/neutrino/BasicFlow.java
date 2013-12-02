package controllers.neutrino;

import play.mvc.Controller;
import play.mvc.Result;

public class BasicFlow extends Controller {

    public static Result index() {
        return ok(views.html.clustrino.index.render());
    }

    public static Result testAction() {
        return ok(views.html.clustrino.test_action.render());
    }


}

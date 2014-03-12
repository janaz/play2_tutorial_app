package controllers.neutrino;

import play.mvc.Controller;
import play.mvc.Result;

public class BasicFlow extends Controller {

    public static Result index() {
        return ok(views.html.neutrino.index.render());
    }

    public static Result testAction() {
        return ok(views.html.neutrino.test_action.render());
    }


}

package controllers.neutrino.secured;

import play.mvc.Controller;
import play.mvc.Security;

@Security.Authenticated(controllers.Secured.class)
public abstract class Secured extends Controller {
}

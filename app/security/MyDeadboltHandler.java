package security;

import models.configuration.User;
import play.libs.F;
import play.mvc.Http;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.core.models.Subject;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUserIdentity;
import play.mvc.SimpleResult;

public class MyDeadboltHandler extends AbstractDeadboltHandler {
    @Override
    public F.Promise<SimpleResult> beforeAuthCheck(final Http.Context context) {
        if (PlayAuthenticate.isLoggedIn(context.session())) {
            return null;
        } else {
            return F.Promise.promise(new F.Function0<SimpleResult>() {
                @Override
                public SimpleResult apply() throws Throwable {
                    final String originalUrl = PlayAuthenticate
                            .storeOriginalUrl(context);

                    context.flash().put("error",
                            "You need to log in first, to view '" + originalUrl + "'");
                    return redirect(PlayAuthenticate.getResolver().login());

                }
            });
        }
    }

    @Override
    public Subject getSubject(final Http.Context context) {
        final AuthUserIdentity u = PlayAuthenticate.getUser(context);
        // Caching might be a good idea here
        return User.findByAuthUserIdentity(u);
    }

}



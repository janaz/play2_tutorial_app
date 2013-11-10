package providers;

import models.MyExtendedIdentity;
import providers.MyUsernamePasswordAuthProvider.MySignup;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.NameIdentity;

public class MyUsernamePasswordAuthUser extends UsernamePasswordAuthUser
		implements NameIdentity, MyExtendedIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private final String name;
    private final String extraInfo;

	public MyUsernamePasswordAuthUser(final MySignup signup) {
		super(signup.password, signup.email);
        this.name = signup.name;
        this.extraInfo = signup.extraInfo;
	}

	/**
	 * Used for password reset only - do not use this to signup a user!
	 * @param password
	 */
	public MyUsernamePasswordAuthUser(final String password) {
		super(password, null);
		name = null;
        this.extraInfo = null;
	}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getExtraInfo() {
        return extraInfo;
    }
}

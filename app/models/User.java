package models;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;
import models.TokenAction.Type;
import models.clustrino.CsvFile;
import org.hibernate.validator.constraints.Email;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.*;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
@Entity
@Table(name = "users")
public class User extends Model implements Subject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@Email
	// if you make this unique, keep in mind that users *must* merge/link their
	// accounts then on signup with additional providers
	// @Column(unique = true)
	public String email;

	public String name;

    public String password;

    public String extraInfo;

    public String firstName;
	
	public String lastName;

    @OneToMany(mappedBy="user")
    public List<CsvFile> files;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date lastLogin;

	public boolean active;

	public boolean emailValidated;

	public static final Finder<Long, User> find = new Finder<Long, User>(
			Long.class, User.class);

	@Override
	public String getIdentifier()
	{
		return Long.toString(id);
	}

	@Override
	public List<? extends Role> getRoles() {
		return Arrays.asList(new Role[]{new Role() {
            @Override
            public String getName() {
                return controllers.Application.USER_ROLE;
            }
        }});
	}

	@Override
	public List<? extends Permission> getPermissions() {
		return Arrays.asList(new Permission[]{});
	}

	public static boolean existsByAuthUserIdentity(
			final AuthUserIdentity identity) {
		final ExpressionList<User> exp;
		if (identity instanceof UsernamePasswordAuthUser) {
			exp = getUsernamePasswordAuthUserFind((UsernamePasswordAuthUser) identity);
		} else {
			exp = getAuthUserFind(identity);
		}
		return exp.findRowCount() > 0;
	}

	private static ExpressionList<User> getAuthUserFind(
			final AuthUserIdentity identity) {
		return find.where().eq("active", true)
				.eq("password", identity.getId());
	}

	public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
		if (identity == null) {
			return null;
		}
		if (identity instanceof UsernamePasswordAuthUser) {
			return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
		} else {
			return getAuthUserFind(identity).findUnique();
		}
	}

    public static User findByUsernamePasswordIdentity(
            final UsernamePasswordAuthUser identity) {
        return getEmailUserFind(identity.getEmail()).findUnique();

    }

    private static ExpressionList<User> getUsernamePasswordAuthUserFind(
			final UsernamePasswordAuthUser identity) {
        return getEmailUserFind(identity.getEmail());
	}

	public static User create(final AuthUser authUser) {
		final User user = new User();
		user.active = true;
		user.lastLogin = new Date();
        user.password = authUser.getId();

		if (authUser instanceof EmailIdentity) {
			final EmailIdentity identity = (EmailIdentity) authUser;
			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application as a security breach there might
			// break your security as well!
			user.email = identity.getEmail();
			user.emailValidated = false;
		}

		if (authUser instanceof NameIdentity) {
			final NameIdentity identity = (NameIdentity) authUser;
			final String name = identity.getName();
			if (name != null) {
				user.name = name;
			}
		}
		
		if (authUser instanceof FirstLastNameIdentity) {
		  final FirstLastNameIdentity identity = (FirstLastNameIdentity) authUser;
		  final String firstName = identity.getFirstName();
		  final String lastName = identity.getLastName();
		  if (firstName != null) {
		    user.firstName = firstName;
		  }
		  if (lastName != null) {
		    user.lastName = lastName;
		  }
		}

        if (authUser instanceof MyExtendedIdentity) {
            final MyExtendedIdentity identity = (MyExtendedIdentity) authUser;
            final String extraInfo = identity.getExtraInfo();
            user.extraInfo = extraInfo;

        }


        user.save();
		return user;
	}

	public static void setLastLoginDate(final AuthUser knownUser) {
		final User u = User.findByAuthUserIdentity(knownUser);
		u.lastLogin = new Date();
		u.save();
	}

	public static User findByEmail(final String email) {
		return getEmailUserFind(email).findUnique();
	}

	private static ExpressionList<User> getEmailUserFind(final String email) {
		return find.where().eq("active", true).eq("email", email);
	}

	public static void verify(final User unverified) {
		// You might want to wrap this into a transaction
		unverified.emailValidated = true;
		unverified.save();
		TokenAction.deleteByUser(unverified, Type.EMAIL_VERIFICATION);
	}

	public void changePassword(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		password = authUser.getHashedPassword();
		save();
	}

	public void resetPassword(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		// You might want to wrap this into a transaction
		this.changePassword(authUser, create);
		TokenAction.deleteByUser(this, Type.PASSWORD_RESET);
	}
}

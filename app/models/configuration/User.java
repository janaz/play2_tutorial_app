package models.configuration;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import com.avaje.ebean.ExpressionList;
import com.clustrino.profiling.MetadataSchema;
import com.clustrino.profiling.StagingSchema;
import com.clustrino.profiling.metadata.File;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import models.clustrino.CsvFile;
import models.configuration.TokenAction.Type;
import org.hibernate.validator.constraints.Email;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
@Entity
@Table(name = "User")
public class User extends Model implements Subject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @Column(name="ID")
    public Integer id;

    @Column(name="CompanyName", length = 60)
    public String companyName;

    @Column(name="ContactName", length = 60)
    public String contactName;

    @Column(name="ContactPhone", length = 20)
    public String contactPhone;

    @Column(name="ContactEmail", length = 200, unique=true)
    @Email
    public String email;

    @Column(name="AddressLine1", length = 120)
    public String addressLine1;

    @Column(name="AddressLine2", length = 120)
    public String addressLine2;

    @Column(name="AddressLine3", length = 120)
    public String addressLine3;

    @Column(name="Suburb", length = 60)
    public String suburb;

    @Column(name="State", length = 30)
    public String state;

    @Column(name="Postcode")
    public Integer postcode;

    @Column(name="EncryptedPassword")
    public String password;

    @Column(name="CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name="ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    @Column(name="LastLogin")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date lastLogin;

    @Column(name="Active")
    public boolean active;

    @Column(name="EmailValidated")
    public boolean emailValidated;

	public static final Finder<Integer, User> find = new Finder<Integer, User>(
            Integer.class, User.class);

    public List<CsvFile> getFiles() {
        //File.
        return null;
    }


    @Override
	public String getIdentifier()
	{
		return Integer.toString(id);
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
        user.creationTimestamp = new Date();
        user.password = authUser.getId();

		if (authUser instanceof EmailIdentity) {
			final EmailIdentity identity = (EmailIdentity) authUser;
			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application as a security breach there might
			// break your security as well!
			user.email = identity.getEmail();
			user.emailValidated = false;
		}

        if (authUser instanceof MyExtendedIdentity) {
            final MyExtendedIdentity identity = (MyExtendedIdentity) authUser;
            user.companyName = identity.getCompanyName();
            user.contactName = identity.getContactName();
            user.contactPhone = identity.getContactPhone();
            user.addressLine1 = identity.getAddressLine1();
            user.addressLine2 = identity.getAddressLine2();
            user.addressLine3 = identity.getAddressLine3();
            user.suburb = identity.getSuburb();
            user.state = identity.getState();
            user.postcode = identity.getPostcode();
        }
        user.save();
        if (user.id != null) {
            user.createDatabases();
        }
		return user;
	}

    private void createDatabases() {
        StagingSchema stg = new StagingSchema(id, null);
        MetadataSchema mtd = new MetadataSchema(id);
        mtd.createDatabase();
        mtd.createTables();
        stg.createDatabase();
    }

    public static void setLastLoginDate(final AuthUser knownUser) {
		final User u = User.findByAuthUserIdentity(knownUser);
		u.lastLogin = new Date();
        u.modificationTimestamp = new Date();
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
        unverified.modificationTimestamp = new Date();
		unverified.save();
		TokenAction.deleteByUser(unverified, Type.EMAIL_VERIFICATION);
	}

	public void changePassword(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		password = authUser.getHashedPassword();
        modificationTimestamp = new Date();
		save();
	}

	public void resetPassword(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		// You might want to wrap this into a transaction
		this.changePassword(authUser, create);
		TokenAction.deleteByUser(this, Type.PASSWORD_RESET);
	}
}

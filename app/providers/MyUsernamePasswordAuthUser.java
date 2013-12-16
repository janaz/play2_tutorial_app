package providers;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.neutrino.models.configuration.MyExtendedIdentity;
import providers.MyUsernamePasswordAuthProvider.MySignup;

public class MyUsernamePasswordAuthUser extends UsernamePasswordAuthUser
		implements MyExtendedIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private final String companyName;
    private final String contactName;
    private final String contactPhone;
    private final String addressLine3;
    private final String addressLine2;
    private final String addressLine1;
    private final String suburb;
    private final String state;
    private final Integer postcode;

    public MyUsernamePasswordAuthUser(final MySignup signup) {
		super(signup.password, signup.email);
        this.companyName = signup.companyName;
        this.contactName = signup.contactName;
        this.contactPhone = signup.contactPhone;
        this.addressLine1 = signup.addressLine1;
        this.addressLine2 = signup.addressLine2;
        this.addressLine3 = signup.addressLine3;
        this.suburb = signup.suburb;
        this.state = signup.state;
        this.postcode = signup.postcode;
	}

	/**
	 * Used for password reset only - do not use this to signup a user!
	 * @param password
	 */
	public MyUsernamePasswordAuthUser(final String password) {
		super(password, null);
		companyName = null;
        contactName = null;
        contactPhone = null;
        addressLine1 = null;
        addressLine2 = null;
        addressLine3 = null;
        suburb = null;
        state = null;
        postcode = null;
	}

    @Override
    public String getCompanyName() {
        return companyName;  
    }

    @Override
    public String getContactName() {
        return contactName;  
    }

    @Override
    public String getContactPhone() {
        return contactPhone;  
    }

    @Override
    public String getAddressLine1() {
        return addressLine1;  
    }

    @Override
    public String getAddressLine2() {
        return addressLine2;  
    }

    @Override
    public String getAddressLine3() {
        return addressLine3;  
    }

    @Override
    public String getSuburb() {
        return suburb;  
    }

    @Override
    public String getState() {
        return state;  
    }

    @Override
    public Integer getPostcode() {
        return postcode;  
    }
}


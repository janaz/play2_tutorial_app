package com.neutrino.models.core;

import com.neutrino.profiling.CoreSchema;

/**
 * Created by tomasz.janowski on 2/03/14.
 */
public class Initializer {
    private final String serverName;

    public Initializer(int userId) {
        this.serverName = (new CoreSchema(userId)).server().getName();
    }

    private void createPersonNameType(Integer id, String type) {
        PersonNameType t = new PersonNameType();
        t.id = id;
        t.type = type;
        t.save(serverName);
    }

    private void createPersonExternalIdentifierType(Integer id, String type) {
        PersonExternalIdentifierType t = new PersonExternalIdentifierType();
        t.id = id;
        t.type = type;
        t.save(serverName);
    }

    private void createPersonAnniversaryType(Integer id, String type) {
        PersonAnniversaryType t = new PersonAnniversaryType();
        t.id = id;
        t.type = type;
        t.save(serverName);
    }

    private void createPersonPhoneType(Integer id, String type) {
        PersonPhoneType t = new PersonPhoneType();
        t.id = id;
        t.type = type;
        t.save(serverName);
    }

    private void createPersonAddressType(Integer id, String type) {
        PersonAddressType t = new PersonAddressType();
        t.id = id;
        t.type = type;
        t.save(serverName);
    }

    public void createReferenceData() {
        createPersonNameType(1, "Legal Name");
        createPersonNameType(2, "Preferred Name");
        createPersonNameType(3, "Business Name");
        createPersonNameType(4, "Alias Name");
        createPersonNameType(5, "Maiden Name");

        createPersonExternalIdentifierType(1, "Passport");
        createPersonExternalIdentifierType(2, "Driver License");
        createPersonExternalIdentifierType(3, "Photo ID");

        createPersonAnniversaryType(1, "Date of Birth");
        createPersonAnniversaryType(2, "Date of Death");
        createPersonAnniversaryType(3, "Date of Join");

        createPersonPhoneType(1, "Home phone");
        createPersonPhoneType(2, "Mobile phone");
        createPersonPhoneType(3, "Business phone");
        createPersonPhoneType(4, "Fax");

        createPersonAddressType(1, "Residential");
        createPersonAddressType(2, "Postal");
        createPersonAddressType(3, "Business");
        createPersonAddressType(4, "Delivery");
        createPersonAddressType(5, "Summer Residence");
        createPersonAddressType(6, "PO Box");
    }
}

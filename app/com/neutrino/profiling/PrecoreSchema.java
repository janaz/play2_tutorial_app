package com.neutrino.profiling;

import com.avaje.ebean.EbeanServer;
import com.neutrino.models.precore.PersonAddress;
import com.neutrino.models.precore.PersonName;
import com.neutrino.models.core_common.*;

import java.util.Arrays;
import java.util.List;

public class PrecoreSchema {
    private static final List<Class<?>> CLASSES = Arrays.asList(new Class<?>[]{
            PersonAddress.class,
            PersonAddressType.class,
            PersonAnniversary.class,
            PersonAnniversaryType.class,
            PersonEmail.class,
            PersonExternalIdentifier.class,
            PersonExternalIdentifierType.class,
            PersonGender.class,
            PersonHeader.class,
            PersonLanguage.class,
            PersonMaritalStatus.class,
            PersonName.class,
            PersonNameType.class,
            PersonOccupation.class,
            PersonPhone.class,
            PersonPhoneType.class});

    private final Integer userId;

    public PrecoreSchema(Integer userId) {
        this.userId = userId;
    }

    private String databaseName() {
        return String.format("Precore%03d", this.userId);
    }

    public boolean isCreated() {
        return EbeanServerManager.getManager().isCreated(databaseName());
    }

    public EbeanServer server() {
        return EbeanServerManager.getManager().getServer(databaseName(), CLASSES, false);
    }

    public void createTables() {
        EbeanServer srv = EbeanServerManager.getManager().getServer(databaseName(), CLASSES, true);
        srv.beginTransaction().end();
    }

    public boolean createDatabase() {
        return EbeanServerManager.getManager().createDatabase(databaseName());

    }

    public void populateTables() {
        (ReferenceData.forPrecore(userId)).createReferenceData();
    }
}

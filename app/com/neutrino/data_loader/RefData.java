package com.neutrino.data_loader;

import java.util.Arrays;
import java.util.List;

/**
 * Created by tomasz.janowski on 31/03/14.
 */
public class RefData {
    public static final CoreSchema CORE_SCHEMA;
    public static final CoreSchema PRECORE_SCHEMA;
    public static final CoreSchemaTypeTable PERSON_NAME_TYPE;
    public static final CoreSchemaTypeTable PERSON_PHONE_TYPE;
    public static final CoreSchemaTypeTable PERSON_EXTERNAL_IDENTIFIER_TYPE;
    public static final CoreSchemaTypeTable PERSON_ADDRESS_TYPE;
    public static final CoreSchemaTypeTable PERSON_ANNIVERSARY_TYPE;
    public static final CoreSchemaTable PERSON_HEADER;
    public static final CoreSchemaTable PERSON_ADDRESS;
    public static final CoreSchemaTable PERSON_NAME;
    public static final CoreSchemaTable PERSON_ADDRESS_CORE;
    public static final CoreSchemaTable PERSON_NAME_CORE;
    public static final CoreSchemaTable PERSON_ANNIVERSARY;
    public static final CoreSchemaTable PERSON_EMAIL;
    public static final CoreSchemaTable PERSON_EXTERNAL_IDENTIFIER;
    public static final CoreSchemaTable PERSON_GENDER;
    public static final CoreSchemaTable PERSON_LANGUAGE;
    public static final CoreSchemaTable PERSON_MARITAL_STATUS;
    public static final CoreSchemaTable PERSON_OCCUPATION;
    public static final CoreSchemaTable PERSON_PHONE;
    public static final String FULL_NAME_COLUMN = "FullName";
    public static final String FULL_ADDRESS_LINE_COLUMN = "FullAddressLine";
    public static final String FULL_AREA_COLUMN = "FullArea";
    public static final String PERSON_HEADER_TABLE_NAME = "PersonHeader";

    public static final List<CoreSchemaTable> CORE_TABLES;
    public static final List<CoreSchemaTable> PRECORE_TABLES;


    static {
        PERSON_NAME_TYPE = new CoreSchemaTypeTable("PersonNameType", "NameTypeID")
                .addValue(1, "Legal Name")
                .addValue(2, "Preferred Name")
                .addValue(3, "Business Name")
                .addValue(4, "Alias Name")
                .addValue(5, "Maiden Name");
        PERSON_EXTERNAL_IDENTIFIER_TYPE = new CoreSchemaTypeTable("PersonExternalIdentifierType", "ExternalIdentifierTypeID")
                .addValue(1, "Passport")
                .addValue(2, "Driver License")
                .addValue(3, "Photo ID");
        PERSON_ANNIVERSARY_TYPE = new CoreSchemaTypeTable("PersonAnniversaryType", "AnniversaryTypeID")
                .addValue(1, "Date of Birth")
                .addValue(2, "Date of Death")
                .addValue(3, "Date of Join");
        PERSON_PHONE_TYPE = new CoreSchemaTypeTable("PersonPhoneType", "PhoneTypeID")
                .addValue(1, "Home phone")
                .addValue(2, "Mobile phone")
                .addValue(3, "Business phone")
                .addValue(4, "Fax");
        PERSON_ADDRESS_TYPE = new CoreSchemaTypeTable("PersonAddressType", "AddressTypeID")
                .addValue(1, "Residential")
                .addValue(2, "Postal")
                .addValue(3, "Business")
                .addValue(4, "Delivery")
                .addValue(5, "Summer Residence")
                .addValue(6, "PO Box");

        PERSON_HEADER = new CoreSchemaTable(PERSON_HEADER_TABLE_NAME)
                .addColumn(new CoreSchemaColumn("HeaderID", "INTEGER").id())
                .addColumn(new CoreSchemaColumn("DataSetID", "INTEGER").notNull())
                .addColumn(new CoreSchemaColumn("SourceID", "VARCHAR", 60).selectable().notNull())
                .addColumn(new CoreSchemaColumn("CreationTimestamp", "TIMESTAMP").notNull())
                .addColumn(new CoreSchemaColumn("MatchType", "VARCHAR", 3))
                .addColumn(new CoreSchemaColumn("MatchWeight", "DECIMAL(5,2)"))
                .addColumn(new CoreSchemaColumn("MatchSetID", "INTEGER"));

        PERSON_ADDRESS = new CoreSchemaTable("PersonAddress", PERSON_ADDRESS_TYPE)
                .addColumn(new CoreSchemaColumn("AddressID", "INTEGER").id())
                .addColumn(new CoreSchemaColumn("HeaderID", "INTEGER").notNull().foreignKey(PERSON_HEADER))
                .addColumn(new CoreSchemaColumn("HouseNumber", "VARCHAR", 10).selectable())
                .addColumn(new CoreSchemaColumn("HouseNumberSuffix", "VARCHAR", 3).selectable())
                .addColumn(new CoreSchemaColumn("StreetName", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("StreetType", "VARCHAR", 5).selectable())
                .addColumn(new CoreSchemaColumn("StreetSuffix", "VARCHAR", 3).selectable())
                .addColumn(new CoreSchemaColumn("PostalDelType", "VARCHAR", 12).selectable())
                .addColumn(new CoreSchemaColumn("PostalDelNumber", "VARCHAR", 10).selectable())
                .addColumn(new CoreSchemaColumn("PostalDelNumberSuffix", "VARCHAR", 3).selectable())
                .addColumn(new CoreSchemaColumn("FloorType", "VARCHAR", 3).selectable())
                .addColumn(new CoreSchemaColumn("FloorNumber", "VARCHAR", 10).selectable())
                .addColumn(new CoreSchemaColumn("UnitType", "VARCHAR", 5).selectable())
                .addColumn(new CoreSchemaColumn("UnitNumber", "VARCHAR", 10).selectable())
                .addColumn(new CoreSchemaColumn("BuildingName", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("Suburb", "VARCHAR", 60).selectable())
                .addColumn(new CoreSchemaColumn("Postcode", "VARCHAR", 10).selectable())
                .addColumn(new CoreSchemaColumn("State", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("Country", "VARCHAR", 60).selectable())
                .addColumn(new CoreSchemaColumn(FULL_AREA_COLUMN, "VARCHAR", 255))
                .addColumn(new CoreSchemaColumn(FULL_ADDRESS_LINE_COLUMN, "VARCHAR", 255).full())
                .addColumn(new CoreSchemaColumn("CreationTimestamp", "TIMESTAMP").notNull())
                .addColumn(new CoreSchemaColumn("ModificationTimestamp", "TIMESTAMP"));

        PERSON_ADDRESS_CORE = new CoreSchemaTable("PersonAddress", PERSON_ADDRESS_TYPE)
                .addColumn(new CoreSchemaColumn("AddressID", "INTEGER").id())
                .addColumn(new CoreSchemaColumn("HeaderID", "INTEGER").notNull().foreignKey(PERSON_HEADER))
                .addColumn(new CoreSchemaColumn("HouseNumber", "VARCHAR", 10).selectable())
                .addColumn(new CoreSchemaColumn("HouseNumberSuffix", "VARCHAR", 3).selectable())
                .addColumn(new CoreSchemaColumn("StreetName", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("StreetType", "VARCHAR", 5).selectable())
                .addColumn(new CoreSchemaColumn("StreetSuffix", "VARCHAR", 3).selectable())
                .addColumn(new CoreSchemaColumn("PostalDelType", "VARCHAR", 12).selectable())
                .addColumn(new CoreSchemaColumn("PostalDelNumber", "VARCHAR", 10).selectable())
                .addColumn(new CoreSchemaColumn("PostalDelNumberSuffix", "VARCHAR", 3).selectable())
                .addColumn(new CoreSchemaColumn("FloorType", "VARCHAR", 3).selectable())
                .addColumn(new CoreSchemaColumn("FloorNumber", "VARCHAR", 10).selectable())
                .addColumn(new CoreSchemaColumn("UnitType", "VARCHAR", 5).selectable())
                .addColumn(new CoreSchemaColumn("UnitNumber", "VARCHAR", 10).selectable())
                .addColumn(new CoreSchemaColumn("BuildingName", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("Suburb", "VARCHAR", 60).selectable())
                .addColumn(new CoreSchemaColumn("Postcode", "VARCHAR", 10).selectable())
                .addColumn(new CoreSchemaColumn("State", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("Country", "VARCHAR", 60).selectable())
                .addColumn(new CoreSchemaColumn("CreationTimestamp", "TIMESTAMP").notNull())
                .addColumn(new CoreSchemaColumn("ModificationTimestamp", "TIMESTAMP"));

        PERSON_NAME_CORE = new CoreSchemaTable("PersonName", PERSON_NAME_TYPE)
                .addColumn(new CoreSchemaColumn("NameID", "INTEGER").id())
                .addColumn(new CoreSchemaColumn("HeaderID", "INTEGER").notNull().foreignKey(PERSON_HEADER))
                .addColumn(new CoreSchemaColumn("Salutation", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("FirstName", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("MiddleName", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("Surname", "VARCHAR", 60).selectable())
                .addColumn(new CoreSchemaColumn("Generation", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("Suffix", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("CreationTimestamp", "TIMESTAMP").notNull())
                .addColumn(new CoreSchemaColumn("ModificationTimestamp", "TIMESTAMP"));

        PERSON_NAME = new CoreSchemaTable("PersonName", PERSON_NAME_TYPE)
                .addColumn(new CoreSchemaColumn("NameID", "INTEGER").id())
                .addColumn(new CoreSchemaColumn("HeaderID", "INTEGER").notNull().foreignKey(PERSON_HEADER))
                .addColumn(new CoreSchemaColumn("Salutation", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("FirstName", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("MiddleName", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("Surname", "VARCHAR", 60).selectable())
                .addColumn(new CoreSchemaColumn("Generation", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("Suffix", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn(FULL_NAME_COLUMN, "VARCHAR", 120).full())
                .addColumn(new CoreSchemaColumn("CreationTimestamp", "TIMESTAMP").notNull())
                .addColumn(new CoreSchemaColumn("ModificationTimestamp", "TIMESTAMP"));

        PERSON_ANNIVERSARY = new CoreSchemaTable("PersonAnniversary", PERSON_ANNIVERSARY_TYPE)
                .addColumn(new CoreSchemaColumn("AnniversaryID", "INTEGER").id())
                .addColumn(new CoreSchemaColumn("HeaderID", "INTEGER").notNull().foreignKey(PERSON_HEADER))
                .addColumn(new CoreSchemaColumn("AnniversaryDate", "DATE").selectable())
                .addColumn(new CoreSchemaColumn("CreationTimestamp", "TIMESTAMP").notNull())
                .addColumn(new CoreSchemaColumn("ModificationTimestamp", "TIMESTAMP"));

        PERSON_EMAIL = new CoreSchemaTable("PersonEmail")
                .addColumn(new CoreSchemaColumn("EmailID", "INTEGER").id())
                .addColumn(new CoreSchemaColumn("HeaderID", "INTEGER").notNull().foreignKey(PERSON_HEADER))
                .addColumn(new CoreSchemaColumn("EmailAddress", "VARCHAR", 128).selectable())
                .addColumn(new CoreSchemaColumn("CreationTimestamp", "TIMESTAMP").notNull())
                .addColumn(new CoreSchemaColumn("ModificationTimestamp", "TIMESTAMP"));

        PERSON_EXTERNAL_IDENTIFIER = new CoreSchemaTable("PersonExternalIdentifier", PERSON_EXTERNAL_IDENTIFIER_TYPE)
                .addColumn(new CoreSchemaColumn("ExternalIdentifierID", "INTEGER").id())
                .addColumn(new CoreSchemaColumn("HeaderID", "INTEGER").notNull().foreignKey(PERSON_HEADER))
                .addColumn(new CoreSchemaColumn("Identifier", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("ExpiryDate", "DATE").selectable())
                .addColumn(new CoreSchemaColumn("CreationTimestamp", "TIMESTAMP").notNull())
                .addColumn(new CoreSchemaColumn("ModificationTimestamp", "TIMESTAMP"));

        PERSON_GENDER = new CoreSchemaTable("PersonGender")
                .addColumn(new CoreSchemaColumn("GenderID", "INTEGER").id())
                .addColumn(new CoreSchemaColumn("HeaderID", "INTEGER").notNull().foreignKey(PERSON_HEADER))
                .addColumn(new CoreSchemaColumn("GenderCode", "VARCHAR", 20).selectable())
                .addColumn(new CoreSchemaColumn("CreationTimestamp", "TIMESTAMP").notNull())
                .addColumn(new CoreSchemaColumn("ModificationTimestamp", "TIMESTAMP"));

        PERSON_LANGUAGE = new CoreSchemaTable("PersonLanguage")
                .addColumn(new CoreSchemaColumn("LanguageID", "INTEGER").id())
                .addColumn(new CoreSchemaColumn("HeaderID", "INTEGER").notNull().foreignKey(PERSON_HEADER))
                .addColumn(new CoreSchemaColumn("LanguageCode", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("CreationTimestamp", "TIMESTAMP").notNull())
                .addColumn(new CoreSchemaColumn("ModificationTimestamp", "TIMESTAMP"));

        PERSON_MARITAL_STATUS = new CoreSchemaTable("PersonMaritalStatus")
                .addColumn(new CoreSchemaColumn("MaritalStatusID", "INTEGER").id())
                .addColumn(new CoreSchemaColumn("HeaderID", "INTEGER").notNull().foreignKey(PERSON_HEADER))
                .addColumn(new CoreSchemaColumn("MaritalStatusCode", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("CreationTimestamp", "TIMESTAMP").notNull())
                .addColumn(new CoreSchemaColumn("ModificationTimestamp", "TIMESTAMP"));

        PERSON_OCCUPATION = new CoreSchemaTable("PersonOccupation")
                .addColumn(new CoreSchemaColumn("OccupationID", "INTEGER").id())
                .addColumn(new CoreSchemaColumn("HeaderID", "INTEGER").notNull().foreignKey(PERSON_HEADER))
                .addColumn(new CoreSchemaColumn("OccupationCode", "VARCHAR", 30).selectable())
                .addColumn(new CoreSchemaColumn("CreationTimestamp", "TIMESTAMP").notNull())
                .addColumn(new CoreSchemaColumn("ModificationTimestamp", "TIMESTAMP"));

        PERSON_PHONE = new CoreSchemaTable("PersonPhone", PERSON_PHONE_TYPE)
                .addColumn(new CoreSchemaColumn("PhoneID", "INTEGER").id())
                .addColumn(new CoreSchemaColumn("HeaderID", "INTEGER").notNull().foreignKey(PERSON_HEADER))
                .addColumn(new CoreSchemaColumn("CountryCode", "TINYINT").selectable())
                .addColumn(new CoreSchemaColumn("AreaCode", "TINYINT").selectable())
                .addColumn(new CoreSchemaColumn("PhoneNumber", "BIGINT").selectable())
                .addColumn(new CoreSchemaColumn("Extension", "INTEGER").selectable())
                .addColumn(new CoreSchemaColumn("CreationTimestamp", "TIMESTAMP").notNull())
                .addColumn(new CoreSchemaColumn("ModificationTimestamp", "TIMESTAMP"));

        CORE_TABLES = Arrays.asList(new CoreSchemaTable[]{PERSON_HEADER,
                PERSON_PHONE,
                PERSON_MARITAL_STATUS,
                PERSON_OCCUPATION,
                PERSON_LANGUAGE,
                PERSON_GENDER,
                PERSON_EXTERNAL_IDENTIFIER,
                PERSON_EMAIL,
                PERSON_ANNIVERSARY,
                PERSON_NAME_CORE,
                PERSON_ADDRESS_CORE});

        PRECORE_TABLES = Arrays.asList(new CoreSchemaTable[]{
                PERSON_HEADER,
                PERSON_PHONE,
                PERSON_MARITAL_STATUS,
                PERSON_OCCUPATION,
                PERSON_LANGUAGE,
                PERSON_GENDER,
                PERSON_EXTERNAL_IDENTIFIER,
                PERSON_EMAIL,
                PERSON_ANNIVERSARY,
                PERSON_NAME,
                PERSON_ADDRESS});

        CORE_SCHEMA = new CoreSchema("Core")
                .addTable(PERSON_HEADER)
                .addTable(PERSON_PHONE)
                .addTable(PERSON_MARITAL_STATUS)
                .addTable(PERSON_OCCUPATION)
                .addTable(PERSON_LANGUAGE)
                .addTable(PERSON_GENDER)
                .addTable(PERSON_EXTERNAL_IDENTIFIER)
                .addTable(PERSON_EMAIL)
                .addTable(PERSON_ANNIVERSARY)
                .addTable(PERSON_NAME_CORE)
                .addTable(PERSON_ADDRESS_CORE);

        PRECORE_SCHEMA = new CoreSchema("Precore")
                .addTable(PERSON_HEADER)
                .addTable(PERSON_PHONE)
                .addTable(PERSON_MARITAL_STATUS)
                .addTable(PERSON_OCCUPATION)
                .addTable(PERSON_LANGUAGE)
                .addTable(PERSON_GENDER)
                .addTable(PERSON_EXTERNAL_IDENTIFIER)
                .addTable(PERSON_EMAIL)
                .addTable(PERSON_ANNIVERSARY)
                .addTable(PERSON_NAME)
                .addTable(PERSON_ADDRESS);
    }
}

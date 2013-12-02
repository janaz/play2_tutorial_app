package com.neutrino.aws;

import com.amazonaws.auth.AWSCredentials;
import com.neutrino.AppConfiguration;

public class Credentials {
    private static final AWSCredentials credentials;

    static {
        credentials = new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return AppConfiguration.get().getConfig("aws").getString("access_key");
            }

            @Override
            public String getAWSSecretKey() {
                return AppConfiguration.get().getConfig("aws").getString("secret_key");
            }
        };
    }

    public static AWSCredentials getCredentials() {
        return credentials;
    }

}

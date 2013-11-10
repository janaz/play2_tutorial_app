package com.clustrino.aws;

import com.amazonaws.auth.AWSCredentials;

public class Credentials {
    private static final AWSCredentials credentials;

    static {
        credentials = new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return play.Play.application().configuration().getConfig("aws").getString("access_key");
            }

            @Override
            public String getAWSSecretKey() {
                return play.Play.application().configuration().getConfig("aws").getString("secret_key");
            }
        };
    }

    public static AWSCredentials getCredentials() {
        return credentials;
    }

}

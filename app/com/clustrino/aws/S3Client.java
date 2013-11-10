package com.clustrino.aws;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;

import java.io.File;
import java.io.InputStream;

public class S3Client {
    private final String bucket;
    private final Region region;

    public static S3Client inDefaultRegion(String bucket) {
        return inRegion(Region.getRegion(Regions.US_EAST_1), bucket);
    }
    public static S3Client inRegion(Region region, String bucket) {
        return new S3Client(region, bucket);
    }

    private S3Client(Region region, String bucket) {
        this.region = region;
        this.bucket = bucket;
    }

    private AmazonS3Client client() {
        AmazonS3Client client = new AmazonS3Client(Credentials.getCredentials());
        client.setRegion(region);
        return client;
    }

    public S3Object get(String objectName) {
        return client().getObject(bucket, objectName);
    }

    public boolean put(String objectName, File file) {
        PutObjectResult result = client().putObject(bucket, objectName, file);
        return result.getContentMd5() != null;
    }


}

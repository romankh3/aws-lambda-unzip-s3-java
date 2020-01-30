package com.github.romankh3.awslambda.s3.unzip;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * AWS Lambda function for unzipping new zip files to S3 bucket.
 */
public class AwsLambdaS3UnzipFiles implements RequestHandler<S3Event, Boolean> {

    private final AmazonS3 amazonS3Client = AmazonS3ClientBuilder.standard().build();

    @Override
    public Boolean handleRequest(S3Event s3Event, Context context) {
        for (S3EventNotificationRecord record : s3Event.getRecords()) {
            String s3Key = record.getS3().getObject().getKey();
            String s3Bucket = record.getS3().getBucket().getName();
            context.getLogger().log(String.format("found file: %s, for S3:%s", s3Key, s3Bucket));

            S3Object s3Object = amazonS3Client.getObject(new GetObjectRequest(s3Bucket, s3Key));
            ZipInputStream zis = new ZipInputStream(s3Object.getObjectContent());
            try {
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    amazonS3Client.putObject(s3Bucket, zipEntry.getName(), zis, null);
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
                zis.close();
            } catch (IOException e) {
                context.getLogger().log(e.getMessage());
                return false;
            }
        }
        return true;
    }
}

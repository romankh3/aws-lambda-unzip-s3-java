package com.github.romankh3.awslambda.s3.unzip;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3Entity;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.S3Object;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * AWS Lambda function for unzipping new zip files to S3 bucket.
 */
public class AwsLambdaS3UnzipFiles implements RequestHandler<S3Event, Map<S3Entity, Boolean>> {

    private final AmazonS3 amazonS3Client = AmazonS3ClientBuilder.defaultClient();

    @Override
    public Map<S3Entity, Boolean> handleRequest(S3Event s3Event, Context context) {
        Map<S3Entity, Boolean> resultMap = new HashMap<>();
        for (S3EventNotificationRecord record : s3Event.getRecords()) {
            String s3Bucket = record.getS3().getBucket().getName();
            String s3Key = record.getS3().getObject().getKey();

            boolean unzip;

            if (isZipFile(s3Key, context)) {
                unzip = unzipFile(s3Bucket, s3Key, context);
                if(unzip) {
                    deleteZipFile(s3Bucket, s3Key, context);
                }
            } else {
                unzip = false;
            }

            resultMap.put(record.getS3(), unzip);
        }
        return resultMap;
    }

    private boolean unzipFile(String s3Bucket, String s3Key, Context context) {
        context.getLogger().log(String.format("found file: %s, for S3 bucket: %s", s3Key, s3Bucket));
        S3Object s3Object = amazonS3Client.getObject(s3Bucket, s3Key);
        try (ZipInputStream zis = new ZipInputStream(s3Object.getObjectContent());) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                amazonS3Client.putObject(s3Bucket, String.format("%s/%s", s3Key, zipEntry.getName()), zis, null);
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        } catch (IOException e) {
            context.getLogger().log(e.getMessage());
            return false;
        }
        return true;
    }

    private void deleteZipFile(String s3Bucket, String s3Key, Context context) {
        context.getLogger().log(String.format("Deleting zip file %s/%s", s3Bucket, s3Key));
        amazonS3Client.deleteObject(s3Bucket, s3Key);
        context.getLogger().log(String.format("Deleted zip file %s/%s", s3Bucket, s3Key));
    }

    private boolean isZipFile(String s3Key, Context context) {
        // Detect file type
        Matcher matcher = Pattern.compile(".*\\.([^\\.]*)").matcher(s3Key);
        if (!matcher.matches()) {
            context.getLogger().log(String.format("Unable to detect file type for key %s", s3Key));
            return false;
        }
        String extension = matcher.group(1).toLowerCase();
        if (!"zip".equals(extension)) {
            context.getLogger().log(String.format("Skipping non-zip file = %s, with extension = %s", s3Key, extension));
            return false;
        }
        return true;
    }
}

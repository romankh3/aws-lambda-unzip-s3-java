package com.github.romankh3.awslambda.s3.unzip;

/**
 * AWS Lambda function for unzipping new zip files to S3 bucket.
 */
public class AwsLambdaS3UnzipFiles implements RequestHandler<S3Event, Boolean> {

    @Override
    public Boolean handleRequest(S3Event s3Event, Context context) {
        for (S3EventNotificationRecord record : s3Event.getRecords()) {
            String srcBucket = record.getS3().getBucket().getName();

            // Object key may have spaces or unicode non-ASCII characters.
            String srcKey = record.getS3().getObject().getKey()
                    .replace('+', ' ');
            srcKey = URLDecoder.decode(srcKey, "UTF-8");


        }
    }
}

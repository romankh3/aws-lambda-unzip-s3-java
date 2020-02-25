package com.github.romankh3.awslambda.s3.unzip;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3BucketEntity;
import com.amazonaws.services.s3.event.S3EventNotification.S3Entity;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.event.S3EventNotification.S3ObjectEntity;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Unit-level testing for AWS Lambda {@link AwsLambdaS3UnzipFiles}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AmazonS3ClientBuilder.class})
@PowerMockIgnore({"javax.management.*"})
public class AwsLambdaS3UnzipFilesTest {

    private static final String S3_NAME = "awsS3TestName";
    private static final String S3_KEY = "logos.zip";
    private File zipFile = new File("src/test/resources/logos.zip");

    private AmazonS3 amazonS3 = mock(AmazonS3.class);
    private AwsLambdaS3UnzipFiles lambda;

    private S3Event s3Event = mock(S3Event.class);
    private Context context = mock(Context.class);

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(AmazonS3ClientBuilder.class);
        when(AmazonS3ClientBuilder.defaultClient()).thenReturn(amazonS3);
        lambda = new AwsLambdaS3UnzipFiles();

        when(context.getLogger()).thenReturn(System.out::println);
        S3EventNotificationRecord record = mock(S3EventNotificationRecord.class);

        S3Entity s3Entity = mock(S3Entity.class);
        when(record.getS3()).thenReturn(s3Entity);

        S3ObjectEntity s3ObjectEntity = mock(S3ObjectEntity.class);
        when(s3Entity.getObject()).thenReturn(s3ObjectEntity);
        when(s3ObjectEntity.getKey()).thenReturn(S3_KEY);

        S3BucketEntity s3BucketEntity = mock(S3BucketEntity.class);
        when(s3Entity.getBucket()).thenReturn(s3BucketEntity);

        when(s3BucketEntity.getName()).thenReturn(S3_NAME);
        when(s3Event.getRecords()).thenReturn(singletonList(record));

        S3Object s3Object = mock(S3Object.class);
        when(amazonS3.getObject(S3_NAME, S3_KEY)).thenReturn(s3Object);

        when(s3Object.getObjectContent())
                .thenReturn(new S3ObjectInputStream(new FileInputStream(zipFile), null));
    }

    @Test
    public void shouldHandleRequest() throws IOException {
        //when
        Map<S3Entity, Boolean> result = lambda.handleRequest(s3Event, context);

        //then
        assertEquals(1, result.size());
        assertTrue(result.values().iterator().next());

        //and
        ZipInputStream zisForAssert = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry = zisForAssert.getNextEntry();

        //then
        while (zipEntry != null) {
            verify(amazonS3, times(1)).putObject(
                    eq(S3_NAME), eq(String.format("%s/%s", S3_KEY, zipEntry.getName())), any(), eq(null)
            );
            zipEntry = zisForAssert.getNextEntry();
        }
    }
}
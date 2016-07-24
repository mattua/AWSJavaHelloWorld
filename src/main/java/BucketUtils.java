import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by mattua on 23/07/2016.
 */


    public class BucketUtils {
        private static String bucketName     = "***bucket name to delete ***";

        public static void deleteBucket(String bucketName,AmazonS3Client s3client) throws IOException {

            try {
                System.out.println("Deleting S3 bucket: " + bucketName);
                ObjectListing objectListing = s3client.listObjects(bucketName);

                while (true) {
                    for ( Iterator<?> iterator = objectListing.getObjectSummaries().iterator(); iterator.hasNext(); ) {
                        S3ObjectSummary objectSummary = (S3ObjectSummary) iterator.next();
                        s3client.deleteObject(bucketName, objectSummary.getKey());
                    }

                    if (objectListing.isTruncated()) {
                        objectListing = s3client.listNextBatchOfObjects(objectListing);
                    } else {
                        break;
                    }
                };
                VersionListing list = s3client.listVersions(new ListVersionsRequest().withBucketName(bucketName));
                for ( Iterator<?> iterator = list.getVersionSummaries().iterator(); iterator.hasNext(); ) {
                    S3VersionSummary s = (S3VersionSummary)iterator.next();
                    s3client.deleteVersion(bucketName, s.getKey(), s.getVersionId());
                }
                s3client.deleteBucket(bucketName);


            } catch (AmazonServiceException ase) {
                System.out.println("Caught an AmazonServiceException, which " +
                        "means your request made it " +
                        "to Amazon S3, but was rejected with an error response" +
                        " for some reason.");
                System.out.println("Error Message:    " + ase.getMessage());
                System.out.println("HTTP Status Code: " + ase.getStatusCode());
                System.out.println("AWS Error Code:   " + ase.getErrorCode());
                System.out.println("Error Type:       " + ase.getErrorType());
                System.out.println("Request ID:       " + ase.getRequestId());
            } catch (AmazonClientException ace) {
                System.out.println("Caught an AmazonClientException, which " +
                        "means the client encountered " +
                        "an internal error while trying to " +
                        "communicate with S3, " +
                        "such as not being able to access the network.");
                System.out.println("Error Message: " + ace.getMessage());
            }
        }
    }


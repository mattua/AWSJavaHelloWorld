import com.amazonaws.auth.BasicAWSCredentials;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;

import java.util.List;

/**
 * Created by mattua on 06/06/2016.
 */
public class TestAWS {

    public static void main(String[] args){



        /*
        Need to make sure the IAM user to whom these keys belong is assigned the
        S3 readonly policy
         */


        BasicAWSCredentials awsCreds = new BasicAWSCredentials(Credentials.access_key_id, Credentials.secret_access_key);
        AmazonS3 s3Client = new AmazonS3Client(awsCreds);
       System.out.println("boom");

        List<Bucket> buckets = s3Client.listBuckets();

        for (Bucket bucket:buckets){

            System.out.println(bucket.getName());

        }

    }

}

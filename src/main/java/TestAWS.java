import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.apigateway.AmazonApiGatewayClient;

import com.amazonaws.regions.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;

import java.util.List;

/**
 * Created by mattua on 06/06/2016.
 */
public class TestAWS {

    public static void main(String[] args){

        String access_key_id= "AKIAIBPDKCCYWXMYBHOQ";

        String secret_access_key ="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";


        /*
        Need to make sure the IAM user to whom these keys belong is assigned the
        S3 readonly policy
         */


        BasicAWSCredentials awsCreds = new BasicAWSCredentials(access_key_id,secret_access_key);
        AmazonS3 s3Client = new AmazonS3Client(awsCreds);
       System.out.println("boom");

        List<Bucket> buckets = s3Client.listBuckets();

        for (Bucket bucket:buckets){

            System.out.println(bucket.getName());

        }

    }

}

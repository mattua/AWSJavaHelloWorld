import com.amazonaws.auth.BasicAWSCredentials;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by mattua on 06/06/2016.
 */
public class S3ServerSideSDKEncryption {


    public static void main(String[] args) throws Exception {





        BasicAWSCredentials awsCreds = new BasicAWSCredentials(Credentials.access_key_id, Credentials.secret_access_key);
        AmazonS3Client s3Client = new AmazonS3Client(awsCreds);


        for (Bucket bucket:s3Client.listBuckets()){

            BucketUtils.deleteBucket(bucket.getName(), s3Client);

        }



        String newBucketName = "mattua" + System.currentTimeMillis();
        s3Client.createBucket(newBucketName);

        String policy = readFileFromResources("encrypted-folder-policy.txt").replace("bucketname",newBucketName);

        /*
        This is a bucket policy - the bucket itself must be mentioned in the policy explicitly
         */

        System.out.println(policy);
        s3Client.setBucketPolicy(newBucketName, policy);

        final String fileName = "sometext.txt";

        File file = new File(S3ServerSideSDKEncryption.class.getResource(fileName).toURI());

        /*
        remember this is a new bucket and "folders" dont exist in S3, they are logical entities derived from the
        path specified in the key. S3 is just a key value store.

        they are created on the fly when we upload an object with a specific key path

        Also, the folder setting in the console on the S3 folder for server side encryption is a slightly misleading
        instruction to encrypt the selected resources - it does NOT set a persistant setting on all resources uploaded
        into that folder


         */


        {
            PutObjectRequest putRequest1 = new PutObjectRequest(newBucketName, "unencrypted/" + fileName + "." + System.currentTimeMillis(), file);
            PutObjectResult response1 = s3Client.putObject(putRequest1);
            System.out.println("Uploaded object encryption status is " +
                    response1.getSSEAlgorithm());
        }
        {
            PutObjectRequest putRequest1 = new PutObjectRequest(newBucketName, "encrypted/" + fileName + "." + System.currentTimeMillis(), file);

            try {
                PutObjectResult response1 = s3Client.putObject(putRequest1);
            } catch (Exception e){
                System.out.println("was not able to store an unencrypted file in this folder");
            }

        }
        {
            PutObjectRequest putRequest1 = new PutObjectRequest(newBucketName, "encrypted/" + fileName + "." + System.currentTimeMillis(), file);
            ObjectMetadata objectMetadata1 = new ObjectMetadata();
            objectMetadata1.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
            putRequest1.setMetadata(objectMetadata1);


            PutObjectResult response1 = s3Client.putObject(putRequest1);
            System.out.println("Uploaded object encryption status is " +
                    response1.getSSEAlgorithm());
        }

    }

    public static String readFileFromResources(String fileName)
            throws Exception
    {

        String path = S3ServerSideSDKEncryption.class.getResource(fileName).toURI().getPath();


        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded);
    }


}

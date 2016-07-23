import com.amazonaws.auth.BasicAWSCredentials;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by mattua on 06/06/2016.
 */
public class S3ServerSideSDKEncryption {

    public static void main(String[] args) throws Exception {


        run();


        /*
        Need to make sure the IAM user to whom these keys belong is assigned the
        S3 policy
         */


        BasicAWSCredentials awsCreds = new BasicAWSCredentials(Credentials.access_key_id, Credentials.secret_access_key);
        AmazonS3 s3Client = new AmazonS3Client(awsCreds);

        List<Bucket> buckets = s3Client.listBuckets();


        String newBucketName = "mattua" + System.currentTimeMillis();
        s3Client.createBucket(newBucketName);

        for (Bucket bucket:buckets){

            System.out.println(bucket.getName());

        }

        final String fileName = "sometext.txt";

        File file = new File(S3ServerSideSDKEncryption.class.getResource(fileName).toURI());

        System.out.println("Uploading to " + newBucketName);

        s3Client.putObject(new PutObjectRequest(newBucketName,fileName, file));


        // create Folder within new bucket
        s3Client.putObject(new PutObjectRequest(newBucketName,"newSubFolder/"+fileName, file));

        // Request server-side encryption.
        PutObjectRequest putRequest =new PutObjectRequest(newBucketName,fileName+".encrypted", file);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
        putRequest.setMetadata(objectMetadata);

        PutObjectResult response = s3Client.putObject(putRequest);
        System.out.println("Uploaded object encryption status is " +
                response.getSSEAlgorithm());


        // Attempt to upload unencrypted assets into bucket - show the bucket policy against mantua-encrypted bucket
        final String encryptedBucketName = "mantua-encrypted";

        PutObjectRequest putRequest1 =new PutObjectRequest(encryptedBucketName,fileName+".encrypted."+System.currentTimeMillis(), file);

        ObjectMetadata objectMetadata1 = new ObjectMetadata();
        objectMetadata1.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
        putRequest1.setMetadata(objectMetadata1);

        PutObjectResult response1 = s3Client.putObject(putRequest1);
        System.out.println("Uploaded object encryption status is " +
                response1.getSSEAlgorithm());

        PutObjectRequest putRequest2 =new PutObjectRequest(encryptedBucketName,fileName+".plaintext."+System.currentTimeMillis(), file);

        try {
            s3Client.putObject(putRequest2);
        } catch (Exception e){

        }

        /*
        xception in thread "main" com.amazonaws.services.s3.model.AmazonS3Exception: Access Denied (Service: Amazon S3; Status Code: 403; Error Code: AccessDenied; Request ID: 48503AC11F509383), S3 Extended Request ID: WbkMiWE5eHbDkMMNgGkvYj6qSn/9yjM8d9ZHbm3YUWXGET/5CG6fa7ha/Dg0tk0MyGT6sMI2ES0=
	at com.amazonaws.http.AmazonHttpClient.handleErrorResponse(AmazonHttpClient.java:1275)
	at com.amazonaws.http.AmazonHttpClient.executeOneRequest(AmazonHttpClient.java:873)
	at com.amazonaws.http.AmazonHttpClient.executeHelper(AmazonHttpClient.java:576)
	at com.amazonaws.http.AmazonHttpClient.doExecute(AmazonHttpClient.java:362)
	at com.amazonaws.http.AmazonHttpClient.executeWithTimer(AmazonHttpClient.java:328)

         */


       /*

        Set on a sub folder of mattua bucket

        {
	"Version": "2012-10-17",
	"Id": "PutObjPolicy",
	"Statement": [
		{
			"Sid": "DenyIncorrectEncryptionHeader",
			"Effect": "Deny",
			"Principal": "*",
			"Action": "s3:PutObject",
			"Resource": "arn:aws:s3:::mattua/encrypted/*",
			"Condition": {
				"StringNotEquals": {
					"s3:x-amz-server-side-encryption": "AES256"
				}
			}
		},
		{
			"Sid": "DenyUnEncryptedObjectUploads",
			"Effect": "Deny",
			"Principal": "*",
			"Action": "s3:PutObject",
			"Resource": "arn:aws:s3:::mattua/encrypted/*",
			"Condition": {
				"Null": {
					"s3:x-amz-server-side-encryption": "true"
				}
			}
		}
	]
}

         */

        {
             putRequest1 = new PutObjectRequest("mattua", "encrypted/"+fileName + ".encrypted." + System.currentTimeMillis(), file);

             objectMetadata1 = new ObjectMetadata();
            objectMetadata1.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
            putRequest1.setMetadata(objectMetadata1);

             response1 = s3Client.putObject(putRequest1);
            System.out.println("Uploaded object encryption status is " +
                    response1.getSSEAlgorithm());

            putRequest1 = new PutObjectRequest("mattua", "encrypted/"+fileName + ".plain." + System.currentTimeMillis(), file);

             // without encryption
           try {
               response1 = s3Client.putObject(putRequest1);
           } catch (Exception e){

           }
            System.out.println("Uploaded object encryption status is " +
                    response1.getSSEAlgorithm());



        }

        putRequest1 = new PutObjectRequest("mattua", "boom/"+fileName + "." + System.currentTimeMillis(), file);

        response1 = s3Client.putObject(putRequest1);
        System.out.println("Uploaded object encryption status is " +
                response1.getSSEAlgorithm());

        // without encryption
        response1 = s3Client.putObject(putRequest1);
        System.out.println("Uploaded object encryption status is " +
                response1.getSSEAlgorithm());

        /* The folder level button is purely to encrypt the selected items - it is not a persistant folder setting */




    }


    public static void run() throws Exception {

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(Credentials.access_key_id, Credentials.secret_access_key);
        AmazonS3 s3Client = new AmazonS3Client(awsCreds);

        String newBucketName = "mattua" + System.currentTimeMillis();
        s3Client.createBucket(newBucketName);

        String policy = readFileFromResources("encrypted-folder-policy.txt").replace("bucketname",newBucketName);

        System.out.println(policy);
        s3Client.setBucketPolicy(newBucketName, policy);

        final String fileName = "sometext.txt";

        File file = new File(S3ServerSideSDKEncryption.class.getResource(fileName).toURI());

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

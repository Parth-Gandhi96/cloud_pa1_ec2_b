package org.parth.textReck;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.TextDetection;
import java.util.List;
import java.util.Map.Entry;

public class textReck 
{
    public static void callDetection(String photo, String bucket){
	    photo += ".jpg";
		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
		    DetectTextRequest request = new DetectTextRequest()
			      .withImage(new Image()
			      .withS3Object(new S3Object()
			      .withName(photo)
			      .withBucket(bucket)));

              try {
                 DetectTextResult result = rekognitionClient.detectText(request);
                 List<TextDetection> textDetections = result.getTextDetections();	

                 for (TextDetection text: textDetections) {
			 System.out.print(photo + " " );
                         System.out.print(text.getDetectedText());
                         //System.out.println("Confidence: " + text.getConfidence().toString());
                         //System.out.println("Id : " + text.getId());
                         //System.out.println("Parent Id: " + text.getParentId());
                         //System.out.println("Type: " + text.getType());
                         System.out.println();
			 break;
                 }
                } catch(AmazonRekognitionException e) {
                 e.printStackTrace();
              }	    
    }
    public static void main( String[] args )
    {
	    AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
	      String bucket = "njit-cs-643";
	      String myQueueUrl = "https://sqs.us-east-1.amazonaws.com/700559207820/cloudpro1";
		final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
	        final ReceiveMessageRequest receiveMessageRequest =
                    new ReceiveMessageRequest(myQueueUrl).withMaxNumberOfMessages(10);
		    // Uncomment the following to provide the ReceiveRequestDeduplicationId
		    // receiveMessageRequest.setReceiveRequestAttemptId("1");
		
		while(true){
			final List<Message> messages = sqs.receiveMessage(receiveMessageRequest)
				    .getMessages();
			int flag = 0;
			for (final Message message : messages) {
				/*System.out.println("Message");
				System.out.println("  MessageId:     "
					+ message.getMessageId());
				System.out.println("  Body:          "
					+ message.getBody());*/
				boolean temp = false;
				if(message.getBody().equals("-1")){
					//System.out.println("-1 came in the queue, all messages in queue are processed!");
					sqs.deleteMessage(myQueueUrl, message.getReceiptHandle());
					flag += 1;
					temp = true;
					//break;
				}
				if (!temp){
					callDetection(message.getBody(), bucket);
					sqs.deleteMessage(myQueueUrl, message.getReceiptHandle());
				}
			}
			if(flag == 3)
				break;	
		}
    }
}

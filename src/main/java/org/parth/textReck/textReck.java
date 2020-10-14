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
import java.util.*;
import java.util.Map.Entry;
import java.io.*;

public class textReck 
{
    	public static void storeInFile(List<String> data){
		try {
      			FileWriter myWriter = new FileWriter("filename.txt");
      			String toStore = "";
			for(String d:data)
				toStore = (toStore + d + "\n");
			myWriter.write(toStore);
      			myWriter.close();
    		} catch (IOException e) {
      			System.out.println("An error occurred while storing the file.");
      			e.printStackTrace();
    		}
	}
	public static String callDetection(String photo, String bucket){
	    String pn = photo;
		photo += ".jpg";
	    String ans = "";
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
                         ans = pn+" "+text.getDetectedText();
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
	      return ans;
    }
    public static void main( String[] args )
    {
	    AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
	      String bucket = "njit-cs-643";
	      String myQueueUrl = "https://sqs.us-east-1.amazonaws.com/728930872376/pa1.fifo";
		AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
	        ReceiveMessageRequest receiveMessageRequest =
                    new ReceiveMessageRequest(myQueueUrl).withMaxNumberOfMessages(10);
		    // Uncomment the following to provide the ReceiveRequestDeduplicationId
		    // receiveMessageRequest.setReceiveRequestAttemptId("1");
		
		List<String> answer = new ArrayList<String>();
		boolean flag = true;
		while(flag){
			List<Message> messages = sqs.receiveMessage(receiveMessageRequest)
				    .getMessages();
			
			for (Message message : messages) {
				System.out.println("Message");
				System.out.println("  MessageId:     "
					+ message.getMessageId());
				System.out.println("  Body:          "
					+ message.getBody());
			
				if(message.getBody().matches("-1")){
					System.out.println("-1 came in the queue, all messages in queue are processed!");
					sqs.deleteMessage(myQueueUrl, message.getReceiptHandle());
					flag = false;
					break;
				}
			
					String temp = callDetection(message.getBody(), bucket);
					System.out.println("After text detection:"+temp);
					sqs.deleteMessage(myQueueUrl, message.getReceiptHandle());
				answer.add(temp);
			}	
		}
		System.out.println(answer.size());
		storeInFile(answer);
    }
}

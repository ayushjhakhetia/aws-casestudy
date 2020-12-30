package com.impetus.FraudDetection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.impetus.model.UserReport;
import com.impetus.utility.FraudDetectionUtility;

import static com.impetus.constants.FraudDectectionConstants.LOGOUT_SERVICE_MSG;
import static com.impetus.constants.FraudDectectionConstants.LOGOUT_SUCCESS_MSG;

public class S3LogReader implements RequestHandler<S3Event, String> {

	Map<String, UserReport> report = new HashMap<>();
	BufferedReader reader = null;
	List<String> sessionLogs = new ArrayList<>();
	private static AmazonSNSClient snsClient = null;
	static final Logger logger = LoggerFactory.getLogger(S3LogReader.class);

	@Override
	public String handleRequest(S3Event input, Context context) {
		System.out.println("Input event: " + input.toJson());

		try {
			S3EventNotificationRecord record = input.getRecords().get(0);

			String srcBucket = record.getS3().getBucket().getName();

			// Object key may have spaces or unicode non-ASCII characters.
			String srcKey = record.getS3().getObject().getUrlDecodedKey();

			/*
			 * System.out.println("Source Bucket is: " + srcBucket);
			 * System.out.println("Source Key is: " + srcKey);
			 */
			System.out.println("Source Bucket is: " + srcBucket);
			System.out.println("Source Key is: " + srcKey);

			AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
			S3Object s3Object = s3Client.getObject(new GetObjectRequest(srcBucket, srcKey));
			InputStream objectData = s3Object.getObjectContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(objectData));
			logger.info("Reading from s3 file");
			String line = reader.readLine();
			System.out.println("line is: " + line);
			while (line != null) {
				System.out.println("Not null line is: " + line);
				if (line.contains("Login-Attempted-Failed")) {
					FraudDetectionUtility.analysisForFailedTransaction(report, line);
					System.out.println("There is a failed transaction");
				} else {
					System.out.println("inside else");
					sessionLogs.add(line);
					if (line.contains(LOGOUT_SERVICE_MSG) && line.contains(LOGOUT_SUCCESS_MSG)) {
						System.out.println("contains LOGOUT_SERVICE_MSG ");
						// Perform analysis on session data captured
						FraudDetectionUtility.analysisForNoOfTransactionInSession(sessionLogs, report);
						FraudDetectionUtility.analysisForNoOfDifferentLocations(sessionLogs, report);
						sessionLogs.clear();
					}
				}
				line = reader.readLine();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			System.err.println("Unable to generate report " + exception.toString());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Unable to close connectin" + e);
				}
			}
		}
		try {
			System.out.println("Printing Report and sending notification");
			Map<String, UserReport> finalReport = FraudDetectionUtility.prepareFinalReport(report);
			FraudDetectionUtility.convertToJsonAndPushReportsToS3(finalReport);
		} catch (Exception e) {
			System.out.println("Error....." + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

}

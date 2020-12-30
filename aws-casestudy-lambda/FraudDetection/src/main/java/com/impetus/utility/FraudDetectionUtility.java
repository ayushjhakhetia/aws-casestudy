package com.impetus.utility;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.impetus.constants.*;
import com.impetus.model.SessionTransaction;
import com.impetus.model.UserReport;
import com.impetus.database.DatabaseOperations;

import static com.impetus.constants.FraudDectectionConstants.AMOUNT_DEBITED_MSG;
import static com.impetus.constants.FraudDectectionConstants.RECORD_DELIMITER;
import static com.impetus.constants.FraudDectectionConstants.REPORT_BUCKET;
import static com.impetus.constants.FraudDectectionConstants.REPORT_FILE_KEY;
import static com.impetus.constants.FraudDectectionConstants.TOPIC_ARN;
import static com.impetus.constants.FraudDectectionConstants.EMAIL_SUBJECT;
import static com.impetus.constants.FraudDectectionConstants.TABLE_NAME;
import static com.impetus.constants.FraudDectectionConstants.THREASHOLD_FOR_FRAUD;

/**
 * @author CEP-A41
 *
 */
public class FraudDetectionUtility {

	private static Logger log = Logger.getLogger(FraudDetectionUtility.class.getName());
	private static FileWriter file;
	static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd");

	private static AmazonSNSClient snsClient = null;
	private static AmazonS3 s3Client = null;

	/**
	 * @param sessionLogs
	 * @param report
	 * 
	 *                    Update report with Different Location
	 */
	public static void analysisForNoOfDifferentLocations(List<String> sessionLogs, Map<String, UserReport> report) {
		String[] tokens = sessionLogs.get(0).split(RECORD_DELIMITER);
		String userId = tokens[3];
		String location = tokens[5];
		System.out.println("analysisForNoOfDifferentLocations");
		UserReport userReport = report.get(userId);
		if (userReport == null) {
			populateMapForDifferentLocations(report, userId, location);
		} else {
			Set<String> differentLocations = userReport.getDifferentLocations();
			differentLocations.add(location);
			userReport.setCountOfDifferentLocation(differentLocations.size());
			report.put(userId, userReport);
		}

	}

	/**
	 * @param report
	 * @param line
	 * 
	 *               Update report with Failed Transactions
	 * 
	 */
	public static void analysisForFailedTransaction(Map<String, UserReport> report, String line) {
		// Its failed transaction
		String[] tokens = line.split(RECORD_DELIMITER);
		System.out.println("Found tokens:");
		for (String token : tokens) {
			System.out.println(token);
		}
		String userId = tokens[2];
		String location = tokens[4];

		System.out.println("analysisForFailedTransaction");
		if (report.get(userId) == null) {
			populateMapForFailedTransaction(report, userId, location);
		} else {
			// get existing object from
			UserReport userReport = report.get(userId);
			int countOfFailedTransaction = userReport.getCountOfFailedTransaction();
			int updatedCountOfFailedTransaction = countOfFailedTransaction + 1;

			// update locations
			Set<String> differentLocations = userReport.getDifferentLocations();
			differentLocations.add(location);
			int updatedCountOfDifferentLocation = differentLocations.size();

			userReport.setCountOfDifferentLocation(updatedCountOfDifferentLocation);
			userReport.setDifferentLocations(differentLocations);
			userReport.setCountOfFailedTransaction(updatedCountOfFailedTransaction);
			report.put(userId, userReport);
		}
	}

	/**
	 * @param sessionLogs
	 * @param report
	 * 
	 *                    Update report with Session Transaction
	 * 
	 */
	public static void analysisForNoOfTransactionInSession(List<String> sessionLogs, Map<String, UserReport> report) {
		String[] tokens = sessionLogs.get(0).split(RECORD_DELIMITER);
		String sessionId = tokens[2];
		String userId = tokens[3];
		UserReport userReport = report.get(userId);
		System.out.println("analysisForNoOfTransactionInSession");
		Long count = sessionLogs.stream().filter(log -> log.contains(AMOUNT_DEBITED_MSG)).count();
		if (userReport == null) {
			populateMapForSessionTransaction(report, userId, sessionId, count);
		} else {
			List<SessionTransaction> listSessionTransaction = userReport.getSessionTransaction();
			SessionTransaction sessionTransaction = new SessionTransaction();
			sessionTransaction.setCountOfTransactionInSession(count.intValue());
			sessionTransaction.setSessionId(sessionId);
			listSessionTransaction.add(sessionTransaction);
			userReport.setSessionTransaction(listSessionTransaction);
			report.put(userId, userReport);

		}
	}

	/**
	 * @param report
	 * @param userId
	 * @param location
	 * 
	 *                 Initiate the User Report with Different Location
	 * 
	 */
	private static void populateMapForDifferentLocations(Map<String, UserReport> report, String userId,
			String location) {
		UserReport userReport = new UserReport();
		int countOfFailedTransaction = 0;
		Set<String> differentLocations = new HashSet<>();
		differentLocations.add(location);
		System.out.println("populateMapForDifferentLocations");
		int countOfDifferentLocation = differentLocations.size();
		List<SessionTransaction> listofSessionTransaction = new ArrayList<>();

		userReport.setSessionTransaction(listofSessionTransaction);
		userReport.setUserId(userId);
		userReport.setCountOfDifferentLocation(countOfDifferentLocation);
		userReport.setDifferentLocations(differentLocations);
		userReport.setCountOfFailedTransaction(countOfFailedTransaction);
		report.put(userId, userReport);
	}

	/**
	 * @param report
	 * @param userId
	 * @param location
	 * 
	 *                 Initiate the User Report with Failed Transactions
	 */
	private static void populateMapForFailedTransaction(Map<String, UserReport> report, String userId,
			String location) {
		// populate UserReport for failed transaction
		UserReport userReport = new UserReport();
		System.out.println("populateMapForFailedTransaction");
		int countOfFailedTransaction = 1;// this is first failed Transaction;
		int countOfDifferentLocation = 1; // this is first failed Transaction
		Set<String> differentLocations = new HashSet<>();
		differentLocations.add(location);
		List<SessionTransaction> listofSessionTransaction = new ArrayList<>();

		userReport.setSessionTransaction(listofSessionTransaction);
		userReport.setUserId(userId);
		userReport.setCountOfDifferentLocation(countOfDifferentLocation);
		userReport.setDifferentLocations(differentLocations);
		userReport.setCountOfFailedTransaction(countOfFailedTransaction);
		report.put(userId, userReport);
	}

	/**
	 * @param report
	 * @param userId
	 * @param sessionId
	 * @param count
	 * 
	 *                  Initiate the User Report with Session Transactions
	 * 
	 */
	private static void populateMapForSessionTransaction(Map<String, UserReport> report, String userId,
			String sessionId, Long count) {
		UserReport userReport = new UserReport();
		int countOfFailedTransaction = 0;
		int countOfDifferentLocation = 0;
		Set<String> differentLocations = new HashSet<>();
		System.out.println("populateMapForSessionTransaction");
		List<SessionTransaction> listofSessionTransaction = new ArrayList<>();
		SessionTransaction sessionTransaction = new SessionTransaction();
		sessionTransaction.setCountOfTransactionInSession(count.intValue());
		sessionTransaction.setSessionId(sessionId);
		listofSessionTransaction.add(sessionTransaction);

		userReport.setSessionTransaction(listofSessionTransaction);
		userReport.setUserId(userId);
		userReport.setCountOfDifferentLocation(countOfDifferentLocation);
		userReport.setDifferentLocations(differentLocations);
		userReport.setCountOfFailedTransaction(countOfFailedTransaction);
		report.put(userId, userReport);
	}

	/**
	 * @param report
	 * 
	 *               Convert to JSON and push data to S2 bucket
	 * 
	 */
	public static void convertToJsonAndPushReportsToS3(Map<String, UserReport> report) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			LocalDateTime now = LocalDateTime.now();
			s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
			String fileName = "FraudDetection_" + dtf.format(now) + ".json";
			String jsonString = mapper.writeValueAsString(report);
//			log.info(jsonString);
			System.out.println("convertToJsonAndPushReportsToS3, json is: " + jsonString);
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType("application/json");
			metadata.addUserMetadata("title", "someTitle");

			s3Client.putObject(REPORT_BUCKET, REPORT_FILE_KEY + "/" + fileName,
					new ByteArrayInputStream(jsonString.getBytes()), metadata);
			String s3URL = "https://" + REPORT_BUCKET + ".s3-ap-south-1.amazonaws.com/" + REPORT_FILE_KEY + "/"
					+ fileName;
			System.out.println("s3URL is: "+s3URL);
			DatabaseOperations.insert(fileName, s3URL, TABLE_NAME);
			sendSNSNotification(s3URL);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
//			log.error("Unable to convert to JSON, ", e);
			e.printStackTrace();
		} catch (Exception e) {
//			log.error("Exception occured while converting to json", e);
			System.out.println("Found exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * @param reportS3LogReader
	 * 
	 *               Send notification when fraud is detected
	 * 
	 */
	public static Map<String,UserReport> prepareFinalReport(Map<String, UserReport> report) {
		Map<String, UserReport> finalReport = new HashMap<>();
		System.out.println("sendNotification");
		report.entrySet().forEach(entry -> {
			UserReport userReport = entry.getValue();
			if (userReport.getCountOfDifferentLocation() >= THREASHOLD_FOR_FRAUD) {
				finalReport.put(entry.getKey(),userReport);
			}
			if (userReport.getCountOfFailedTransaction() >= THREASHOLD_FOR_FRAUD) {
				finalReport.put(entry.getKey(),userReport);
			}

			userReport.getSessionTransaction().forEach(trans -> {
				if (trans.getCountOfTransactionInSession() >= THREASHOLD_FOR_FRAUD) {
					finalReport.put(entry.getKey(),userReport);
				}
			});
		});
		return finalReport;
	}

	private static void sendSNSNotification(String s3URL) {
		System.out.println("sendSNSNotification");
		try {
			snsClient = (AmazonSNSClient) AmazonSNSClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
			String EMAIL_BODY = "Hi,\n\nThere is a fraud detectated. Please find the details from the below URL\n\n" + s3URL + "\n\nThanks.";
			PublishRequest publishRequest = new PublishRequest(TOPIC_ARN, EMAIL_BODY, EMAIL_SUBJECT);
			snsClient.publish(publishRequest);
		} catch (Exception e) {
			System.out.println("Exception while sending SNS: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
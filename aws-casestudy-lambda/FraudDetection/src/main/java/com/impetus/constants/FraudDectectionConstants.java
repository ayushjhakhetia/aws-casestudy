package com.impetus.constants;

/**
 * @author CEP-A41
 *
 */
public class FraudDectectionConstants {

	public static final String RECORD_DELIMITER = "  ";
	public static final String AMOUNT_DEBITED_MSG = "Amount Debited";
	public static final String LOGOUT_SUCCESS_MSG = "Logout Successfully";
	public static final String LOGOUT_SERVICE_MSG = "LogoutService";
	public static final String LOGS_BUCKET = "a41-frauddetection-s3-logs";
	public static final String REPORT_BUCKET = "a41-frauddetection-s3-reports";
	public static final String REPORT_FILE_KEY = "Reports";
	public static final String TOPIC_ARN = "arn:aws:sns:ap-south-1:852943274432:A41-FraudDetection";
	public static final String EMAIL_SUBJECT = "Fraud Detection Report!";
	public static final String TABLE_NAME = "fraud_analyis_report";
	public static final int THREASHOLD_FOR_FRAUD = 2;

}
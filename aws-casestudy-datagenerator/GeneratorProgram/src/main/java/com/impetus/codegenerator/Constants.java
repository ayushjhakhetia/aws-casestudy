package com.impetus.codegenerator;

/**
 * @author akhilesh.joshi
 *
 * Constants for Click Stream Data Generation
 * 
 */
public class Constants {
	public static final String PREFIX_SESSION_CLICK_SHOP = "Session_id_";
	public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String CREDIT_CARD = "Credit Card";
	public static final String CASH_ON_DELIVERY = "Cash On Delivery";
	public static final String ONLINE_BANKING = "Online Banking";
	public static String[] paymentAllowed = new String[] { ONLINE_BANKING, CASH_ON_DELIVERY, CREDIT_CARD };
	public static final String URL_HTTP_WWW_SHOP_COM_LOGGEDOUT = "\",\"url\":\"http://www.shop.com/loggedout\",";
	public static final String URL_HTTP_WWW_SHOP_COM_PURCHASED_ITEM = "\",\"url\":\"http://www.shop.com/purchased/item?";
	public static final String URL_HTTP_WWW_SHOP_COM_ADDEDTOCART_ITEM = "\",\"url\":\"http://www.shop.com/addedtocart/item?";
	public static final String NEW_LINE = "\n";
	public static final String LOG_DATE = "\"logDate\":\"";
	public static final String LOG_TIME = "\"logTime\":\"";
	public static final String URL_HTTP_WWW_SHOP_COM_HOME_PAGE = "\",\"url\":\"http://www.shop.com/homePage\",";
	public static final String SESSION_ID = "\",\"sessionId\":\"";
	public static final String LOCATION = "\",\"location\":\"";
	public static final String USER = "{\"userID\":\"";
	public static final String FILE_EXTENSION_FORMAT = "yyyy_MM_dd_HH_mm'.txt'";
	public static final String URL_HTTP_WWW_SHOP_COM_VIEW_ITEM = "\",\"url\":\"http://www.shop.com/viewItem?";
	public static final String TRANSACTION_SERVICE = "  TransactionService  ";
}

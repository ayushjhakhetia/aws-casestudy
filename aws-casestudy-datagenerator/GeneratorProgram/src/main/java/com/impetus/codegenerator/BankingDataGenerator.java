package com.impetus.codegenerator;

import static com.impetus.codegenerator.Constants.DATE_FORMAT;
import static com.impetus.codegenerator.Constants.FILE_EXTENSION_FORMAT;
import static com.impetus.codegenerator.Constants.NEW_LINE;
import static com.impetus.codegenerator.Constants.PREFIX_SESSION_CLICK_SHOP;
import static com.impetus.codegenerator.Constants.TIMESTAMP_FORMAT;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.github.javafaker.Faker;

public class BankingDataGenerator {

    public BankingDataGenerator() {
        super();
        c.setTime(currentDate);
    }

    static Map<Integer, String> product = new HashMap<>();
    static Map<Integer, String> users = new HashMap<>();
    static final Date date = new Date();
    static SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
    static String todaydate = formatter.format(date);
    static DateFormat timeFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
    static Long logTime = date.getTime();
    static String sessioncont = PREFIX_SESSION_CLICK_SHOP + logTime;
    private static List<String> states = new ArrayList<>();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Date currentDate = new Date();
    private static Calendar c = Calendar.getInstance();
    private static Random rand = new Random();

    public static void main(String[] args) throws IOException {
        Faker faker = new Faker(Locale.UK);
        populateUserIDOfWebsite(users);
        String format = new SimpleDateFormat(FILE_EXTENSION_FORMAT).format(new Date());
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);
        if(args.length>0)
             logNavigationFlow(Integer.parseInt(args[1]), args[0] + "FakerLogs_" + format, faker);
        else 
            logNavigationFlow(Integer.parseInt("100"), "/home/ec2-user/logs/FakerLogs" + "_" + format, faker);
    }

    /** @param users
     * @param faker
     *            Will populate the user id of the Website. To get the user we use JavaFaker API which will generate the users for the Website. */
    private static void populateUserIDOfWebsite(Map<Integer, String> users) {
        int numberOfUsers = 0;
        int uidNumber = 23948;
        while (numberOfUsers != 70) {
            uidNumber++;
            users.put(numberOfUsers, "UID100" + uidNumber);
            numberOfUsers++;
        }
    }

    /** @param faker
     * @return list of states Will populate the list of sates users belongs to. To get the states we use JavaFaker API */
    private static List<String> populateStates(Faker faker) {
        int ii = 0;
        while (ii != 50) {
            states.add(faker.address().state());
            ii++;
        }
        return states;
    }

    /** @param numberOfUsers
     * @param fileName
     * @param faker
     * @throws IOException
     *             This method defines the navigation flow of a user. */
    private static void logNavigationFlow(int numberOfUsers, String fileName, Faker faker) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            if (file.createNewFile())
                System.out.println("Log File Created Successfully");
            else
                System.out.println("Can't create new file");
        }
        try (FileWriter out = new FileWriter(file); BufferedWriter bufferedWriter = new BufferedWriter(out);) {
            List<String> states = populateStates(faker);
            for (int i = 0; i < numberOfUsers; i++) {
                String userlocation = states.get(getRandomint() % (states.size()));
                String user = users.get(getRandomint(70));
                String sessionid = sessioncont + i;
                StringBuilder navigationFlow = new StringBuilder();
                bufferedWriter.write(startNavigation(navigationFlow, user, userlocation, sessionid) + "");
            }
        }
    }

    /** @param navigationFlow
     * @param user
     * @param userlocation
     * @param sessionid
     * @return Navigation flow of User */
    private static StringBuilder startNavigation(StringBuilder navigationFlow, String user, String userlocation, String sessionid) {
        int randomValue = getRandomint(100);
        if (randomValue < 90) {
            appendLoginService(navigationFlow, user, userlocation, sessionid);
            navigateToViewProduct(navigationFlow, user, userlocation, sessionid);
            navigateToLogout(navigationFlow, user, userlocation, sessionid);
        } else {
            appendLoginServiceFailedAttempt(navigationFlow, user, userlocation, sessionid);
        }
        return navigationFlow;
    }

    /** @param navigationFlow
     * @param user
     * @param userlocation
     * @param sessionid
     * @return View Instance of Navigation */
    private static StringBuilder navigateToViewProduct(StringBuilder navigationFlow, String user, String userlocation, String sessionid) {
        int randomValue = getRandomint(90);
        if (randomValue < 25) {
            // View Balance
            appendViewBalance(navigationFlow, user, userlocation, sessionid);
            navigateToViewProduct(navigationFlow, user, userlocation, sessionid);
        } else if (randomValue < 45) {
            // Amount Debited
            appendAmountDebited(navigationFlow, user, userlocation, sessionid);
            navigateToViewProduct(navigationFlow, user, userlocation, sessionid);
        } else if (randomValue < 65) {
            // Amount Credited
            appendAmountCredited(navigationFlow, user, userlocation, sessionid);
            navigateToViewProduct(navigationFlow, user, userlocation, sessionid);
        }
        return navigationFlow;
    }

    /** @return if page is refreshed */
    private static boolean isF5Pressed() {
        return getRandomint() % 25 > 18;
    }

    /** @param sb
     * @param user
     * @param userlocation
     * @param sessionid
     * @param item
     * @return Add to cart instance of navigation */
    private static StringBuilder navigateToAmountCredited(StringBuilder sb, String user, String userlocation, String sessionid) {
        appendAmountCredited(sb, user, userlocation, sessionid);
        if (isF5Pressed()) {
            appendAmountCredited(sb, user, userlocation, sessionid);
        }
        return navigateToAmountDebited(sb, user, userlocation, sessionid);
    }

    /** @param sb
     * @param user
     * @param userlocation
     * @param sessionid
     * @param item
     * @return Transaction Page /Add to Cart Instacne of navigation */
    private static StringBuilder navigateToAmountDebited(StringBuilder sb, String user, String userlocation, String sessionid) {
        if (getRandomint() % 11 == 1) {
            return navigateToAmountCredited(sb, user, userlocation, sessionid);
        }
        appendAmountDebited(sb, user, userlocation, sessionid);
        return sb;
    }

    /** @param navigationFlow;
     * @param user
     * @param userlocation
     * @param sessionid
     *            Completes the log out flow of user */
    private static StringBuilder navigateToLogout(StringBuilder navigationFlow, String user, String userlocation, String sessionid) {
        appnedLogoutService(navigationFlow, user, userlocation, sessionid);
        return navigationFlow;
    }

    /** Append login service failed attempt.
     *
     * @param navigationFlow
     *            the navigation flow
     * @param user
     *            the user
     * @param userlocation
     *            the userlocation
     * @param sessionid
     *            the sessionid */
    private static void appendLoginServiceFailedAttempt(StringBuilder navigationFlow, String user, String userlocation, String sessionid) {
        navigationFlow.append(generateCalendarDate() + "  LoginService  " + user + "  Login-Attempted-Failed  " + userlocation);
        navigationFlow.append(NEW_LINE);
        int num = getRandomint(10);
        if (num > 4) {
            if (num > 7) {
                userlocation = states.get(getRandomint() % (states.size()));
            }
            appendLoginServiceFailedAttempt(navigationFlow, user, userlocation, sessionid);
        }
    }

    /** @param sb
     * @param user
     * @param userlocation
     * @param sessionid
     *            Appends Home page /login instance */
    private static void appendLoginService(StringBuilder sb, String user, String userlocation, String sessionid) {
        sb.append(generateCalendarDate() + "  LoginService  " + sessionid + "  " + user + "  Login Successfully  " + userlocation);
        sb.append(NEW_LINE);
    }

    /** @param navigationFlow
     * @param user
     * @param userlocation
     * @param sessionid
     * @param item
     *            Appends View Item instance */
    private static void appendViewBalance(StringBuilder navigationFlow, String user, String userlocation, String sessionid) {
        navigationFlow.append(generateCalendarDate() + Constants.TRANSACTION_SERVICE + sessionid + "  " + user + "  Balance Inquiry  " + userlocation);
        navigationFlow.append(NEW_LINE);
    }

    /** @param navigationFlow
     * @param user
     * @param userlocation
     * @param sessionid
     * @param item
     *            Appends add to cart instance */
    private static void appendAmountCredited(StringBuilder navigationFlow, String user, String userlocation, String sessionid) {
        navigationFlow.append(generateCalendarDate() + Constants.TRANSACTION_SERVICE + sessionid + "  " + user + "  Amount Credited  " + userlocation);
        navigationFlow.append(NEW_LINE);
    }

    /** @param navigationFlow
     * @param user
     * @param userlocation
     * @param sessionid
     * @param item
     *            Appends Purchased instance */
    private static void appendAmountDebited(StringBuilder navigationFlow, String user, String userlocation, String sessionid) {
        navigationFlow.append(generateCalendarDate() + Constants.TRANSACTION_SERVICE + sessionid + "  " + user + "  Amount Debited  " + userlocation);
        navigationFlow.append(NEW_LINE);
    }

    /** @param navigationFlow
     * @param user
     * @param userlocation
     * @param sessionid
     *            Appends Logout Instance */
    private static void appnedLogoutService(StringBuilder navigationFlow, String user, String userlocation, String sessionid) {
        navigationFlow.append(generateCalendarDate() + "  LogoutService  " + sessionid + "  " + user + "  Logout Successfully  " + userlocation);
        navigationFlow.append(NEW_LINE);
    }

    /** @return Random number */
    private static int getRandomint() {
        int upperbound = 255;
        return rand.nextInt(upperbound);
    }

    /** Gets the randomint.
     *
     * @param upperbound
     *            the upperbound
     * @return the randomint */
    private static int getRandomint(int upperbound) {
        return rand.nextInt(upperbound);
    }

    /** Generate calendar date.
     *
     * @return the string */
    private static String generateCalendarDate() {
        int seconds = getRandomint(25);
        c.add(Calendar.SECOND, seconds);
        // Convert calendar back to Date
        Date currentDatePlusOne = c.getTime();
        return dateFormat.format(currentDatePlusOne);
    }
}

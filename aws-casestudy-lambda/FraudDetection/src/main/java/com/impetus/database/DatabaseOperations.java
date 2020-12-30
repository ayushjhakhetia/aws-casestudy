package com.impetus.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseOperations {

//	static final Logger logger = LoggerFactory.getLogger(CreateConnection.class);

	public static void insert(String reportName, String location, String tableName) {
		try {
//			logger.info("Insert report information to RDS");
			System.out.println("Insert report information to RDS");
			Connection con = CreateConnection.getRemoteConnection();
			System.out.println("Connection created: "+con);
			Statement stmt = (Statement) con.createStatement();
			System.out.println("stmt created: "+stmt);
			String query1 = "INSERT INTO " + tableName + " (Name, Location) VALUES ('" + reportName + "', '" + location
					+ "')";
			System.out.println("executing query: "+query1);
			stmt.executeUpdate(query1);
			System.out.println("Record is inserted in the table successfully..................");
		} catch (SQLException e) {
			System.out.println("got error: "+e.getMessage());			
			e.printStackTrace();
		}
	}
	public String select(String fetchColumn, String identifierColumn, String tableName) {
		String emailID="";
		try {
//			logger.info("Insert report information to RDS");
			System.out.println("select email id from RDS");
			Connection con = CreateConnection.getRemoteConnection();
			System.out.println("Connection created: "+con);
		    String query = "SELECT "+fetchColumn+" FROM "+tableName+" where UID='"+identifierColumn+"'";
			Statement stmt = (Statement) con.createStatement();
			System.out.println("stmt created: "+stmt);
			System.out.println("executing query: "+query);
		    ResultSet rs = stmt.executeQuery(query);

		      while(rs.next()){
		         //Retrieve by column name		       
		         emailID = rs.getString(fetchColumn);

		         //Display values
		         System.out.print("emailID: " + emailID);
		      }
		      rs.close();
		      return emailID;
		} catch (SQLException e) {
			System.out.println("got error: "+e.getMessage());			
			e.printStackTrace();
			return null;
		}catch (Exception ex) {
			System.out.println("got error: "+ex.getMessage());			
			ex.printStackTrace();
			return null;
		}
	}
}

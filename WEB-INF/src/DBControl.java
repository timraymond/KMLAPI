
import java.sql.*;
import org.postgresql.*;
import org.postgresql.Driver;
import org.postgis.*;

public class DBControl
{
	java.sql.Connection con;
	public DBControl() throws Exception
	{
		Class.forName("org.postgresql.Driver");
		 String url = "jdbc:postgresql://localhost:5432/kmlapi";
		 //"jdbc:mysql://localhost:3306/mysql";
		
		 //Get a connection to the database for a
		 // user named root with a blank password.
		 // This user is the default administrator
		 // having full privileges to do anything.
		 con = DriverManager.getConnection(url,"postgres", "admin");
		
		 //Display URL and connection information
		 /*System.out.println("Connected to Database.\n\tURL: " + url);
		 System.out.println("\tConnection: " + con);*/
		
		 //Get a Statement object
		 //stmt = con.createStatement();
		
		 //con.close();
	 }//end main
	 
	 public void sendCommand(String in)
	 {
		 Statement stmt;
		 String z;
		 try{
			 stmt = con.createStatement();
			 stmt.executeUpdate(in);
		 }
		 catch(Exception e){e.printStackTrace();}
	 }
	 
	 public ResultSet QueryDB(String command)
	 {
		 Statement stmt;
		 
		 try{
			 stmt = con.createStatement();
			 ResultSet rout=stmt.executeQuery(command);
			 return rout;
		 }
		 catch(Exception e){e.printStackTrace();}
		 return null;
	 }
}
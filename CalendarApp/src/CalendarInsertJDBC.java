import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author karlamiletti
 */
public class CalendarInsertJDBC {
    
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/test";
    // Database credentials
    static final String USER = "sa";
    static final String PASS = "";
    
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try{
            // register JDBC driver
            Class.forName(JDBC_DRIVER);
            //open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            System.out.println("Connected database successfully...");
            //execute a query
            stmt = conn.createStatement();
            String sql = "INSERT INTO Events " +
            "VALUES (4, 'Karate Class', '2018-12-01 19:00:00')";
            stmt.executeUpdate(sql);
//            String sql = "INSERT INTO Registration " +
//            "VALUES (100, 'Zara', 'Ali', 18)";
//            stmt.executeUpdate(sql);
//            sql = "INSERT INTO Registration " +
//            "VALUES (101, 'Mahnaz', 'Fatma', 25)";
//            stmt.executeUpdate(sql);
//            sql = "INSERT INTO Registration " +
//            "VALUES (102, 'Zaid', 'Khan', 30)";
//            stmt.executeUpdate(sql);
//            sql = "INSERT INTO Registration " +
//            "VALUES(103, 'Sumit', 'Mittal', 28)";
//            stmt.executeUpdate(sql);
            System.out.println("Inserted records into the table...");

            //STEP 4: Clean-up environment
            stmt.close();
            conn.close();
        }catch(SQLException se){
        //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){//Handle errors for Class.forName
            e.printStackTrace();
        }
        finally{//finally block used to close resources
            try{
                if(stmt!=null)
                    stmt.close();
            }
            catch(SQLException se2){
            }// nothing to do

            try{
                if(conn!=null)
                    conn.close();
            }
            catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try
            System.out.println("Goodbye!");
       }
    
}

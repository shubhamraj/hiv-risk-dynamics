package javasql;
// JDBC|Test – complete code
public class JDBCTest {

   public static void main(String args[]){

      RunDB runDB = new RunDB();

      try{
         runDB.loadDriver();
         runDB.makeConnection();
         runDB.buildStatement();
         runDB.executeQuery();
//         runDB.createTable();
//         runDB.insertRecord("1", "John");
//         runDB.insertRecord("2", "Kim");
         runDB.updateRecord();
         runDB.commitChanges();
         runDB.connection.close();
         
      }catch(Exception e){
         e.printStackTrace();
      }

   }
}

package javasql;
//RunDB
import java.sql.*;

public class RunDB {

   Connection connection;
   public Connection getConnection() {
	return connection;
}

public void setConnection(Connection connection) {
	this.connection = connection;
}

public Statement getStatement() {
	return statement;
}

public void setStatement(Statement statement) {
	this.statement = statement;
}

Statement statement;

   public void loadDriver() throws ClassNotFoundException{
      Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
   }

   public void makeConnection() throws SQLException {
      connection=DriverManager.getConnection("jdbc:odbc:purchaseOrder");
   }

   public void buildStatement() throws SQLException {
      statement = connection.createStatement();
   }

   public void executeQuery() throws SQLException {
      boolean foundResults =
         statement.execute("SELECT * FROM Table1");
      if(foundResults){
         ResultSet set = statement.getResultSet();
         if(set!=null) displayResults(set);
      }else {
         connection.close();
      }
   }

   void displayResults(ResultSet rs) throws SQLException {
      ResultSetMetaData metaData = rs.getMetaData();
      int columns=metaData.getColumnCount();
      String text="";

      while(rs.next()){
         for(int i=1;i<=columns;++i) {
            text+="<"+metaData.getColumnName(i)+">";
            text+=rs.getString(i);
             text+="</"+metaData.getColumnName(i)+">";
             text+="n";
         }
         text+="n";
      }

      System.out.println(text);
   }
/*   public void createStatement(){

	   try {
	      DBStatement = DBConnection.createStatement();
	      System.out.println ("+++++++++++++++++++++");
	      System.out.println ("+ statement created +");
	      System.out.println ("+++++++++++++++++++++");
	   } catch (Exception excep) {
	      System.out.println ("Unable to create statement: n" + excep);
	      System.exit(0);
	   }
	}
*/
   
   public void createTable(){
	   String agentTable = "CREATE TABLE tblAgentInfo2 ( " +
  		"ID INTEGER  NOT NULL," +
		" EntryTick INTEGER  NOT NULL," +
		" ExitTick INTEGER  NOT NULL," +
		" Infected INTEGER NOT NULL," +
		" InfectedTick INTEGER NOT NULL," +
		" InfectorID INTEGER  NOT NULL," +
		" InfectorStatus INTEGER  NOT NULL," +
		" InfectionContactType INTEGER NOT NULL" +	
		")";	   
	   
	   String sexualHistoryTable = "CREATE TABLE tblSexualHistory2 ( " +
  		" ID INTEGER NOT NULL," +
  		" Timestep INTEGER NOT NULL," +
		" InfectionStatus INTEGER NOT NULL," +
		" PartnerID INTEGER NOT NULL," +
		" PartnerInfectionStatus INTEGER NOT NULL," +
		" ContactType INTEGER NOT NULL" +
		")";

/*	   String sexualHistoryTable = "CREATE TABLE tblSexualHistory ( " +
	   		"ID INTEGER  NOT NULL," +
			" AgentTo INTEGER  NOT NULL," +
			" AgentFrom INTEGER  NOT NULL," +
			" Infected INTEGER NOT NULL," +
			" InfectorID INTEGER  NOT NULL," +
			" InfectorStatus INTEGER  NOT NULL," +
			" ContactType INTEGER NOT NULL" +
			")";
*/	   try {	   
		  statement.execute(agentTable);
		  statement.execute(sexualHistoryTable);
	      System.out.println ("+++++++++++++++++");
	      System.out.println ("+ table created +");
	      System.out.println ("+++++++++++++++++");
	   } catch (Exception excep) {
	      System.out.println ("Unable to create table: n" + excep);
	      System.exit(1);
	   }
	}

/*   public void createTable(){
	   String pairTable = "CREATE TABLE tblPairsJune10 ( " +
	   		"RecID INTEGER  NOT NULL," +
			" AgentTo INTEGER  NOT NULL," +
			" AgentFrom INTEGER  NOT NULL," +
			" Infected INTEGER NOT NULL," +
			" InfectorID INTEGER  NOT NULL," +
			" InfectorStatus INTEGER  NOT NULL," +
			" ContactType INTEGER NOT NULL" +
			")";
	   
	    * 				str += " (" + pair.agentFrom.getID() + ", " 
						+ pair.agentTo.getID() + ", "
						+ pair.infected + ", "
						+ pair.infectorID + ", "
						+ pair.infectorHIVStatus + ", "
						+ pair.contactType + ") ,";
	    
	   String pairTable = "CREATE TABLE tblPairs ( RecID INTEGER  NOT NULL," +
	   											" AgentTo INTEGER  NOT NULL," +
	   											" AgentFrom INTEGER  NOT NULL," +
	   											" Infected INTEGER NOT NULL," +
	   											" InfectorID INTEGER  NOT NULL," +
	   											" InfectorStatus INTEGER  NOT NULL," +
	   											"[ContactType] TEXT(50) NOT NULL" +
	   											")";

//	   String custTable = "CREATE TABLE tblCustomers (CustomerID INTEGER  NOT NULL,[Name] TEXT(50) NOT NULL)";
	   try {	   
//		  statement.execute(custTable);
		  statement.execute(pairTable);
	      System.out.println ("+++++++++++++++++");
	      System.out.println ("+ table created +");
	      System.out.println ("+++++++++++++++++");
	   } catch (Exception excep) {
	      System.out.println ("Unable to create table: n" + excep);
	      System.exit(1);
	   }
	}
*/
   public void insertRecord(String tableName, String data){
	   String sqlRecord = "insert into " + tableName + " values " + "(" + data + ");";	   
	   System.out.println ("record = " + sqlRecord);
	   try {
	      statement.executeUpdate (sqlRecord);
	      System.out.println ("+++++++++++++++++++");
	      System.out.println ("+ record inserted +");
	      System.out.println ("+++++++++++++++++++");
	   } catch (Exception excep) {
	      System.out.println ("Unable to insert record: n" + excep);
	      System.exit(0);
	   }
	}

/*   public void insertRecord(String id, String name){

	   String data = "("+ id + "," + "'" +   name + "'" + ");";

	   String sqlRecord = "insert into tblCustomers values " + data;
	   System.out.println ("record = " + sqlRecord);

	   try {
	      statement.executeUpdate (sqlRecord);
	      System.out.println ("+++++++++++++++++++");
	      System.out.println ("+ record inserted +");
	      System.out.println ("+++++++++++++++++++");
	   } catch (Exception excep) {
	      System.out.println ("Unable to insert record: n" + excep);
	      System.exit(0);
	   }
	}
*/
   public void commitChanges(){
	   try {
	      connection.commit();
	      System.out.println ("+++++++++++++++++++++");
	      System.out.println ("+ changes committed +");
	      System.out.println ("+++++++++++++++++++++");
	   } catch (Exception excep) {
	      System.out.println ("Unable to commit changes: n" + excep);
	      System.exit(0);
	   }
	}

   public void updateRecord(){
	   String sqlRecord = 
	      "UPDATE tblCustomers SET [Name] = 'Jane Jones' WHERE CustomerID = 1";

	   try {
	      statement.executeUpdate (sqlRecord);
	      System.out.println ("++++++++++++++++++");
	      System.out.println ("+ record updated +");
	      System.out.println ("++++++++++++++++++");
	   } catch (Exception excep) {
	      System.out.println ("Unable to update record: n" + excep);
	      System.exit(0);
	   }
	}
}

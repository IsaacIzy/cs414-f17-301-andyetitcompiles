package edu.colostate.cs.cs414.andyetitcompiles.p3.server;

import edu.colostate.cs.cs414.andyetitcompiles.p3.common.GameRecord;
import edu.colostate.cs.cs414.andyetitcompiles.p3.common.User;
import edu.colostate.cs.cs414.andyetitcompiles.p3.common.UserStatus;
import edu.colostate.cs.cs414.andyetitcompiles.p3.protocol.LoginResponse;
import edu.colostate.cs.cs414.andyetitcompiles.p3.protocol.RegisterResponse;
import edu.colostate.cs.cs414.andyetitcompiles.p3.protocol.UnregisterResponse;
import edu.colostate.cs.cs414.andyetitcompiles.p3.protocol.UserResponse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

public class DatabaseManagerSQL extends DatabaseManager {

	private String dbLocation = "~/jungleDB";
//									  + "?verifyServerCertificate=true"

	private String dbUsername = "Admin";
	private String useSSL = "&useSSL=true";
	private String ignoreCase = ";IGNORECASE=FALSE";
	private String dbPassword = "43533431342d4631372d416e645965744974436f6d70696c6573"; //CS414-F17-AndYetItCompiles
	
	private Connection connection;

	public DatabaseManagerSQL() throws ClassNotFoundException, SQLException{
		setupDB();//setup database connection and tables
	}
	
	public DatabaseManagerSQL(String dbFileLocation, String dbUsername, String dbPassword) throws ClassNotFoundException, SQLException{

		this.dbLocation = dbFileLocation;
		this.dbUsername = dbUsername;
		this.dbPassword = dbPassword;

		setupDB();//setup database connection and tables
	}
	
	/**
	 * Connect to database and setup tables if not already set up.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private void setupDB() throws ClassNotFoundException, SQLException {
		System.out.println("-------- Connecting to "+dbLocation+" -----------");
		Class.forName("org.h2.Driver");
		connection = DriverManager.getConnection("jdbc:h2:"+dbLocation+useSSL+ignoreCase, dbUsername, dbPassword);
		System.out.println("-------- Connected to "+dbLocation+" -----------");
		setupTables();
		//TODO set all online statuses to 0
	}
	
	private void setupTables() throws SQLException {
		//*****setup tables*****
		
		//setup userProfile table
		String sql = "CREATE TABLE IF NOT EXISTS userProfile (\r\n"
				+ "  idUser INT(11) NOT NULL AUTO_INCREMENT,\r\n"
				+ "  Username VARCHAR_IGNORECASE(63) NOT NULL,\r\n"
				+ "  Email VARCHAR_IGNORECASE(63) NOT NULL,\r\n"
				+ "  Password VARCHAR(63) NOT NULL,\r\n"
				+ "  Online TINYINT(1) NULL DEFAULT NULL,\r\n"
				+ "  PRIMARY KEY (idUser),\r\n"
				+ "  UNIQUE INDEX idUser_UNIQUE (idUser ASC),\r\n"
				+ "  UNIQUE INDEX Username_UNIQUE (Username ASC),\r\n"
				+ "  UNIQUE INDEX Email_UNIQUE (Email ASC))\r\n"
				+ "ENGINE = InnoDB\r\n"
				+ "AUTO_INCREMENT = 1"
				+ ";";
		connection.prepareStatement(sql).executeUpdate();
		
		//setup userHistory table
		sql = "CREATE TABLE IF NOT EXISTS `userHistory` (\r\n" + 
				"  `idUser` INT(11) NOT NULL,\r\n" + 
				"  `opponent` INT(11) NOT NULL,\r\n" + 
				"  `startTimestamp` TIMESTAMP NULL DEFAULT NULL,\r\n" + 
				"  `endTimestamp` TIMESTAMP NULL DEFAULT NULL,\r\n" + 
				"  `won` TINYINT(1) NULL DEFAULT NULL,\r\n" + 
				"  `abandoned` TINYINT(1) NULL DEFAULT NULL,\r\n" + 
//				"  INDEX `fk_UserHistory_UserProfile_idx` (`idUser` ASC),\r\n" + 
				"  PRIMARY KEY (`idUser`),\r\n" + 
//				"  CONSTRAINT `fk_UserHistory_UserProfile`\r\n" + 
				"    FOREIGN KEY (`idUser`)\r\n" + 
				"    REFERENCES `userprofile` (`idUser`)\r\n" + 
				"    ON DELETE CASCADE\r\n" + 
				"    ON UPDATE NO ACTION)\r\n" + 
				"ENGINE = InnoDB;";
		connection.prepareStatement(sql).executeUpdate();
			
		System.out.println("-------- Database tables configured -------");
	}
	
	public void resetTable() throws SQLException{
		System.out.println("-------- Database tables deleted -------");
		connection.prepareStatement("DROP TABLE IF EXISTS userHistory").execute();
		connection.prepareStatement("DROP TABLE IF EXISTS userProfile").execute();
		
		setupTables();
	}

	public RegisterResponse registerUser(User user) {
		if (checkUser(user)) {
			return new RegisterResponse(false, "Username or Email already in use");
		}

		String query = "INSERT INTO userProfile (Username, Email, Password, Online) "
					 + "VALUES('" + user.getNickname() + "', '" + user.getEmail() + "', '" + user.getPassword() + "', '0');";
		try {
			connection.prepareStatement(query).executeUpdate();
			return new RegisterResponse(true, "User Registered");

		} catch (SQLException e) {
			e.printStackTrace();
			return new RegisterResponse(false, "User Failed to register in database");
		}

	}

	private boolean checkUser(User user) {
		String sql = "SELECT * FROM userProfile" 
				   + " WHERE Username = '" + user.getNickname() +"'"
				   + " OR Email = '" + user.getEmail() +"'"
				   + " ;";
		try {
			ResultSet rtnSet = connection.prepareStatement(sql).executeQuery();
			int n=0;
			while(rtnSet.next()) {
				n++; 
			}
			if(n>0) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public RegisterResponse registerUser(String email, String nickname, String password) {
		return registerUser(new User(email, nickname, password));
	}

	public UnregisterResponse unRegisterUser(String email, String password) {
		
		//search for Email with matching password
		String sql = "SELECT * FROM userProfile" 
				   + " WHERE Email = '" + email	+ "'"			//if email matches 
				   + " AND Password = '" + password +"'"	//AND password (Case Sensitive) matches
				   + " ;";
		ResultSet rtnSet = null;
		try {
			rtnSet = connection.prepareStatement(sql).executeQuery();
			int n=0;
			String idUser = null;
			while(rtnSet.next()) {
				n++; //should never be more than 1
				idUser = rtnSet.getString("idUser");
			}
			//found 1 user
			if(n==1) {

				//remove user
				sql = "DELETE FROM userProfile WHERE idUser = " + idUser;
				connection.prepareStatement(sql).executeUpdate();
				return new UnregisterResponse(true, "Account Deleted");
			}
			if(n==0) {
				return new UnregisterResponse(false, "Account not found");
			}
			if(n>1) {
				System.out.println("SERIOUS DATABASE ERROR OCCURED!");
				return new UnregisterResponse(false, "Serious error occured");
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return new UnregisterResponse(false, "Failed to remove user");
					
		}
	}

	public LoginResponse authenticateUser(String email, String password) {
		String sql = "SELECT * FROM userProfile "
				   + "WHERE email LIKE '" + email + "'"
				   + "AND password LIKE '" + password + "'";
		try {
			ResultSet rtnSet = connection.prepareStatement(sql).executeQuery();
			String idUser = null;
			User tempUser = null;
			int n = 0;
			while (rtnSet.next()) {
				n++; // should never be more than 1
				idUser = rtnSet.getString("idUser");
				tempUser = new User(rtnSet.getInt("idUser"),rtnSet.getString("Email"),rtnSet.getString("Username"),"", UserStatus.ONLINE); 
			}
			// found 1 user
			if (n == 1) {
				// Return user information
				sql = "UPDATE userProfile "
					+ "SET Online = '1' "
					+ "WHERE idUser = '" + idUser + "'"; 
				connection.prepareStatement(sql).executeUpdate();
				return new LoginResponse(true, tempUser, "User login successful.");
			}
			if (n == 0) {
				return new LoginResponse(false, new User(), "User not found.");
			}
			if (n > 1) {
				System.out.println("SERIOUS DATABASE ERROR OCCURED!");
				return new LoginResponse(false, new User(), "Serious Error Occured.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new LoginResponse(false, new User(), "User login failed.");
	}

	public void logout(User user) {
		String sql = "UPDATE userProfile "
				   + "SET Online = '0' "
				   + "WHERE Username = '" + user.getNickname() + "'";
		try {
			connection.prepareStatement(sql).executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// take username or email as input for user.
	public UserResponse findUser(String username) {
		String sql = "SELECT * FROM userProfile WHERE Username LIKE '" + username +"'";
		try {
			ResultSet rtnSet = connection.prepareStatement(sql).executeQuery();
			int n = 0;
			User tempUser = new User();
			while (rtnSet.next()) {
				n++; // should never be more than 1
				boolean online = rtnSet.getBoolean("Online");
				int idUser = rtnSet.getInt("idUser");
				if(online) {
					tempUser = new User(idUser,rtnSet.getString("Email"),rtnSet.getString("Username"),"", UserStatus.ONLINE,gameHistory(idUser)); //user online
				}else {
					tempUser = new User(idUser,rtnSet.getString("Email"),rtnSet.getString("Username"),"", UserStatus.OFFLINE,gameHistory(idUser)); //user offline
				}
			}
			// found 1 user
			if (n == 1) {
				// Return user information
				return new UserResponse(true, tempUser, "User Found.");
			}
			if (n == 0) {
				return new UserResponse(false, new User(), "User not found.");
			}
			if (n > 1) {
				System.out.println("SERIOUS DATABASE ERROR OCCURED!");
				return new UserResponse(false, new User(), "Serious Error Occured.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new UserResponse(false, new User(), "User not found");
	}
	
	public boolean addGame(GameRecord user1, GameRecord user2) {

		try {
			int idUser1 = user1.getIdUser();
			int idUser2 = user2.getIdUser();
			Timestamp startTime1 = user1.getStartTime();
			Timestamp startTime2 = user2.getStartTime();
			Timestamp endTime1 = user1.getEndTime();
			Timestamp endTime2 = user2.getEndTime();
			int won1=0;
			if(user1.isWon()) { 
				won1=1;
			}
			int won2=0;
			if(user2.isWon()) { 
				won2=1;
			}
			int abandoned=0;
			if(user1.isAbandoned()) { 
				abandoned=1;
			}
			
			String query = "INSERT INTO userHistory (idUser, opponent, startTimestamp, endTimeStamp, won, abandoned) "
					+ "VALUES('" + idUser1 + "', '" + idUser2 + "', '"+startTime1+"', '"+endTime1+"', '"+won1+"', '"+abandoned+"' );";
			connection.prepareStatement(query).executeUpdate();

			query = "INSERT INTO userHistory (idUser, opponent, startTimestamp, endTimeStamp, won, abandoned) "
					+ "VALUES('" + idUser2 + "', '" + idUser1 + "', '"+startTime2+"', '"+endTime2+"', '"+won2+"', '"+abandoned+"' );";
			connection.prepareStatement(query).executeQuery();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public Set<GameRecord> gameHistory(int idUser){
		Set<GameRecord> record = new HashSet<>();
		
		String sql = "SELECT * FROM userHistory WHERE idUser = '" + idUser +"'";
			try {
				ResultSet rtnSet = connection.prepareStatement(sql).executeQuery();
				while (rtnSet.next()) {
					record.add(new GameRecord(rtnSet.getInt("idUser"), searchNickname(rtnSet.getInt("opponent")) , rtnSet.getTimestamp("startTimestamp"), rtnSet.getTimestamp("endTimestamp"), rtnSet.getBoolean("won"), rtnSet.getBoolean("abandoned")));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		
		return record;
	}
	
	private String searchNickname(int idUser) {
		String sql = "SELECT * FROM userProfile WHERE Username idUser '" + idUser +"'";
		try {
			ResultSet rtnSet = connection.prepareStatement(sql).executeQuery();
			while (rtnSet.next()) {
				return rtnSet.getString("Username"); //user online
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		return "User Not Found";
	}
	
	public static void main(String[] argv) {
			DatabaseManagerSQL db = null;
		try {
			db = new DatabaseManagerSQL();
			db.resetTable();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		
		RegisterResponse regResp = db.registerUser( "Email@email", "Nickname", "Password");
		System.out.println(regResp.getMessage());
		regResp = db.registerUser( "Email2@email", "Nickname2", "Password");
		System.out.println(regResp.getMessage());
		
		//db.addGame(new GameRecord(1, "nickname2", new Timestamp(5), new Timestamp(55), true, false), new GameRecord(2, "nickname", new Timestamp(5), new Timestamp(55), false, false));
		
		UserResponse uResp = db.findUser("Nickname");
		System.out.println(uResp.getMessage());
	}
}
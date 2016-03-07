package src.db;

import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * データベースを扱うためのテンプレート処理(すべてのデータベース操作クラスのスーパークラス)
 * @author murase
 *
 */
public class HandleDbTemplateSuper {
//	private static final String MySQLJDBCDriver = "org.gjt.mm.mysql.Driver";
	/** postgresql のJDBCDriver */
	public static final String POSTGRESJDBCDRIVER_STRING = "org.postgresql.Driver";
	/** 旧日本測地系 */
	public static final int TOKYO97_EPGS_CODE = 4301;
	/** WGS84 経度緯度(世界測地系とほぼ同じ) */
	public static final int WGS84_EPSG_CODE = 4326;
	/** 世界測地系UTMゾーン53 */
	public static final int WGS84_UTM_EPGS_CODE=3099;
	// 下記の変数を正しく設定する
	// DBNAME, DBDIR, USER, PASS, JDBCDriver, DBURL
	
	/** DB名 */
	private String DBNAME;// = "blue_db"; // Database Name
	/** ユーザ名 */
	private String USER;// = "root"; // user name for DB.
	/** パスワード */
	private String PASS;// = "usadasql"; // password for DB.hoge$#34hoge5
	/** JDBCDriver */
	private String JDBCDriver = POSTGRESJDBCDRIVER_STRING;
	/** URL */
	private String DBURL;// = "jdbc:mysql://amon2.elcom.nitech.ac.jp:3306/" + DBNAME;
	/**  */
	public Statement stmt = null;
	
	/**
	 * コンストラクタ　データベースの設定.
	 * @param aDbName
	 * @param aUser
	 * @param aPass
	 * @param aDbUrl
	 */
	public HandleDbTemplateSuper(String aDbName, String aUser, String aPass, String aDbUrl) {
		DBNAME = aDbName;
		USER = aUser;
		PASS = aPass;
		DBURL = aDbUrl;
	}
	/**
	 * コンストラクタ　データベースの設定.
	 * @param aDbName
	 * @param aUser
	 * @param aPass
	 * @param aDbUrl
	 * @param JDBCDriver
	 */
	public HandleDbTemplateSuper(String aDbName, String aUser, String aPass, String aDbUrl, String JDBCDriver) {
		DBNAME = aDbName;
		USER = aUser;
		PASS = aPass;
		DBURL = aDbUrl;
		this.JDBCDriver = JDBCDriver;
		
	}
	
	/**
	 * データベースへ接続
	 */
	public void startConnection(){
		try{
			connect();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * データベースから切断
	 */
	public void endConnection(){
		try{
			disconnect();
		}catch(Exception exception){
			
		}
	}

	
	// ここから先はコピペのプログラム.
	/*
	 *  Service Functions
	 *  ここから先は，決まり文句を関数化したもの．
	 */
 	public Connection conn = null;
 	/**
 	 * database open
 	 * @throws SQLException
 	 * @throws ClassNotFoundException
 	 */
	public void connect() 
	throws SQLException, ClassNotFoundException {
		try {
			// JDBC Driver Loading
			Class.forName(JDBCDriver).newInstance();
			System.setProperty("jdbc.driver",JDBCDriver);
		}
		catch (Exception e) {
			// Error Message and Error Code
			System.out.print(e.toString());
			if (e instanceof SQLException) {
				System.out.println("Error Code:" + ((SQLException)e).getErrorCode());
			}
			// Print Stack Trace
			e.printStackTrace();
		}
		try {
			// Connection
			if ( USER.isEmpty() && PASS.isEmpty() ) {
				conn = DriverManager.getConnection(DBURL);			
			}
			else {
				Properties prop = new Properties();
				prop.put("user", USER);
				prop.put("password", PASS);
				conn = DriverManager.getConnection(DBURL,prop);	
			}
		}
		catch (Exception e) {
			// Error Message and Error Code
			System.out.print(e.toString());
			if (e instanceof SQLException) {
				System.out.println("Error Code:" + ((SQLException)e).getErrorCode());
			}
			// Print Stack Trace
			e.printStackTrace();
			if (conn != null) {
				conn.rollback();
				conn.close();
			}
		}
	}

	/**
	 * database close
	 * @throws SQLException
	 */
	public void disconnect()
	    throws SQLException {
		try {
			conn.close();
		}
		catch (Exception e) {
			// Error Message and Error Code
			System.out.print(e.toString());
			if (e instanceof SQLException) {
				System.out.println("Error Code:" + ((SQLException)e).getErrorCode());
			}
			// Print Stack Trace
			e.printStackTrace();
			if (conn != null) {
				conn.rollback();
				conn.close();
			}
		}
	}
	
	/***
	 * execute select文用
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public ResultSet execute(String sql)
	    throws SQLException {
		try {	
			conn.setReadOnly(true);
			// Execute 'commit' Automatically after each SQL
			// conn.setAutoCommit(true);		 
			// Query Exection 		 
			stmt = conn.createStatement();
			return stmt.executeQuery(sql);
		}
		catch (Exception e) {
			// Error Message and Error Code
			System.out.print(e.toString());
			if (e instanceof SQLException) {
				System.out.println("Error Code:" + ((SQLException)e).getErrorCode());
			}
			// Print Stack Trace
			e.printStackTrace();
			if (conn != null) {
				conn.rollback();
				conn.close();
			}
			return null;
		}
	}
	
	 
	/**
	 * insert into 用  (データベースへデータの挿入)
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public int insertInto(String sql)
		    throws SQLException {
			try {	
				conn.setReadOnly(false);
				// Execute 'commit' Automatically after each SQL
				// conn.setAutoCommit(true);		 
				// Query Exection 		 
				stmt = conn.createStatement();
				return stmt.executeUpdate(sql);
			}
			catch (Exception e) {
				// Error Message and Error Code
				System.out.print(e.toString());
				if (e instanceof SQLException) {
					System.out.println("Error Code:" + ((SQLException)e).getErrorCode());
				}
				// Print Stack Trace
				e.printStackTrace();
				if (conn != null) {
					conn.rollback();
					conn.close();
				}
				return -1;
			}
		}
	
}

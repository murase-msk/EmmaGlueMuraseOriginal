package src;

/**
 * データベースの設定
 * @author murase
 *
 */
public class DbConfig {
	/** DBユーザ名 */
	public static final String USER = "postgres";			// user name for DB.
	/** DBパスワード */
	public static final String PASS = "usadasql";		// password for DB.
	/** DBのURL */
	public static final String URL = "rain2.elcom.nitech.ac.jp";//osm3.cgq49v9481vk.ap-northeast-1.rds.amazonaws.com
//	public static final String URL = "osm3.cgq49v9481vk.ap-northeast-1.rds.amazonaws.com";
	/** DBのポート */
	public static final int PORT = 5432;
	
	/** 道路データDB名 */
	public static final String DBNAME_osm_road_db = "osm_road_db";
	/** 道路データのURL */
	public static final String DBURL_osm_road_db = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME_osm_road_db;
	
	/** ストロークのスキーマ名 */
	public static final String SCHEMA_stroke = "stroke_v2";
	/** ストロークのテーブル名 */
	public static final String TBNAME_flatted_stroke_table = "flatted_stroke_table";
	
	/** public */
	public static final String SCHEMA_public  = "public";
	/** すべての道路を持つテーブル名 */
	public static final String TBNAME_osm_japan_car_bike_foot_2po_4pgr = "osm_japan_car_bike_foot_2po_4pgr";
	/** 自動車道路を持つテーブル名 */
	public static final String TBNAME_osm_japan_car_2po_4pgr = "osm_japan_car_2po_4pgr";
	/** 鉄道のデータを持つテーブル名 */
	public static final String TBNAME_osm_japan_rail_2po_4pgr = "osm_japan_rail_2po_4pgr";
	
	/** OSMのデータに関するDB */
	public static final String DBNAME_osm_all_db = "osm_all_db";
	/** OSMのデータに関するデータベースのURL */
	public static final String DBURL_osm_all_db = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME_osm_all_db;
	/** OSMの線データに関するテーブル名 */
	public static final String TBNAME_planet_osm_line = "planet_osm_line";
	/** OSMの面データに関するテーブル名 */
	public static final String TBNAME_planet_osm_polygon = "planet_osm_polygon";
	
	/** モリコロパークのDB名 */
	public static final String DBNAME_osm_morikoro_20151201 = "osm_morikoro_20151201";
	/** モリコロパークのDBURL */
	public static final String DBURL_osm_morikoro_20151201 = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME_osm_morikoro_20151201;


}

package src.db.getData;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.naming.java.javaURLContextFactory;
import org.postgis.PGgeometry;

import src.db.GeometryParsePostgres;
import src.db.HandleDbTemplateSuper;


/**
 * OSM道路データを扱う
 * @author murase
 *
 */
public class OsmRoadDataGeom extends HandleDbTemplateSuper {
	private static final String DBNAME = "osm_road_db";	// Database Name
	private static final String USER = "postgres";			// user name for DB.
	private static final String PASS = "usadasql";		// password for DB.
	private static final String URL = "rain2.elcom.nitech.ac.jp";
	private static final int PORT = 5432;
	private static final String DBURL = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME;
	
	/** リンクID */
	public ArrayList<Integer> _linkId;
	/** (sourcePoint, targetPoint)の組 */
	public ArrayList<Line2D> _link;
	public ArrayList<Integer> _sourceId;
	public ArrayList<Integer> _targetId;
	/** km */
	public ArrayList<Double> _length;
	/** cost */
	public ArrayList<Double> _length2;
	/** 道路のクラス */
	public ArrayList<Integer> _clazz;
	/** 道路の形状を表す */
	public ArrayList<ArrayList<Line2D>> _arc;
	
	//////////////////////////
	/** リンクID */
	public ArrayList<Integer> __linkId;
	/** (sourcePoint, targetPoint)の組 */
	public ArrayList<Line2D> __link;
	public ArrayList<Integer> __sourceId;
	public ArrayList<Integer> __targetId;
	/** km */
	public ArrayList<Double> __length;
	/** cost */
	public ArrayList<Double> __length2;
	/** 道路のクラス */
	public ArrayList<Integer> __clazz;
	/** 道路の形状を表す */
	public ArrayList<ArrayList<Line2D>> __arc;
	
	public OsmRoadDataGeom(){
		super(DBNAME, USER, PASS, DBURL, HandleDbTemplateSuper.POSTGRESJDBCDRIVER_STRING);
	}
	
	/**
	 * 矩形範囲のデータを取り出す
	 */
	public void insertOsmRoadData(Point2D aUpperLeftLngLat, Point2D aLowerRightLngLat){
		_linkId = new ArrayList<>();
		_link = new ArrayList<>();
		_sourceId = new ArrayList<>();
		_targetId = new ArrayList<>();
		_length = new ArrayList<>();
		_length2 = new ArrayList<>();
		_clazz = new ArrayList<>();
		_arc = new ArrayList<>();
		
		try{
			String statement;
			// SRID=4326.
			statement = "select " +
					" id, osm_name,osm_source_id, osm_target_id, clazz, source, target, km, cost, x1, y1, x2, y2, geom_way" +
					" from osm_japan_car_2po_4pgr " +
					" where" +
					" st_intersects(" +
						"st_geomFromText(" +
							"'polygon(("+
								aUpperLeftLngLat.getX()+" "+aLowerRightLngLat.getY()+","+
								aLowerRightLngLat.getX()+" "+aLowerRightLngLat.getY()+","+
								aLowerRightLngLat.getX()+" "+aUpperLeftLngLat.getY()+","+
								aUpperLeftLngLat.getX()+" "+aUpperLeftLngLat.getY()+","+
								aUpperLeftLngLat.getX()+" "+aLowerRightLngLat.getY()+
							"))',"+WGS84_EPSG_CODE+
						"), "+
					"geom_way) " +
					" and " +
					" clazz > 12" +
					"";
			System.out.println(statement);
			ResultSet rs = execute(statement);
			while(rs.next()){
				_linkId.add(rs.getInt("id"));
				_sourceId.add(rs.getInt("source"));
				_targetId.add(rs.getInt("target"));
				_link.add((Line2D)new Line2D.Double(rs.getDouble("x1"), rs.getDouble("y1"), rs.getDouble("x2"), rs.getDouble("y2")));
				_length.add(rs.getDouble("km"));
				_length2.add(rs.getDouble("cost"));
				_clazz.add(rs.getInt("clazz"));
//				System.out.println(GeometryParsePostgres.getLineStringMultiPoint((PGgeometry)rs.getObject("geom")));
				_arc.add(GeometryParsePostgres.getLineStringMultiLine((PGgeometry)rs.getObject("geom_way")));
			}
			rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * 指定したpolygon(wkt形式のString型)内の道路データを取得する
	 */
	public void getOsmRoadFromPolygon(Point2D aCenterLngLat, double aRadius){
		__linkId = new ArrayList<>();
		__link = new ArrayList<>();
		__sourceId = new ArrayList<>();
		__targetId = new ArrayList<>();
		__length = new ArrayList<>();
		__length2 = new ArrayList<>();
		__clazz = new ArrayList<>();
		__arc = new ArrayList<>();
		
		try{
			String statement;
			// SRID=4326.
			statement = "select " +
					" id, osm_name,osm_source_id, osm_target_id, clazz, source, target, km, cost, x1, y1, x2, y2, geom_way" +
					" from osm_japan_car_2po_4pgr " +
					" where" +
					" st_intersects(" +
						"st_transform(" +
							"ST_Buffer(" +
								"st_transform(" +
									"ST_SetSRID(ST_MakePoint("+aCenterLngLat.getX()+", "+aCenterLngLat.getY()+"),"+WGS84_EPSG_CODE+"), "+
									WGS84_UTM_EPGS_CODE+"" +
								"), "+aRadius+"" +
							"), "+WGS84_EPSG_CODE+"" +
						"), "+
						"geom_way) " +
					" and " +
					" clazz > 12" +
					"";
			System.out.println(statement);
			ResultSet rs = execute(statement);
			while(rs.next()){
				__linkId.add(rs.getInt("id"));
				__sourceId.add(rs.getInt("source"));
				__targetId.add(rs.getInt("target"));
				__link.add((Line2D)new Line2D.Double(rs.getDouble("x1"), rs.getDouble("y1"), rs.getDouble("x2"), rs.getDouble("y2")));
				__length.add(rs.getDouble("km"));
				__length2.add(rs.getDouble("cost"));
				__clazz.add(rs.getInt("clazz"));
//				System.out.println(GeometryParsePostgres.getLineStringMultiPoint((PGgeometry)rs.getObject("geom")));
				__arc.add(GeometryParsePostgres.getLineStringMultiLine((PGgeometry)rs.getObject("geom_way")));
			}
			rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	//////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////
	/////////////////このクラスに入れるべきでないメソッド/////////////////
	//////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////
	
	/**
	 * 1ピクセルあたりのx方向y方向の長さ(メートル)を取得する
	 * @param aCenterLngLat
	 * @param aLnglatPerPixel
	 * @return
	 */
	public Point2D calcMeterPerPixel(Point2D aCenterLngLat, Point2D aLnglatPerPixel){
		try{
			String stmt;
			//8202ms
//			stmt = "select " +
//					"st_length(" +
//						"st_transform(" +
//							"st_geomFromText(" +
//								"'lineString("+aCenterLngLat.getX()+" "+aCenterLngLat.getY()+"," +
//									""+(aCenterLngLat.getX()+aLnglatPerPixel.getX())+" "+aCenterLngLat.getY()+"" +
//								")',"+WGS84_EPSG_CODE+"" +
//							"), "+WGS84_UTM_EPGS_CODE+"" +
//						")" +
//					") as lengthX," +
//					"st_length(" +
//						"st_transform(" +
//							"st_geomFromText(" +
//								"'lineString("+aCenterLngLat.getX()+" "+aCenterLngLat.getY()+"," +
//									""+(aCenterLngLat.getX())+" "+(aCenterLngLat.getY()+aLnglatPerPixel.getY())+"" +
//								")',"+WGS84_EPSG_CODE+"" +
//							"), "+WGS84_UTM_EPGS_CODE+"" +
//						")" +
//					") as lengthY";
			//8300ms
			stmt = "select " +
					"st_length(" +
						"st_transform(" +
							"st_setSRID(" +
								"st_makeLine(" +
									"st_makePoint("+aCenterLngLat.getX()+","+aCenterLngLat.getY()+")," +
									"st_makePoint("+(aCenterLngLat.getX()+aLnglatPerPixel.getX())+","+aCenterLngLat.getY()+")" +
								"), " +
								""+WGS84_EPSG_CODE+"" +
							"), "+WGS84_UTM_EPGS_CODE+"" +
						")" +
					") as lengthX," +
					"st_length(" +
						"st_transform(" +
							"st_setSRID(" +
								"st_makeLine(" +
									"st_makePoint("+aCenterLngLat.getX()+","+aCenterLngLat.getY()+")," +
									"st_makePoint("+aCenterLngLat.getX()+","+(aCenterLngLat.getY()+aLnglatPerPixel.getY())+")" +
								"), " +
								""+WGS84_EPSG_CODE+"" +
							"), "+WGS84_UTM_EPGS_CODE+"" +
						")" +
					") as lengthY";
			
			System.out.println(stmt);
			ResultSet rs = execute(stmt);
			if(rs.next()){
				return new Point2D.Double(rs.getDouble("lengthX"), rs.getDouble("lengthY"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 2点の緯度経度から長さ(メートル)を求める
	 * 
	 */
	public double calcMeterLength(Point2D p1, Point2D p2){
		try{
			String stmt = "select st_length(" +
						"st_transform(" +
							"st_setSRID(" +
								"st_makeLine(" +
									"st_makePoint("+p1.getX()+","+p1.getY()+")," +
									"st_makePoint("+p2.getX()+","+p2.getY()+")" +
								"), " +
								""+WGS84_EPSG_CODE+"" +
							"), "+WGS84_UTM_EPGS_CODE+"" +
						")" +
					") as length";
			ResultSet rSet = execute(stmt);
			if(rSet.next()){
				return rSet.getDouble("length");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	
}

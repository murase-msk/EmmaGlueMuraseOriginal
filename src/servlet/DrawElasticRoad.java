package servlet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import src.ElasticPoint;
import src.coordinate.ConvertLngLatXyCoordinate;
import src.coordinate.ConvertMercatorXyCoordinate;
import src.coordinate.GetLngLatOsm;
import src.coordinate.LngLatMercatorUtility;
import src.db.getData.OsmRoadDataGeom;

/**
 * 伸縮する道路を描画
 * http://localhost:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawElasticRoad
 * @author murase
 *
 */
public class DrawElasticRoad {
	
	/** 地図の大きさ */
	public static Point DEFAULT_WINDOW_SIZE = new Point(700, 700);
	/** 初期の緯度経度Point2D形式 */
	private static final Point2D.Double DEFAULT_LNGLAT = new Point2D.Double(136.9309671669116, 35.15478942665804);// 鶴舞公園.
	
	/** focusのスケール */
	private static final int FOCUS_SCALE = 16;
	/** contextのスケール */
	private static final int CONTEXT_SCALE = 15;
	/** glue内側の半径(pixel) */
	private static final int GLUE_INNER_RADIUS=200;
	/** glue外側の半径(pixel) */
	private static final int GLUE_OUTER_RADIUS=300;
	/** glue部分の同心円方向の縮尺の種類 */
	private static final int GLUE_SCALE_NUM = GLUE_OUTER_RADIUS - GLUE_INNER_RADIUS;
	
	// 中心点からglue内側の長さ.
	public double glueInnerRadiusMeter;
	// 中心点からglue外側の長さ.
	public double glueOuterRadiusMeter;
	
	
	Graphics2D _graphics2d;
	/** focus */
	public GetLngLatOsm _getLngLatOsmFocus;
	/** focus領域の緯度経度xy変換 */
	public ConvertLngLatXyCoordinate _convertFocus;
	/** context */
	public GetLngLatOsm _getLngLatOsmContext;
	/** context領域の緯度経度xy変換 */
	public ConvertLngLatXyCoordinate _convertContext;
	/** メルカトル座標系xy変換 */
	public ConvertMercatorXyCoordinate _contextMercatorConvert;
//	public Point2D _upperLeftLngLat;
//	public Point2D _lowerRightLngLat;
	
	
	// 一時的な変数.
	public double __radiationDirectionScale;
	public ArrayList<Double>__concentricCircleScaleArray;
	
	public DrawElasticRoad(HttpServletRequest request, HttpServletResponse response) {
		try{
			OutputStream out=response.getOutputStream();
			ImageIO.write( drawImage(), "png", out);
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	
	
	/**
	 * 道路データの取得しbufferedimageの作成
	 * @return
	 */
	private BufferedImage drawImage(){
		BufferedImage bfImage=null;
		bfImage=new BufferedImage( DEFAULT_WINDOW_SIZE.x, DEFAULT_WINDOW_SIZE.y, BufferedImage.TYPE_INT_ARGB);
		_graphics2d = (Graphics2D) bfImage.getGraphics();
		// アンチエイリアス設定：遅いときは次の行をコメントアウトする.
		_graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// 中心座標とスケールから 左上と右下の座標取得.
//		_getLngLatOsm = new GetLngLatOsm(new Point2D.Double(DEFAULT_LNG, DEFAULT_LAT), DEFAULT_SCALE, new Point(WINDOW_WIDTH, WINDOW_HEIGHT));
//		_upperLeftLngLat = _getLngLatOsm._upperLeftLngLat;
//		_lowerRightLngLat = _getLngLatOsm._lowerRightLngLat;
		// 緯度経度とXy座標の変換用インスタンス.
//		_convert = new ConvertLngLatXyCoordinate((Point2D.Double)_upperLeftLngLat,
//				(Point2D.Double)_lowerRightLngLat, new Point(WINDOW_WIDTH, WINDOW_HEIGHT));
		
		
		//focus用の緯度経度xy変換
		_getLngLatOsmFocus = new GetLngLatOsm(DEFAULT_LNGLAT, FOCUS_SCALE, DEFAULT_WINDOW_SIZE);
		_convertFocus = new ConvertLngLatXyCoordinate((Point2D.Double)_getLngLatOsmFocus._upperLeftLngLat,
				(Point2D.Double)_getLngLatOsmFocus._lowerRightLngLat, DEFAULT_WINDOW_SIZE);
		//context用の緯度経度xy変換
		_getLngLatOsmContext = new GetLngLatOsm(DEFAULT_LNGLAT, CONTEXT_SCALE, DEFAULT_WINDOW_SIZE);
		_convertContext = new ConvertLngLatXyCoordinate((Point2D.Double)_getLngLatOsmContext._upperLeftLngLat,
				(Point2D.Double)_getLngLatOsmContext._lowerRightLngLat, DEFAULT_WINDOW_SIZE);
		glueInnerRadiusMeter = GLUE_INNER_RADIUS*_convertFocus.meterPerPixel.getX();
		glueOuterRadiusMeter = GLUE_OUTER_RADIUS*_convertContext.meterPerPixel.getX();
		// contextでのメルカトル座標系xy変換.
		_contextMercatorConvert = new ConvertMercatorXyCoordinate(
				LngLatMercatorUtility.ConvertLngLatToMercator((Point2D.Double)_getLngLatOsmContext._upperLeftLngLat), 
				LngLatMercatorUtility.ConvertLngLatToMercator((Point2D.Double)_getLngLatOsmContext._lowerRightLngLat), DEFAULT_WINDOW_SIZE);
		
		// 道路データの取得.
		OsmRoadDataGeom osmRoadDataGeom = new OsmRoadDataGeom();
		osmRoadDataGeom.startConnection();
		// 矩形範囲内の道路データを取得する.
		osmRoadDataGeom.insertOsmRoadData(_getLngLatOsmContext._upperLeftLngLat, _getLngLatOsmContext._lowerRightLngLat);
		osmRoadDataGeom.__arc = osmRoadDataGeom._arc;
		// 半径150pixel分の道路データを取得する.
//		osmRoadDataGeom.getOsmRoadFromPolygon(new Point2D.Double(DEFAULT_LNG, DEFAULT_LAT), (_convert.meterPerPixel.getX() * glue_outer_radius));
		
		
		osmRoadDataGeom.endConnection();
		
		// 道路の描画.
		paintElasticRoadData(osmRoadDataGeom.__arc);
		
		// glueの枠線の描画.
		_graphics2d.setColor(Color.red);
		// 中心点.
		_graphics2d.drawOval(DEFAULT_WINDOW_SIZE.x/2-2, DEFAULT_WINDOW_SIZE.x/2-2, 4, 4);
		// glue領域内側想定範囲.
		_graphics2d.drawOval(DEFAULT_WINDOW_SIZE.x/2-GLUE_INNER_RADIUS, DEFAULT_WINDOW_SIZE.x/2-GLUE_INNER_RADIUS, GLUE_INNER_RADIUS*2, GLUE_INNER_RADIUS*2);
		// glue領域外側想定範囲.
		_graphics2d.drawOval(DEFAULT_WINDOW_SIZE.x/2-GLUE_OUTER_RADIUS, DEFAULT_WINDOW_SIZE.x/2-GLUE_OUTER_RADIUS, GLUE_OUTER_RADIUS*2, GLUE_OUTER_RADIUS*2);
		
		return bfImage;
	}
	

	
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	///////////////////道路描画について//////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/**
	 * 伸縮した道路データの描画.
	 * @param __arc
	 */
	public void paintElasticRoadData(ArrayList<ArrayList<Line2D>> __arc){
		// あるセグメントにおける始点.
		Point p1Xy;
		// あるセグメントにおける終点.
		Point p2Xy;
		
		ElasticPoint elasticPoint = new ElasticPoint(
				_contextMercatorConvert.mercatorPerPixel.getX()*GLUE_INNER_RADIUS, 
				_contextMercatorConvert.mercatorPerPixel.getX()*GLUE_OUTER_RADIUS, 
				Math.pow(2, FOCUS_SCALE-CONTEXT_SCALE), 
				LngLatMercatorUtility.ConvertLngLatToMercator(DEFAULT_LNGLAT));
		
		for(ArrayList<Line2D> arrArc : __arc){
			for(Line2D arc : arrArc){
				// 2点の緯度経度から中心までの距離(メートル)を求める.
				double p1Meter = LngLatMercatorUtility.calcDistanceFromLngLat(DEFAULT_LNGLAT, arc.getP1());
				double p2Meter = LngLatMercatorUtility.calcDistanceFromLngLat(DEFAULT_LNGLAT, arc.getP2());
				// p1について.
				if(p1Meter < glueInnerRadiusMeter){	// focus領域にある.
					p1Xy = _convertFocus.convertLngLatToXyCoordinate(arc.getP1());
					continue;
				}else if ( glueInnerRadiusMeter < p1Meter && p1Meter < glueOuterRadiusMeter){// glue領域にある.
					// glue内側から見て何パーセントの位置にあるか(0~1).
					double glueRatio = (p1Meter-glueInnerRadiusMeter)/(glueOuterRadiusMeter - glueInnerRadiusMeter);
					Point2D elasticPointMercator = elasticPoint.calcElasticPoint(LngLatMercatorUtility.ConvertLngLatToMercator(arc.getP1()), glueRatio);
					p1Xy = _contextMercatorConvert.convertMercatorToXyCoordinate(elasticPointMercator);
//					continue;
				}else{// context領域にある.
					p1Xy = _convertContext.convertLngLatToXyCoordinate(arc.getP1());
					continue;
				}
				// p2について.
				if(p2Meter < glueInnerRadiusMeter){	// focus領域にある.
					p2Xy = _convertFocus.convertLngLatToXyCoordinate(arc.getP2());
					continue;
				}else if ( glueInnerRadiusMeter < p2Meter && p2Meter < glueOuterRadiusMeter){// glue領域にある.
					// glue内側から見て何パーセントの位置にあるか(0~1).
					double glueRatio = (p2Meter-glueInnerRadiusMeter)/(glueOuterRadiusMeter - glueInnerRadiusMeter);
					Point2D elasticPointMercator = elasticPoint.calcElasticPoint(LngLatMercatorUtility.ConvertLngLatToMercator(arc.getP2()), glueRatio);
					p2Xy = _contextMercatorConvert.convertMercatorToXyCoordinate(elasticPointMercator);
//					continue;
				}else{// context領域にある.
					p2Xy = _convertContext.convertLngLatToXyCoordinate(arc.getP2());
					continue;
				}
				paint2dLine(new Line2D.Double(p1Xy, p2Xy), Color.pink, (float)3);
			}
		}
	}

	// 線分の描画.
	private void paint2dLine(Line2D aLine, Color aColor, float aLineWidth){
		Line2D linkLine = aLine;
		// 線の幅.
		BasicStroke wideStroke = new BasicStroke(aLineWidth);
		_graphics2d.setStroke(wideStroke);
		_graphics2d.setPaint(aColor);
		_graphics2d.draw(linkLine);
	}

	
	////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////

}

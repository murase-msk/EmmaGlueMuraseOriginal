package servlet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.org.apache.bcel.internal.generic.NEW;

import src.coordinate.ConvertLngLatXyCoordinate;
import src.coordinate.ConvertPolarPlaneCoordinate;
import src.coordinate.GetLngLatOsm;
import src.db.getData.OsmRoadDataGeom;

/**
 * 伸縮する道路を描画
 * http://localhost:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawElasticRoad
 * @author murase
 *
 */
public class DrawElasticRoad {
	
	/** 地図パネルの横幅. */
	public static  int WINDOW_WIDTH = 700;
	/** 地図パネルの高さ. */
	public static  int WINDOW_HEIGHT = 700;
	/** 初期の経度. */
	private static final double DEFAULT_LNG = 136.9309671669116;	// 鶴舞公園.
	/** 初期の緯度. */
	private static final double DEFAULT_LAT = 35.15478942665804;	// 鶴舞公園.
	/** 初期の緯度経度Point2D形式 */
	private static final Point2D.Double DEFAULT_LNGLAT = new Point2D.Double(DEFAULT_LNG, DEFAULT_LAT);
	/** 初期のスケール. */
//	private static final int DEFAULT_SCALE = 15;
	
	
	/** focusのスケール */
	private static final int FOCUS_SCALE = 16;
	/** contextのスケール */
	private static final int CONTEXT_SCALE = 15;
	/** glue内側の半径 */
	private static final int GLUE_INNER_RADIUS=200;
	/** glue外側の半径 */
	private static final int GLUE_OUTER_RADIUS=300;
	/** glue部分の同心円方向の縮尺の種類 */
	private static final int GLUE_SCALE_NUM = GLUE_OUTER_RADIUS - GLUE_INNER_RADIUS;
	
	// 中心点からglue内側の長さ.
	public double glueInnerRadiusMeter;
	// 中心点からglue外側の長さ.
	public double glueOuterRadiusMeter;
	
	
	Graphics2D _graphics2d;
//	public GetLngLatOsm _getLngLatOsm;
//	public ConvertLngLatXyCoordinate _convert;
	/** focus */
	public GetLngLatOsm _getLngLatOsmFocus;
	/** focus領域の緯度経度xy変換 */
	public ConvertLngLatXyCoordinate _convertFocus;
	/** context */
	public GetLngLatOsm _getLngLatOsmContext;
	/** context領域の緯度経度xy変換 */
	public ConvertLngLatXyCoordinate _convertContext;
	/** glue領域の緯度経度xy変換 */
	public ArrayList<ConvertLngLatXyCoordinate> _arrayConvert;
//	public Point2D _upperLeftLngLat;
//	public Point2D _lowerRightLngLat;
	
	
	// 一時的な変数.
	public double __radiationDirectionScale;
	public ArrayList<Double>__concentricCircleScaleArray;
	
	public DrawElasticRoad(HttpServletRequest request, HttpServletResponse response) {
		try{
			OutputStream out=response.getOutputStream();
			//BufferedImage img = emgd.getEmGlueImage(param);
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
		bfImage=new BufferedImage( WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
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
		_getLngLatOsmFocus = new GetLngLatOsm(new Point2D.Double(DEFAULT_LNG, DEFAULT_LAT), FOCUS_SCALE, new Point(WINDOW_WIDTH, WINDOW_HEIGHT));
		_convertFocus = new ConvertLngLatXyCoordinate((Point2D.Double)_getLngLatOsmFocus._upperLeftLngLat,
				(Point2D.Double)_getLngLatOsmFocus._lowerRightLngLat, new Point(WINDOW_WIDTH, WINDOW_HEIGHT));
		//context用の緯度経度xy変換
		_getLngLatOsmContext = new GetLngLatOsm(new Point2D.Double(DEFAULT_LNG, DEFAULT_LAT), CONTEXT_SCALE, new Point(WINDOW_WIDTH, WINDOW_HEIGHT));
		_convertContext = new ConvertLngLatXyCoordinate((Point2D.Double)_getLngLatOsmContext._upperLeftLngLat,
				(Point2D.Double)_getLngLatOsmContext._lowerRightLngLat, new Point(WINDOW_WIDTH, WINDOW_HEIGHT));
		glueInnerRadiusMeter = GLUE_INNER_RADIUS*_convertFocus.meterPerPixel.getX();
		glueOuterRadiusMeter = GLUE_OUTER_RADIUS*_convertContext.meterPerPixel.getX();
		// GLUE_SCALE_NUM種類のの変換オブジェクト.
		_arrayConvert = calcArrayConvert();
		
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
		
		_graphics2d.setColor(Color.red);
		// 中心点.
		_graphics2d.drawOval(WINDOW_WIDTH/2-2, WINDOW_WIDTH/2-2, 4, 4);
		// glue領域内側想定範囲.
		_graphics2d.drawOval(WINDOW_WIDTH/2-GLUE_INNER_RADIUS, WINDOW_WIDTH/2-GLUE_INNER_RADIUS, GLUE_INNER_RADIUS*2, GLUE_INNER_RADIUS*2);
		// glue領域外側想定範囲.
		_graphics2d.drawOval(WINDOW_WIDTH/2-GLUE_OUTER_RADIUS, WINDOW_WIDTH/2-GLUE_OUTER_RADIUS, GLUE_OUTER_RADIUS*2, GLUE_OUTER_RADIUS*2);
		
		return bfImage;
	}
	
	/**
	 * glueの内側から外側までの座標変換のオブジェクト生成(100個生成する)
	 * focusからcontextまで行くと縦横(1/2)になるとする
	 * 例:glue内側からglue外側まで100pixelあるとするとXpixel移動するとスケールはfocusスケールの2^(X/100)倍になる
	 * @return
	 */
	public ArrayList<ConvertLngLatXyCoordinate> calcArrayConvert(){
		ArrayList<ConvertLngLatXyCoordinate> arrayConvert= new ArrayList<>();
		// 端点の緯度経度を求める.
		GetLngLatOsm getLngLatOsmFocus = new GetLngLatOsm(new Point2D.Double(DEFAULT_LNG, DEFAULT_LAT), FOCUS_SCALE, new Point(WINDOW_WIDTH, WINDOW_HEIGHT));
		GetLngLatOsm getLngLatOsmContext = new GetLngLatOsm(new Point2D.Double(DEFAULT_LNG, DEFAULT_LAT), CONTEXT_SCALE, new Point(WINDOW_WIDTH, WINDOW_HEIGHT));
		// contextとglueの端点の緯度経度の差.
		Point2D.Double diffLngLat = new Point2D.Double(
				Math.abs(getLngLatOsmFocus._upperLeftLngLat.getX() - getLngLatOsmContext._upperLeftLngLat.getX()), 
				Math.abs(getLngLatOsmFocus._upperLeftLngLat.getY() - getLngLatOsmContext._upperLeftLngLat.getY())
				);
		// 緯度経度とXy座標の変換用インスタンス.(左上の緯度経度，右下の緯度経度，地図の大きさ(pixel))
//		_convert = new ConvertLngLatXyCoordinate((Point2D.Double)getLngLatOsm._upperLeftLngLat,
//				(Point2D.Double)getLngLatOsm._lowerRightLngLat, new Point(1000, 1000));
		__concentricCircleScaleArray = new ArrayList<>();
		for(int i=0; i<GLUE_SCALE_NUM; i++){
			// 放射方向の倍率(r).
			double radiationDirectionScale = (double)(GLUE_OUTER_RADIUS-GLUE_INNER_RADIUS)/(GLUE_OUTER_RADIUS-GLUE_INNER_RADIUS/Math.pow(2, FOCUS_SCALE-CONTEXT_SCALE));
			// 同心円方向の倍率(θ).
			double concentricCircleScale = (double)(GLUE_OUTER_RADIUS-(GLUE_INNER_RADIUS+i))/(GLUE_OUTER_RADIUS-GLUE_INNER_RADIUS)*Math.pow(2, FOCUS_SCALE-CONTEXT_SCALE)+
					(double)((GLUE_INNER_RADIUS+i)-GLUE_INNER_RADIUS)/(GLUE_OUTER_RADIUS-GLUE_INNER_RADIUS);
			__radiationDirectionScale = radiationDirectionScale;
			__concentricCircleScaleArray.add(concentricCircleScale);
			// ｘ方向 x=rcosθ.
//			double xScale = radiationDirectionScale*Math.cos(concentricCircleScale);
			// y方向 y=rsinθ.
//			double yScale = radiationDirectionScale*Math.sin(concentricCircleScale);
			System.out.println(radiationDirectionScale);
			System.out.println(concentricCircleScale);
//			arrayConvert.add(
//				new ConvertLngLatXyCoordinate(
//					new Point2D.Double(
//						// 同心円方向のスケールS_GC(x) = (b-x)/(b-a)*M*S_C+(x-a)/(b-a)*S_C.
//							(double)(GLUE_OUTER_RADIUS-(GLUE_INNER_RADIUS+i))/(GLUE_OUTER_RADIUS-GLUE_INNER_RADIUS)*Math.pow(2, FOCUS_SCALE-CONTEXT_SCALE)*getLngLatOsmContext._onePixelLngLat.getX()+
//						(double)((GLUE_INNER_RADIUS+i)-GLUE_INNER_RADIUS)/(GLUE_OUTER_RADIUS-GLUE_INNER_RADIUS)*getLngLatOsmContext._onePixelLngLat.getX(),
//						// 放射方向のスケール S_GR = (b-a)/(b-a/M)*S_C.
//						getLngLatOsmContext._onePixelLngLat.getY()
//					),
//					DEFAULT_LNGLAT,
//					new Point(WINDOW_WIDTH,WINDOW_HEIGHT),
//					true
//				)
//			);
			
//			System.out.println("SGC   : "+((double)(GLUE_OUTER_RADIUS-(GLUE_INNER_RADIUS+i))/(GLUE_OUTER_RADIUS-GLUE_INNER_RADIUS)*Math.pow(2, FOCUS_SCALE-CONTEXT_SCALE)+
//					(double)((GLUE_INNER_RADIUS+i)-GLUE_INNER_RADIUS)/(GLUE_OUTER_RADIUS-GLUE_INNER_RADIUS)));
//			System.out.println("S_GR  : "+((double)(GLUE_OUTER_RADIUS-GLUE_INNER_RADIUS)/(GLUE_OUTER_RADIUS-GLUE_INNER_RADIUS/Math.pow(2, FOCUS_SCALE-CONTEXT_SCALE))));
		}
//		System.exit(0);
		long end = System.currentTimeMillis();
		return arrayConvert;
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
		OsmRoadDataGeom osmRoadDataGeom = new OsmRoadDataGeom();
		osmRoadDataGeom.startConnection();
		Point p1Xy;
		Point p2Xy;
		
		// todo.
		// 地理座標系でなく投影座標系で考える(UTMなど).
		// ->長さをメートルで考えられる.
		
		for(ArrayList<Line2D> arrArc : __arc){
			for(Line2D arc : arrArc){
				// 2点の緯度経度から中心までの距離(メートル)を求める.
				double p1Meter = osmRoadDataGeom.calcMeterLength(DEFAULT_LNGLAT, arc.getP1());
				double p2Meter = osmRoadDataGeom.calcMeterLength(DEFAULT_LNGLAT, arc.getP2());
				// 2点の緯度経度ぞれぞれの中心からの角度(ラジアン)を求める(3時の方角から反時計回り).
				double p1angle = osmRoadDataGeom.calcAzimath(DEFAULT_LNGLAT, arc.getP1());
				double p2angle = osmRoadDataGeom.calcAzimath(DEFAULT_LNGLAT,  arc.getP2());
				// p1について.
				if(p1Meter < glueInnerRadiusMeter){	// focus領域にある.
					p1Xy = _convertFocus.convertLngLatToXyCoordinate(arc.getP1());
					continue;
				}else if ( glueInnerRadiusMeter < p1Meter && p1Meter < glueOuterRadiusMeter){// glue領域にある.
					// glue内側から見て何パーセントの位置にあるか(0~1).
					double glueRatio = ((p1Meter-glueInnerRadiusMeter)/(glueOuterRadiusMeter - glueInnerRadiusMeter));
					// glueの外側から見て何パーセントの位置にあるか.
					double glueOuterRatio = (((glueOuterRadiusMeter - glueInnerRadiusMeter)-(p1Meter-glueInnerRadiusMeter))/(glueOuterRadiusMeter - glueInnerRadiusMeter));
					// contextのxy座標.
					p1Xy = _convertContext.convertLngLatToXyCoordinate(arc.getP1());
					// 中心を原点とした平面座標(y軸は上向き).
					Point p1Plane = new Point((int)(p1Xy.getX()-WINDOW_WIDTH/2), -1* (int)(p1Xy.getY()-WINDOW_HEIGHT/2));
					// 極座標系に変換.
					Point2D p1Polar = ConvertPolarPlaneCoordinate.convertPlaneToPolar(p1Plane);
					// 変形.
					// ｘは２(focus)～１(context)の間.
					p1Polar = new Point2D.Double(p1Polar.getX(), p1Polar.getY());
//					p1Polar = new Point2D.Double(p1Polar.getX()*__concentricCircleScaleArray.get((int)(glueRatio*GLUE_SCALE_NUM)), p1Polar.getY());
//					p1Polar = new Point2D.Double(p1Polar.getX()*1, p1Polar.getY());
					// 極座標系から原点を中心とした平面座標へと変換.
					p1Plane = ConvertPolarPlaneCoordinate.convertPolarToPlane(p1Polar);
					// 左上を原点としたxy座標へと変換(y軸は下向き).
					p1Xy = new Point(p1Plane.x+WINDOW_WIDTH/2, (-1 * p1Plane.y+WINDOW_HEIGHT/2)); 
//					System.out.println(glueRatio);
					System.out.println(glueOuterRatio);
//					System.out.println();
//					System.out.println(((__concentricCircleScaleArray.get((int)(glueRatio*GLUE_SCALE_NUM))*-1)+3));
					System.out.println(p1Xy.getX()+"  a  "+p1Xy.getY());
//					p1Xy = _arrayConvert.get((int)glueRatio*GLUE_SCALE_NUM).convertLngLatToXyCoordinate(arc.getP1());
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
					double glueRatio = ((p2Meter-glueInnerRadiusMeter)/(glueOuterRadiusMeter - glueInnerRadiusMeter));
					// glueの外側から見て何パーセントの位置にあるか.
					double glueOuterRatio = (((glueOuterRadiusMeter - glueInnerRadiusMeter)-(p2Meter-glueInnerRadiusMeter))/(glueOuterRadiusMeter - glueInnerRadiusMeter));
					p2Xy = _convertContext.convertLngLatToXyCoordinate(arc.getP2());
					// 中心を原点とした平面座標(y軸は上向き).
					Point p2Plane = new Point((int)(p2Xy.getX()-WINDOW_WIDTH/2), -1* (int)(p2Xy.getY()-WINDOW_HEIGHT/2));
					// 極座標系に変換.
					Point2D p2Polar = ConvertPolarPlaneCoordinate.convertPlaneToPolar(p2Plane);
					// 変形.
					p2Polar = new Point2D.Double(p2Polar.getX(), p2Polar.getY());
//					p2Polar = new Point2D.Double(p2Polar.getX()*__concentricCircleScaleArray.get((int)(glueRatio*GLUE_SCALE_NUM)), p2Polar.getY());
//					p2Polar = new Point2D.Double(p2Polar.getX()*1, p2Polar.getY());
					// 極座標系から原点を中心とした平面座標へと変換.
					p2Plane = ConvertPolarPlaneCoordinate.convertPolarToPlane(p2Polar);
					// 左上を原点としたxy座標へと変換(y軸は下向き).
					p2Xy = new Point(p2Plane.x+WINDOW_WIDTH/2, (-1 * p2Plane.y+WINDOW_HEIGHT/2)); 

//					p2Xy = _arrayConvert.get((int)glueRatio*GLUE_SCALE_NUM).convertLngLatToXyCoordinate(arc.getP2());
//					continue;
				}else{// context領域にある.
					p2Xy = _convertContext.convertLngLatToXyCoordinate(arc.getP2());
					continue;
				}
				System.out.println("drawLine");
				paint2dLine(new Line2D.Double(p1Xy, p2Xy), Color.pink, (float)3);
			}
		}
		osmRoadDataGeom.endConnection();
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


}

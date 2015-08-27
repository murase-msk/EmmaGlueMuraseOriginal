package servlet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
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
import src.db.getData.OsmLineDataGeom;
import src.db.getData.OsmRoadDataGeom;

/**
 * 伸縮する道路を描画
 * http://133.68.13.112:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawElasticRoad&centerLngLat=136.9309671669116,35.15478942665804&focus_zoom_level=17&context_zoom_level=15&glue_inner_radius=200&glue_outer_radius=300&roadType=car
 * @author murase
 *
 */
public class DrawElasticRoad {
	
	/** 地図の大きさ */
	public Point windowSize = new Point(700, 700);
	/** 初期の緯度経度Point2D形式 */
	private  Point2D.Double centerLngLat = new Point2D.Double(136.9309671669116, 35.15478942665804);// 鶴舞公園.
	/** focusのスケール */
	private int focusScale = 17;
	/** contextのスケール */
	private int contextScale = 15;
	/** glue内側の半径(pixel) */
	private int glueInnerRadius=200;
	/** glue外側の半径(pixel) */
	private int glueOuterRadius=300;
	
	/** 道路の種類(car, bikeFoot) */
	public String roadType = "car";
	
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
	
	
	//http://133.68.13.112:8080/EmmaGlueMuraseOriginal/MainServlet?type=DrawElasticRoad&centerLngLat=136.9309671669116,35.15478942665804&focus_zoom_level=17&context_zoom_level=15&glue_inner_radius=200&glue_outer_radius=300
	public DrawElasticRoad(HttpServletRequest request, HttpServletResponse response) {
		// 必須パラメータがあるか.
		if(request.getParameter("centerLngLat")==null ||
				request.getParameter("focus_zoom_level")==null ||
				request.getParameter("context_zoom_level")==null ||
				request.getParameter("glue_inner_radius")==null ||
				request.getParameter("glue_outer_radius")==null
				){
			ErrorMsg.errorResponse(request, response, "必要なパラメータがありません");
			return;
		}
		// パラメータの受け取り.
		centerLngLat = new Point2D.Double(
				Double.parseDouble(request.getParameter("centerLngLat").split(",")[0]), 
				Double.parseDouble(request.getParameter("centerLngLat").split(",")[1]));
		focusScale = Integer.parseInt(request.getParameter("focus_zoom_level"));
		contextScale = Integer.parseInt(request.getParameter("context_zoom_level"));
		glueInnerRadius = Integer.parseInt(request.getParameter("glue_inner_radius"));
		glueOuterRadius = Integer.parseInt(request.getParameter("glue_outer_radius"));
		windowSize = new Point(glueOuterRadius*2, glueOuterRadius*2);
		roadType = request.getParameter("roadType") == null  ? "car" : request.getParameter("roadType");
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
		bfImage=new BufferedImage( windowSize.x, windowSize.y, BufferedImage.TYPE_INT_ARGB);
		_graphics2d = (Graphics2D) bfImage.getGraphics();
		_graphics2d.setBackground(new Color(241,238,232));	// 背景指定.
		_graphics2d.clearRect(0, 0, windowSize.x, windowSize.y);
		// アンチエイリアス設定：遅いときは次の行をコメントアウトする.
		_graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		//focus用の緯度経度xy変換
		_getLngLatOsmFocus = new GetLngLatOsm(centerLngLat, focusScale, windowSize);
		_convertFocus = new ConvertLngLatXyCoordinate((Point2D.Double)_getLngLatOsmFocus._upperLeftLngLat,
				(Point2D.Double)_getLngLatOsmFocus._lowerRightLngLat, windowSize);
		//context用の緯度経度xy変換
		_getLngLatOsmContext = new GetLngLatOsm(centerLngLat, contextScale, windowSize);
		_convertContext = new ConvertLngLatXyCoordinate((Point2D.Double)_getLngLatOsmContext._upperLeftLngLat,
				(Point2D.Double)_getLngLatOsmContext._lowerRightLngLat, windowSize);
		glueInnerRadiusMeter = glueInnerRadius*_convertFocus.meterPerPixel.getX();
		glueOuterRadiusMeter = glueOuterRadius*_convertContext.meterPerPixel.getX();
		// contextでのメルカトル座標系xy変換.
		_contextMercatorConvert = new ConvertMercatorXyCoordinate(
				LngLatMercatorUtility.ConvertLngLatToMercator((Point2D.Double)_getLngLatOsmContext._upperLeftLngLat), 
				LngLatMercatorUtility.ConvertLngLatToMercator((Point2D.Double)_getLngLatOsmContext._lowerRightLngLat), windowSize);
		
		
		// 道路データの取得.
		OsmRoadDataGeom osmRoadDataGeom = new OsmRoadDataGeom();
		osmRoadDataGeom.startConnection();
		//////////////////////////////////
		// 道路データを取得する.///////////////
		//////////////////////////////////
		osmRoadDataGeom.insertOsmRoadData(_getLngLatOsmContext._upperLeftLngLat, _getLngLatOsmContext._lowerRightLngLat, roadType);
		// 道路の描画.
		paintElasticRoadData(osmRoadDataGeom._arc2, osmRoadDataGeom._clazz);
		//////////////////////////////////
		// 鉄道データの取得.//////////////////
		//////////////////////////////////
		osmRoadDataGeom.insertOsmRoadData(_getLngLatOsmContext._upperLeftLngLat, _getLngLatOsmContext._lowerRightLngLat, "rail");
		// 鉄道の描画.
		paintElasticRoadData(osmRoadDataGeom._arc2, osmRoadDataGeom._clazz);
		osmRoadDataGeom.endConnection();
		
		// 地下鉄の取得.
		OsmLineDataGeom osmLineDataGeom = new OsmLineDataGeom();
		osmLineDataGeom.startConnection();
		osmLineDataGeom.insertLineDataSpecificColumn("railway", "subway", _getLngLatOsmContext._upperLeftLngLat, _getLngLatOsmContext._lowerRightLngLat);
		osmLineDataGeom.endConnection();
		paintElasticRoadData(osmLineDataGeom._arc, osmLineDataGeom._clazz);
		
		BasicStroke wideStroke = new BasicStroke(3);
		_graphics2d.setStroke(wideStroke);
		// glueの枠線の描画.
		_graphics2d.setColor(Color.red);
		// 中心点.
		_graphics2d.drawOval(windowSize.x/2-2, windowSize.x/2-2, 4, 4);
		// glue領域内側想定範囲.
		_graphics2d.drawOval(windowSize.x/2-glueInnerRadius, windowSize.x/2-glueInnerRadius, glueInnerRadius*2, glueInnerRadius*2);
		// glue領域外側想定範囲.
		_graphics2d.drawOval(windowSize.x/2-glueOuterRadius, windowSize.x/2-glueOuterRadius, glueOuterRadius*2, glueOuterRadius*2);
		
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
	public void paintElasticRoadData(ArrayList<ArrayList<Point2D>> aPointSeries, ArrayList<Integer> __clazz){
		
		ArrayList<ArrayList<Point2D>> bluePath = new ArrayList<>(); 
		ArrayList<ArrayList<Point2D>> greenPath = new ArrayList<>();
		ArrayList<ArrayList<Point2D>> redPath = new ArrayList<>();
		ArrayList<ArrayList<Point2D>> orangePath = new ArrayList<>();
		ArrayList<ArrayList<Point2D>> yellowPath = new ArrayList<>();
		ArrayList<ArrayList<Point2D>> whitePath = new ArrayList<>();
		ArrayList<ArrayList<Point2D>> railPath = new ArrayList<>();
		ArrayList<ArrayList<Point2D>> subwayPath = new ArrayList<>();
		
		// 点を歪める準備.
		ElasticPoint elasticPoint = new ElasticPoint(
				_contextMercatorConvert.mercatorPerPixel.getX()*glueInnerRadius, 
				_contextMercatorConvert.mercatorPerPixel.getX()*glueOuterRadius, 
				Math.pow(2, focusScale-contextScale), 
				LngLatMercatorUtility.ConvertLngLatToMercator(centerLngLat));
		
		for(int i=0; i<aPointSeries.size(); i++){
			Point pXy;// あるセグメントにおける点.
			ArrayList<Point2D> path = new ArrayList<>();	// .
			for(Point2D onePoint : aPointSeries.get(i)){
				// 2点の緯度経度から中心までの距離(メートル)を求める.
				double pMeter = LngLatMercatorUtility.calcDistanceFromLngLat(centerLngLat, onePoint);
				// p1について.
				if(pMeter < glueInnerRadiusMeter){	// focus領域にある.
					pXy = _convertFocus.convertLngLatToXyCoordinate(onePoint);
				}else if ( glueInnerRadiusMeter < pMeter && pMeter < glueOuterRadiusMeter){// glue領域にある.
					// glue内側から見て何パーセントの位置にあるか(0~1).
					double glueRatio = (pMeter-glueInnerRadiusMeter)/(glueOuterRadiusMeter - glueInnerRadiusMeter);
					Point2D elasticPointMercator = elasticPoint.calcElasticPoint(LngLatMercatorUtility.ConvertLngLatToMercator(onePoint), glueRatio);
					pXy = _contextMercatorConvert.convertMercatorToXyCoordinate(elasticPointMercator);
				}else{// context領域にある.
					pXy = _convertContext.convertLngLatToXyCoordinate(onePoint);
				}
				path.add(pXy);
//				paint2dLine(new Line2D.Double(p1Xy, p2Xy), Color.pink, (float)3, __clazz.get(i));
			}
			
			dispatchLineStyle(path, __clazz.get(i), bluePath, greenPath, redPath, orangePath, yellowPath, whitePath, railPath, subwayPath);
		}
		
		paintEachLineStyle(bluePath, greenPath, redPath, orangePath, yellowPath, whitePath, railPath, subwayPath);
	}

	/**
	 * 線の描画形式を道路クラスによって振り分ける.
	 * @param path
	 * @param __clazz
	 */
	private void dispatchLineStyle(ArrayList<Point2D> path, int __clazz, 
			ArrayList<ArrayList<Point2D>> bluePath, 
			ArrayList<ArrayList<Point2D>> greenPath, 
			ArrayList<ArrayList<Point2D>> redPath, 
			ArrayList<ArrayList<Point2D>> orangePath, 
			ArrayList<ArrayList<Point2D>> yellowPath,
			ArrayList<ArrayList<Point2D>> whitePath,
			ArrayList<ArrayList<Point2D>> railPath,
			ArrayList<ArrayList<Point2D>> subwayPath
			){

		switch(__clazz){
			// 水色.
			case 11:
			case 12:
				bluePath.add(path);
				break;
				//緑.
			case 13:
			case 14:
				greenPath.add(path);
				break;
				// 赤.
			case 15:
			case 16:
				redPath.add(path);
				break;
				// オレンジ.
			case 21:
			case 22:
				orangePath.add(path);
				break;
				// 黄色.
			case 31:
				yellowPath.add(path);
				break;
				// 白.
			case 32:
			case 41:
			case 42:
			case 51:
			case 63:
				whitePath.add(path);
				break;			
				// 鉄道 白黒.
			case 3:
				railPath.add(path);
				break;
				// 地下鉄.
			case 2:
				subwayPath.add(path);
				break;
			default:
				whitePath.add(path);
				break;
		}
	}
	
	/**
	 * 道路クラスが大きい順に道路クラスごとに描画
	 * @param bluePath
	 * @param greenPath
	 * @param redPath
	 * @param orangePath
	 * @param yellowPath
	 * @param whitePath
	 */
	private void paintEachLineStyle(
			ArrayList<ArrayList<Point2D>> bluePath, 
			ArrayList<ArrayList<Point2D>> greenPath, 
			ArrayList<ArrayList<Point2D>> redPath, 
			ArrayList<ArrayList<Point2D>> orangePath, 
			ArrayList<ArrayList<Point2D>> yellowPath,
			ArrayList<ArrayList<Point2D>> whitePath,
			ArrayList<ArrayList<Point2D>> railPath,
			ArrayList<ArrayList<Point2D>> subwayPath
		){
		// 道路クラスごとに描画.
		for(ArrayList<Point2D> whiteLine: whitePath){
			paintPath(whiteLine, 8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, new Color(189,189,188));// 枠.
			paintPath(whiteLine, 7, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_ROUND, new Color(255,255,255));// 中の線.
		}
		for(ArrayList<Point2D>yellowLine: yellowPath){
			paintPath(yellowLine, 8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, new Color(153,153,0));
			paintPath(yellowLine, 7, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_ROUND, new Color(248,248,186));
		}
		for(ArrayList<Point2D>orangeLine: orangePath){
			paintPath(orangeLine, 8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, new Color(204,102,0));
			paintPath(orangeLine, 7, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_ROUND, new Color(248,214,170));
		}
		for(ArrayList<Point2D>redLine: redPath){
			paintPath(redLine, 8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, new Color(211,147,148));
			paintPath(redLine, 7, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_ROUND, new Color(220,158,158));
		}
		for(ArrayList<Point2D>greenLine: greenPath){
			paintPath(greenLine, 8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, new Color(0,153,0));
			paintPath(greenLine, 7, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_ROUND, new Color(148,211,148));
		}
		for(ArrayList<Point2D>blueLine: bluePath){
			paintPath(blueLine, 8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, new Color(40,40,40));
			paintPath(blueLine, 7, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_ROUND, new Color(137,163,202));
		}
		for(ArrayList<Point2D>railLine: railPath){
			paintPath(railLine, 4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, new Color(255,255,255));
			float[] dash = { 19.f, 19.f };
			paintPath(railLine, 4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, new Color(164, 164, 164), (float) 1., dash, (float) 0.);
		}
		for(ArrayList<Point2D>subwayLine: subwayPath){
			float[] dash = { 6.f, 6.f };
			paintPath(subwayLine, 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, new Color(153, 153, 153), (float) 1., dash, (float) 0.);
		}
	}
	
	/**
	 * パスの描画
	 * @param path
	 * @param aPathWidth
	 * @param aColor
	 */
	private void paintPath(ArrayList<Point2D> path,int aPathWidth, int CAP_BUTT,int JOIN_BEVEL, Color aColor){
		_graphics2d.setStroke(new BasicStroke(aPathWidth, CAP_BUTT, JOIN_BEVEL));
		drawPath(path, aPathWidth, aColor);
	}
	private void paintPath(ArrayList<Point2D> path,int aPathWidth, int CAP_BUTT,int JOIN_BEVEL, Color aColor, float miterlimit, float[] dash, float dash_phase){
		_graphics2d.setStroke(new BasicStroke(aPathWidth, CAP_BUTT, JOIN_BEVEL, miterlimit, dash, dash_phase));
		drawPath(path, aPathWidth, aColor);
	}
	private void drawPath(ArrayList<Point2D> path,int aPathWidth, Color aColor){
		_graphics2d.setPaint(aColor);
		GeneralPath generalPath = new GeneralPath();
		for(int i=0; i<path.size(); i++){
			if(i==0){
				generalPath.moveTo(path.get(i).getX(), path.get(i).getY());
			}
			generalPath.lineTo(path.get(i).getX(), path.get(i).getY());
		}
		_graphics2d.draw(generalPath);

	}
	
	////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////

}

package src.coordinate;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import src.db.getData.OsmRoadDataGeom;

/**
 * メルカトル座標系と画面上のxy座標変換に関するクラス.
 * 
 * <br>
 * サンプルコード<br>
 * <div style="border-style: solid ; border-width: 1px; border-radius: 10px; box-shadow: 5px 5px 5px #AAA;">
 * <pre>{@code
 * ConvertMercatorXyCoordinate convert = new ConvertMercatorXyCoordinate(
 * LngLatMercatorUtility.ConvertLngLatToMercator((Point2D.Double)_getLngLatOsmContext._upperLeftLngLat), 
 * LngLatMercatorUtility.ConvertLngLatToMercator((Point2D.Double)_getLngLatOsmContext._lowerRightLngLat), windowSize);
 * メルカトル座標からxy座標に変換
 * Point outputXy = convert.convertMercatorToXyCoordinate(mercator);
 * xy座標からメルカトル座標に変換
 * Point outputMercator = convert.convertXyCoordinateToMercator(xy);
 * }</pre>
 * </div>
 * 
 * @author murase
 *
 */
public class ConvertMercatorXyCoordinate {
	
	/** 左上の球面メルカトル座標. */
	private Point2D _upperLeftMercator;
	/** 右下の球面メルカトル座標. */
	private Point2D _lowerRightMercator;
	/** ウインドウサイズ */
	private Point _windowSize;
	
	/** 球面メルカトル座標1あたりのピクセル数 */
	public Point2D pixelPerMercator;
	/** 1ピクセルあたりの球面メルカトル座標の増加幅 */
	public Point2D mercatorPerPixel;
	
	/**
	 * コンストラクタ，初期設定をする
	 * @param aUpperLeftMercator 左上のメルカトル座標
	 * @param aLowerRightMercator 右下のメルカトル座標
	 * @param aWindowSize 画面の大きさ(縦横　ピクセル数)
	 */
	public ConvertMercatorXyCoordinate(Point2D aUpperLeftMercator, Point2D aLowerRightMercator,
			Point aWindowSize) {
		_upperLeftMercator = aUpperLeftMercator;
		_lowerRightMercator = aLowerRightMercator;
		_windowSize = aWindowSize;
		mercatorPerPixel = new Point2D.Double(Math.abs(_upperLeftMercator.getX()-_lowerRightMercator.getX())/(_windowSize.getY())
		,Math.abs(_upperLeftMercator.getY()-_lowerRightMercator.getY())/(_windowSize.getY()));
		pixelPerMercator = new Point2D.Double((_windowSize.getY()/Math.abs(_upperLeftMercator.getX()-_lowerRightMercator.getX()))
				,(_windowSize.getY())/Math.abs(_upperLeftMercator.getY()-_lowerRightMercator.getY()));
		OsmRoadDataGeom osmRoadDataGeom = new OsmRoadDataGeom();
	}
	
	/**
	 * メルカトル座標からxy内座標に変換.
	 * @param aMercator メルカトル座標
	 * @return　xy座標
	 */
	public Point convertMercatorToXyCoordinate(Point2D aMercator){
		double width = _lowerRightMercator.getX() - _upperLeftMercator.getX();	// 右端から左端までの経度の差（経度であらわされている）.
		double hight = _upperLeftMercator.getY() - _lowerRightMercator.getY() ;	// 上端から下端までの緯度の差（緯度であらわされている）.
		double widthBase = _windowSize.x/width;				// widthBaseの逆数がxy1ドットあたりの経度の増加幅.
		double heightBase = _windowSize.y/hight;			// heightBaseの逆数がxy1ドットあたりの緯度の増加幅.
		
		int xtoi = (int)((aMercator.getX() - _upperLeftMercator.getX())*widthBase);	// xy内のｘ座標.
		int ytoi = (int)((aMercator.getY() - _lowerRightMercator.getY())*heightBase);	// xy内のy座標.
		return(new Point(xtoi, _windowSize.y - ytoi));	// 戻り値のY軸は反転させる必要がある.
	}
	
	/**
	 * メルカトル座標からxy内座標に変換.
	 * @param aMercatorArray メルカトル座標
	 * @return　xy座標
	 */
	public ArrayList<Point> convertMercatorToXyCoordinate(ArrayList<Point2D> aMercatorArray){
		Point lnglat;
		ArrayList<Point> XyCoordinateArray = new ArrayList<Point>();;
		for (int i=0; i<aMercatorArray.size(); i++) {
			lnglat = convertMercatorToXyCoordinate(aMercatorArray.get(i));
			XyCoordinateArray.add(new Point(lnglat.x,lnglat.y));
		}
		return XyCoordinateArray;
	}
	
	/**
	 * xy内座標からメルカトル座標に変換
	 * @param aXyCoordinate　xy内座標
	 * @return メルカトル座標
	 */
	public Point2D.Double convertXyCoordinateToMercator(Point aXyCoordinate){
		//aXyCoordinate.y = MapPanel.WINDOW_HEIGHT - aXyCoordinate.y;	// Y軸の反転.
		int XyCoordinateX = aXyCoordinate.x;
		int XyCoordinateY = _windowSize.y - aXyCoordinate.y;	// Y軸の反転.
		double width = _lowerRightMercator.getX() - _upperLeftMercator.getX();	// 右端から左端までの経度の差（経度であらわされている）.
		double hight = _upperLeftMercator.getY() - _lowerRightMercator.getY() ;	// 上端から下端までの緯度の差（緯度であらわされている）.
		double widthBase = _windowSize.x/width;			// widthBaseの逆数がxy1ドットあたりの経度の増加幅.
		double heightBase = _windowSize.y/hight;		// heightBaseの逆数がxy1ドットあたりの緯度の増加幅.
		
		return(new Point2D.Double((XyCoordinateX/widthBase)+_upperLeftMercator.getX(),
				(XyCoordinateY/heightBase)+_lowerRightMercator.getY()));
	}
	
	/**
	 * xy内座標からメルカトル座標に変換.
	 * @param aXyCoordinateArray　xy座標
	 * @return メルカトル座標
	 */
	public ArrayList<Point2D> convertXyCoordinateToMercator(ArrayList<Point> aXyCoordinateArray){
		Point2D XyCoordinate;
		ArrayList<Point2D> lnglatArray = new ArrayList<Point2D>();
		for (int i=0; i<aXyCoordinateArray.size(); i++) {
			XyCoordinate = convertXyCoordinateToMercator(aXyCoordinateArray.get(i));
			lnglatArray.add(new Point2D.Double(XyCoordinate.getX(), XyCoordinate.getY()));
		}
		return lnglatArray;
	}
	
	/**
	 * メルカトル座標からxy内座標に変換.
	 * @param aLine2Double
	 * @return
	 */
	public Line2D convertMercatorToXyCoordinateLine2D(Line2D aLine2Double){
		Line2D line2d = new Line2D.Double(convertMercatorToXyCoordinate((Point2D)aLine2Double.getP1()),
				convertMercatorToXyCoordinate((Point2D)aLine2Double.getP2()));
		return line2d;
	}
	public ArrayList<Line2D> convertMercatorToXyCoordinateLine2D(ArrayList<Line2D> aLine2dArrayList){
		ArrayList<Line2D> line2dArrayList = new ArrayList<>();
		for(int i=0; i<aLine2dArrayList.size(); i++){
			line2dArrayList.add(convertMercatorToXyCoordinateLine2D(aLine2dArrayList.get(i)));
		}
		return line2dArrayList;
	}
	
	
}

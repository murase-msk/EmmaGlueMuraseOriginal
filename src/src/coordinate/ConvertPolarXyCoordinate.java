package src.coordinate;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * 極座標と左上を原点とした平面座標の変換
 * @author murase
 *
 */
public class ConvertPolarXyCoordinate {
	
	private Point _windowSize;
	
	public ConvertPolarXyCoordinate(Point aWindowSize) {
		_windowSize = aWindowSize;
	}
	
	
	/**
	 * xyから極座標変換
	 */
	public Point2D convertXyToPolar(Point aXy){
		// 極座標.
		Point2D polarCoordinate;
		// 原点を中心とした平面座標系に変換.
		Point planeCoordinate = new Point((int)(aXy.getX()+(_windowSize.getX()/2)), (int)(_windowSize.getX() - aXy.getY()+(_windowSize.getY()/2)));
		polarCoordinate = new Point2D.Double(Math.hypot(planeCoordinate.getX(), planeCoordinate.getY()), Math.atan2(planeCoordinate.getY(), planeCoordinate.getX()));
		return polarCoordinate;
	}
	
	
	/**
	 * 極座標からxy(左上を原点)
	 */
	public Point convertPolarToXy(Point2D aR_Theta){
		//原点を中心とした平面座標
		Point planeCoordinate = new Point((int)(aR_Theta.getX()*Math.cos(aR_Theta.getY())), (int)(aR_Theta.getX()*Math.sin(aR_Theta.getY())));
		Point xy = new Point((int)(planeCoordinate.getX()), (int)(_windowSize.getY() - planeCoordinate.getY()));
		return xy;
	}

}

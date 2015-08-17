package src.coordinate;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * 中心を原点とした極座標系と平面座標系の変換
 * @author murase
 *
 */
public class ConvertPolarPlaneCoordinate {

	private Point _windowSize;
	
	public ConvertPolarPlaneCoordinate(Point aWindowSize) {
		_windowSize = aWindowSize;
	}
	
	/**
	 * から極座標変換
	 */
	public static Point2D convertPlaneToPolar(Point aPlaneCoordinate){
		// 極座標.
		Point2D polarCoordinate;
		polarCoordinate = new Point2D.Double(Math.hypot(aPlaneCoordinate.getX(), aPlaneCoordinate.getY()), Math.atan2(aPlaneCoordinate.getY(), aPlaneCoordinate.getX()));
		return polarCoordinate;
	}
	
	
	/**
	 * 極座標から
	 */
	public static Point convertPolarToPlane(Point2D aR_Theta){
		//原点を中心とした平面座標
		Point planeCoordinate = new Point((int)(aR_Theta.getX()*Math.cos(aR_Theta.getY())), (int)(aR_Theta.getX()*Math.sin(aR_Theta.getY())));
		return planeCoordinate;
	}


}

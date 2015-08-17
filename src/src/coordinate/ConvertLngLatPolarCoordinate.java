package src.coordinate;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * 緯度経度と緯度経度の極座標系の変換
 * @author murase
 *
 */
public class ConvertLngLatPolarCoordinate {
	
	private Point2D _centerLngLat;
	
	public ConvertLngLatPolarCoordinate(Point2D aCenterLngLat){
		_centerLngLat = aCenterLngLat;
	}
	
	
	public Point2D convertLngLatToPolar(Point2D aLngLat){
		Point2D polar = new Point2D.Double();
		return polar;
		
	}
	
	public Point2D convertPolarToLngLat(Point2D aPolar){
		Point2D lnglat = new Point2D.Double();
		return lnglat;
	}
	
	
}

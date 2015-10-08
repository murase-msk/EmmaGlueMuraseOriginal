package src;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;

import  src.db.getData.OsmStrokeDataGeom;
import sun.launcher.resources.launcher;
import  servlet.DrawElasticStrokeConnectivity_v2;
import src.coordinate.LngLatMercatorUtility;

/**
 * コネクティビティーを保つためのクラス
 * v2 PostGISを使わないようにする
 * @author murase
 *
 */
public class ConnectivityAlgorithm_v2 {
	
	/** 最大何本ストロークを描画するか */
	public static final int MAX_DRAW_NUM = 20;
	
	
	// 最初に必要な変数.
	/** データベースからそのまま取り出したストローク(arc形式) */
	public ArrayList<ArrayList<Line2D>> _strokeArc = new ArrayList<>();
	public ArrayList<ArrayList<Point2D>> _strokeArcPoint = new ArrayList<>();
	/** ストロークのWKT形式 */
	public ArrayList<String> _strokeArcString = new ArrayList<>();
	/** ストロークの長さ */
	public ArrayList<Double> _strokeLength = new ArrayList<>();
	/** ストロークIDからインデックスを求めるハッシュ */
	public HashMap<Integer, Integer> _strokeIdToIndexHash = new HashMap<>();
	/** ストロークのクラス */
	public ArrayList<Integer> _strokeClazz = new ArrayList<>();
	
	/** 中心点からglue内側の長さ(メートル) */
	public double _glueInnerRadiusMeter;
	/** 中心点からglue外側の長さ(メートル)  */
	public double _glueOuterRadiusMeter;
//	private Point2D _upperLeftLngLat;
//	private Point2D _lowerRightLngLat;
	private  Point2D _centerLngLat;
	
	
	// 中間処理で使う.
	/** 描画が確定していないストロークのインデックス */
	public ArrayList<Integer> _reserveQueue = new ArrayList<>();
	// 最終結果.
	/** 描画が確定したストロークのインデックス */
	public ArrayList<Integer> _selectedStrokeIndex = new ArrayList<Integer>();

	
	
	public ConnectivityAlgorithm_v2(OsmStrokeDataGeom aOsmStrokeDataGeom, DrawElasticStrokeConnectivity_v2 DrawElasticStrokeConnectivity_v2){
		initSetting(aOsmStrokeDataGeom, DrawElasticStrokeConnectivity_v2);
		selectDrawingStroke();
	}
	
	public void initSetting(OsmStrokeDataGeom aOsmStrokeDataGeom, DrawElasticStrokeConnectivity_v2 DrawElasticStrokeConnectivity_v2){
		_strokeArc = aOsmStrokeDataGeom._strokeArc;
		_strokeArcPoint = aOsmStrokeDataGeom._strokeArcPoint;
		_strokeArcString = aOsmStrokeDataGeom._strokeArcString;
		_strokeLength = aOsmStrokeDataGeom._strokeLength;
		_strokeIdToIndexHash = aOsmStrokeDataGeom._strokeIdToIndexHash;
		_strokeClazz = aOsmStrokeDataGeom._strokeClazz;
		_glueInnerRadiusMeter = DrawElasticStrokeConnectivity_v2.glueInnerRadiusMeter;
		_glueOuterRadiusMeter = DrawElasticStrokeConnectivity_v2.glueOuterRadiusMeter;
		_centerLngLat = DrawElasticStrokeConnectivity_v2.centerLngLat;
	}
	
	/**
	 * 描画するストロークを選択する
	 */
	public void selectDrawingStroke(){
		
		_selectedStrokeIndex = new ArrayList<>();
		_reserveQueue = new ArrayList<>();
		
		// ストロークの重要度が高い順に順に調べる.
		for(int i=0; i<_strokeArcString.size(); i++){
			System.out.println("oneStroke");
			if(
				isStepOverFromFocusToContext(_centerLngLat, _glueInnerRadiusMeter, _glueOuterRadiusMeter, _strokeArcPoint.get(i)) || // focus-contextを直接またぐストロークである.
				isConnectedIndirect(_strokeArcPoint.get(i))// focus-contextを間接的にまたぐストロークである.
			){
				_selectedStrokeIndex.add(i);
				System.out.println("add");
				if(_selectedStrokeIndex.size() > MAX_DRAW_NUM) break;	// 指定した数だけ描画したら終了.
				// 新しく描画か確定したストロークと保留キューにあるストロークが交差するか確かめる.
				func(_strokeArcPoint.get(i));
			}else{
				_reserveQueue.add(i);
			}
		}
	}
	
	/**
	 *  新しく描画か確定したストロークと保留キューにあるストロークが交差するか確かめる.
	 * @param osmStrokeDataGeom
	 * @param strokeString 新しく描画が確定したストローク
	 */
	public void func(ArrayList<Point2D> aStrokeArcPoint){
		// 新しく描画か確定したストロークと保留キューにあるストロークが交差するか確かめる.
		for(int j=0; j<_reserveQueue.size(); j++){
			if(isIntersectTwoStroke(aStrokeArcPoint, _strokeArcPoint.get(_reserveQueue.get(j)))){
				_selectedStrokeIndex.add(_reserveQueue.get(j));
				//System.out.println("func add");
				if(_selectedStrokeIndex.size() > MAX_DRAW_NUM) break;// 指定した数だけ描画したら終了.
				_reserveQueue.remove(j);
				j--;
				func(_strokeArcPoint.get(_selectedStrokeIndex.get(_selectedStrokeIndex.size()-1)));	// 再帰で呼び出す.
			}
		}
	}
	
	
	/**
	 * 指定したストロークがfocus-contextをまたぐか
	 * @param aCenterLngLat
	 * @param aGlueInnerRadiusMeter
	 * @param aGlueOuterRadiusMeter
	 * @param aStrokeArcPoint
	 * @return
	 */
	public boolean isStepOverFromFocusToContext(Point2D aCenterLngLat, double aGlueInnerRadiusMeter, double aGlueOuterRadiusMeter,
			ArrayList<Point2D> aStrokeArcPoint){
		
		boolean innerFlg = false, outerFlg = false;
		for(Point2D item: aStrokeArcPoint){
			if(isInCircle(aCenterLngLat, item, aGlueInnerRadiusMeter)){	// focusの内側にあるか.
				innerFlg = true;
			}
			if(!isInCircle(aCenterLngLat, item, aGlueOuterRadiusMeter)){// contextの外側にあるか.
				outerFlg = true;
			}
			if((innerFlg == true) && (outerFlg == true)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 間接的にまたぐストロークであるか
	 * @param aStrokeArcPoint 交差するか調べたいストローク
	 * @return
	 */
	public boolean isConnectedIndirect(ArrayList<Point2D> aStrokeArcPoint){
		for(int i=0; i<_selectedStrokeIndex.size(); i++){
			if(isIntersectTwoStroke(aStrokeArcPoint, _strokeArcPoint.get(_selectedStrokeIndex.get(i)))){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 指定した円の中に点があるか
	 * @return
	 */
	public boolean isInCircle(Point2D p1, Point2D p2, double border){
		if (LngLatMercatorUtility.calcDistanceFromLngLat(p1, p2) < border){
			return true;
		}
		return false;
	}
	
	/**
	 * 2つのストロークが交差するか
	 * @param s1 _strokeArcPointの形
	 * @param s2 _strokeArcPointの形
	 * @return
	 */
	public boolean isIntersectTwoStroke(ArrayList<Point2D> s1, ArrayList<Point2D> s2){
		
		// 端点で交差するか確かめる.
		if(
			(s1.get(0).getX() == s2.get(0).getX() && s1.get(0).getY() == s2.get(0).getY())||
			(s1.get(0).getX() == s2.get(s2.size()-1).getX() && s1.get(0).getY() == s2.get(s2.size()-1).getY())||
			(s1.get(s1.size()-1).getX() == s2.get(0).getX() && s1.get(s1.size()-1).getY() == s2.get(0).getY())||
			(s1.get(s1.size()-1).getX() == s2.get(s2.size()-1).getX() && s1.get(s1.size()-1).getY() == s2.get(s2.size()-1).getY())
		){
			return true;
		}
		
		// 中で交差するか確かめる.
		ArrayList<Line2D> sList = new ArrayList<>();
		for(int i=0; i<s1.size()-1; i=i+2){
			sList.add(new Line2D.Double(s1.get(i), s1.get(i+1)));
		}
		for(int i=0; i<s2.size()-1; i=i+2){
			sList.add(new Line2D.Double(s2.get(i), s2.get(i+1)));
		}
		return isIntersectLines(sList);
	}
	
	/**
	 * 複数の線分が交差しているか(端点でしか交差しないことを想定)
	 * @return
	 */
	public boolean isIntersectLines(ArrayList<Line2D> segmentList){
		HashSet<Double> hash = new HashSet<>();
		for(int i=0; i<segmentList.size(); i++){
			hash.add(segmentList.get(i).getX1());
			hash.add(segmentList.get(i).getX2());
		}
		
		if(hash.size() == segmentList.size()){
			return false;// 交差しない.
		}
		
		hash = new HashSet<>();
		for(int i=0; i<segmentList.size(); i++){
			hash.add(segmentList.get(i).getY2());
			hash.add(segmentList.get(i).getY2());
		}
		
		if(hash.size() == segmentList.size()){
			return false;// 交差しない.
		}
		
		return true;// 交差する.
	}
	
}

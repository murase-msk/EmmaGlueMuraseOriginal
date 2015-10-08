package servlet;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import servlet.sample.DrawSimpleRoad;
import servlet.sample.DrawSimplifiedStroke;
import servlet.sample.Test;



/**
 * メインのサーブレット
 * Servlet implementation class MainServlet
 */
@WebServlet(name="MainServlet",urlPatterns={"/MainServlet"})// このアノテーションでweb.xml不要になる.
public class MainServlet extends HttpServlet{

    /**
     * @see HttpServlet#HttpServlet()
     */
    public MainServlet() {
        super();
    }

    /**
	 * @see HttpServlet#HttpServlet()
	 * getリクエスト  http://localhost/projectName/MainServlet?
	 * type=...&.....
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// パラメータの受け取り.
		String type="";	// サーバのやること.
		
		System.out.println("postRequest");
		
		if(request.getParameter("type")==null){
			ErrorMsg.errorResponse(request, response, "typeパラメータがありません");
			return;
		}
		
		type = request.getParameter("type");
		switch(type){
		case "DrawElasticStrokeConnectivity":
			new DrawElasticStrokeConnectivity_v2(request, response);
			break;
		case "DrawElasticStroke":
			new DrawElasticStroke_v2(request, response);
			break;
		case "DrawElasticRoad":
			new DrawElasticRoad(request, response);
			break;
		case "DrawSimplifiedStroke":
			new DrawSimplifiedStroke(request, response);
		case "DrawSimpleRoad":
			new DrawSimpleRoad(request, response);
			break;
		case "Test":
			new Test(request, response);
			break;
		default:
			ErrorMsg.errorResponse(request, response, "typeパラメータの値が正しくありません");
			return;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 * postリクエスト
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}


}

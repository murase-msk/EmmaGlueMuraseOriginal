package servlet;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



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
			System.out.println("\"type\" parameter not found");
			return;
		}
		
		type = request.getParameter("type");
		switch(type){
		case "drawSimpleRoad" :
			new DrawSimpleRoad(request, response);
			break;
		case "Test":
			new Test(request, response);
			break;
		case "" :
			break;

		default:
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

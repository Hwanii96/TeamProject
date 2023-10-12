package controller;

import com.google.gson.Gson;
import model.ProductDAO;
import model.ProductVO;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet(value = "/fruitkha-1.0.0/filterSearch.do", asyncSupported = true)
public class FilterSearchController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public FilterSearchController() {
        super();

    }

    private void doAction(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        final AsyncContext asyncContext = request.startAsync();
        response = (HttpServletResponse) asyncContext.getResponse();
        response.setCharacterEncoding("UTF-8");

        try {
            ArrayList<ProductVO> pdatas = new ArrayList<ProductVO>();
            ProductVO pVO = new ProductVO();
            ProductDAO pDAO = new ProductDAO();

            //	사용자가 입력한 이름 키워드 값을 pVO 객체에 set.
            pVO.setpName(request.getParameter("pName"));

            //	이름 키워드 값을 가지고, DB 조회 하기.
            pdatas = pDAO.selectAll(pVO);

            // Gson 객체 생성
            Gson gson = new Gson();

            //	out 객체에 담아서 View 에게 보내기.
            PrintWriter out = response.getWriter();

            //	View로 상품의 데이터가 담긴 배열리스트 데이터 보내기.
            out.print(gson.toJson(pdatas));

        } catch (IOException e) {
            e.printStackTrace();
        }
        asyncContext.complete();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doAction(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doAction(request, response);
    }

}

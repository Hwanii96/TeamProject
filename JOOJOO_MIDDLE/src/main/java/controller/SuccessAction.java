package controller;

import model.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Set;

public class SuccessAction implements Action {

    @Override
    public ActionForward execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 결제 승인 API 호출하기
        ActionForward forward = null;
        String orderName = request.getParameter("orderName");
        String orderId = request.getParameter("orderId");
        String paymentKey = request.getParameter("paymentKey");
        int amount = Integer.parseInt(request.getParameter("amount"));
        String secretKey = "test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R:";

        Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode(secretKey.getBytes("UTF-8"));
        String authorizations = "Basic " + new String(encodedBytes, 0, encodedBytes.length);

        paymentKey = URLEncoder.encode(paymentKey, StandardCharsets.UTF_8);

        URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        JSONObject obj = new JSONObject();
        obj.put("orderName", orderName);
        obj.put("orderId", orderId);
        obj.put("paymentKey", paymentKey);
        obj.put("amount", amount);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(obj.toString().getBytes("UTF-8"));

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200 ? true : false;

        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
        JSONParser parser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            request.setAttribute("JSONObject", jsonObject);
            request.setAttribute("isSuccess", isSuccess);

            HttpSession session = request.getSession();  // 세션 객체 생성
            String mid = (String) session.getAttribute("mid"); // 세션에 저장된 사용자 아이디 값

            MemberVO mVO = new MemberVO();   // 사용자 객체
            MemberDAO mDAO = new MemberDAO();

            OrderVO oVO = new OrderVO();  // 구매 목록 객체
            OrderDAO oDAO = new OrderDAO();

            ProductVO pVO = new ProductVO();
            ProductDAO pDAO = new ProductDAO();

            OrderdetailVO odVO = new OrderdetailVO(); // 구매 상세정보 객체
            OrderdetailDAO odDAO = new OrderdetailDAO();

            // 구독 결제인지 확인 하기 위해 세션에서 subNum 값을 가져오기.
            // 세션에 subNum 값이 없고,
            if (session.getAttribute("subNum") == null) {
                // 세션에 cartFlag 값도 없으면 -> 일반 결제
                if (session.getAttribute("cartFlag") == null) {

                    String address = request.getParameter("address");
                    oVO.setmID(mid);   // 아이디.
                    oVO.setoAddress(address);  // 주소.
                    oVO.setoPrice(amount);   // 결제 총 금액.
                    oVO.setoState("결제 완료");  // 주문 상태.
                    oDAO.insert(oVO);  // 주문 테이블에 주문 정보 삽입.

                    oVO = oDAO.selectOne(oVO);  // 주문 번호를 가져오기 위한 장치
                    int pnum = (int) session.getAttribute("pnum");
                    pVO.setpNum(pnum);
                    pVO = pDAO.selectOne(pVO);
                    int cnt = amount / pVO.getpPrice();
                    odVO.setpNum(pnum);   // 상품 번호
                    odVO.setoNum(oVO.getoNum());   // 주문 번호
                    odVO.setOdCnt(cnt);   // 구매한 상품 개수

                    odDAO.insert(odVO);

                    forward = new ActionForward(); // 객체 생성
                    forward.setPath("success.jsp");   // 이동 경로
                    forward.setRedirect(false); // 넘겨줄 값 없음

                    // 세션에 cartFlag 값이 cart 이면, 장바구니 결제.
                } else if (session.getAttribute("cartFlag").equals("cart")) {

                    ArrayList<HashMap<Integer, ProductVO>> cart = (ArrayList<HashMap<Integer, ProductVO>>) session.getAttribute("cart");  // 장바구니 세션 가져오기

                    oVO.setmID(mid);   // 아이디 (세션)
                    oVO.setoAddress("경기도");  // 사용자가 입력한 주소
                    oVO.setoPrice(amount);   // 사용자가 구매한 가격
                    oVO.setoState("결제 완료");  // 결제 상태

                    oDAO.insert(oVO);  // DB에 삽입

                    oVO = oDAO.selectOne(oVO);  // 주문 번호를 가져오기 위한 장치

                    for (int i = 0; i < cart.size(); i++) {
                        HashMap<Integer, ProductVO> hashMap = cart.get(i); // 키 값
                        Set<Integer> keySet = hashMap.keySet();
                        ArrayList<Integer> keyList = new ArrayList<Integer>();
                        for (int v : keySet) {
                            keyList.add(v);
                        }
                        for (int v : keyList) {
                            pVO = hashMap.get(v);
                            odVO.setpNum(pVO.getpNum()); // 상품 번호
                            odVO.setoNum(oVO.getoNum()); // 주문 번호
                            odVO.setOdCnt(pVO.getTmpCnt()); // 구매한 상품 개수
                            odDAO.insert(odVO);
                        }
                    }

                    session.removeAttribute("cart");
                    forward = new ActionForward(); // 객체 생성
                    forward.setPath("success.jsp");   // 이동 경로
                    forward.setRedirect(false); // 넘겨줄 값 없음
                }

            } else { // 위의 조건이 전부 아니라면, 구독 결제.
                int subNum = (int) session.getAttribute("subNum");
                SubsinfoVO subVO = new SubsinfoVO();
                SubsinfoDAO subDAO = new SubsinfoDAO();
                subVO.setmID(mid);  // 아이디.
                subVO.setSubNum(subNum);  // 구독 상품 번호.
                subDAO.insert(subVO);  // 구독 테이블에 구독 정보 삽입.

                // 구독 정보 추가 후 구독 여부 변경하기
                mVO.setSk("CHANGESUBS");  // 구독 변경 SK
                mVO.setmID(mid); // 사용자 아이디 세팅

                mDAO.update(mVO);   // 구독 여부 변경 (0에서 1로 변경)

                forward = new ActionForward();  // 객체 생성
                forward.setPath("success.jsp"); // 이동 경로
                forward.setRedirect(false);  // 넘겨줄 값 없음

                session.removeAttribute("subNum"); // 구매후 세션 제거 (구독 상품 여부)
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        responseStream.close();

        return forward;
    }

}
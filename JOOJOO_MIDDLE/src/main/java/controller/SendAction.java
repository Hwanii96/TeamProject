package controller;

import model.MemberDAO;
import model.MemberVO;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

public class SendAction implements Action {

    @Override
    public ActionForward execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ActionForward forward = null;  // 객체 선언
        //  → 문자 보내기에 성공했을 때 객체 생성 (문자 전송 실패시 null값 반환)
        //  넘겨줄 값 : 없음 (문자 전송 후 메인 페이지로 이동)
        //  이동할 페이지 : main.jsp

        //  문자 API
        String api_key = "NCSOXSPWLLPGSTAP";
        String api_secret = "8S05FMW6BXJVF5CFRJ1GJDYGEEOKC9HA";
        Message coolsms = new Message(api_key, api_secret); // API 로 부터 가져온 클래스의 객체 생성

        MemberVO mVO = new MemberVO();
        MemberDAO mDAO = new MemberDAO();

        String mphone = request.getParameter("mphone");
        mVO.setmPhone(mphone);
        mVO.setSk("FINDID");
        mVO = mDAO.selectOne(mVO);

        //  4 params(to, from, type, text) are mandatory. must be filled
        HashMap<String, String> params = new HashMap<String, String>(); // 키 값
        params.put("to", request.getParameter("mphone"));   // 수신자 번호
        params.put("from", "01062638008");   // 발신 전화번호
        params.put("type", "SMS");  // 보내는 문자의 타입
        params.put("text", mVO.getmID()); // 보낼 내용
        params.put("app_version", "test app 1.2"); // 애플리케이션 이름과 버전

        try {  //  문자 보내는 기능
            JSONObject obj = (JSONObject) coolsms.send(params);
            System.out.println(obj.toString());
            forward = new ActionForward();
            forward.setPath("main.do"); //  성공적으로 문자 발송시 메인 페이지로 이동.
            forward.setRedirect(true);  //   리다이렉트 : true (넘겨줄 값 없음)
        } catch (CoolsmsException e) {  //  유효 하지 않은 번호를 입력 했다면, 예외 처리.
            System.out.println(e.getMessage());
            System.out.println(e.getCode());
        }

        return forward;
    }

}
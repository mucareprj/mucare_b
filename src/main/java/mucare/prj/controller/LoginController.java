package mucare.prj.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/pb")
public class LoginController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CLIENT_ID = "70a5316ede9855bd6e30b4369e792aa1";
    private static final String REDIRECT_URI = "http://localhost:3000/oauth/kakao/callback";

    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String id = credentials.get("id");
        String pw = credentials.get("pw");

        if ("admin".equals(id) && "1234".equals(pw)) {
            session.setAttribute("LOGIN_USER", id);
            session.setAttribute("loginMethod", "normal");
            session.setAttribute("userId", id);
            return ResponseEntity.ok(Map.of(
                    "result", "success",
                    "userId", id,
                    "loginMethod", "normal"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("result", "fail"));
        }
    }

    @PostMapping("/oauth/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> body, HttpSession session) {
        try {
            
            String code = body.get("code");

            // 1. 인가 코드로 access_token 요청
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String tokenBody = "grant_type=authorization_code"
                    + "&client_id=" + CLIENT_ID
                    + "&redirect_uri=" + REDIRECT_URI
                    + "&code=" + code;

            HttpEntity<String> tokenRequest = new HttpEntity<>(tokenBody, tokenHeaders);

            ResponseEntity<String> tokenResponse = restTemplate.postForEntity(
                    URI.create("https://kauth.kakao.com/oauth/token"),
                    tokenRequest,
                    String.class
            );

            JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
            String accessToken = tokenJson.get("access_token").asText();

            // 2. access_token으로 사용자 정보 요청
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);

            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<String> userResponse = restTemplate.exchange(
                    URI.create("https://kapi.kakao.com/v2/user/me"),
                    HttpMethod.GET,
                    userRequest,
                    String.class
            );

            JsonNode userJson = objectMapper.readTree(userResponse.getBody());
            long kakaoUserId = userJson.get("id").asLong();

            //JsonNode kakaoAccount = userJson.get("kakao_account");
            //String email = kakaoAccount.has("email") ? kakaoAccount.get("email").asText() : null;

            //JsonNode profile = kakaoAccount.get("profile");
            //String nickname = (profile != null && profile.has("nickname")) ? profile.get("nickname").asText() : null;

            // 3. 세션에 사용자 정보 저장
            session.setAttribute("LOGIN_USER", kakaoUserId);
            session.setAttribute("userId", kakaoUserId);
            session.setAttribute("loginMethod", "kakao");
            session.setAttribute("kakaoAccessToken", accessToken);  
            //session.setAttribute("EMAIL", email);
            //session.setAttribute("NICKNAME", nickname);

            return ResponseEntity.ok(Map.of(
                    "result", "success",
                    "userId", kakaoUserId,
                    "loginMethod", "kakao"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "result", "fail",
                    "message", "카카오 로그인 중 오류 발생"
            ));
        }
    }

    

    @GetMapping("/session")
    public ResponseEntity<?> session(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String loginMethod = (String) session.getAttribute("loginMethod");
        boolean loggedIn = session.getAttribute("LOGIN_USER") != null;
        Object userId = session.getAttribute("userId");
        
        System.out.println("로그인방식 : "+loginMethod+", 사용자id : "+userId);

        response.put("loggedIn", loggedIn);
        if (userId != null) response.put("userId",userId);
        if (loginMethod != null) response.put("loginMethod", loginMethod);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout/normal")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("result", "logout"));
    }

    @PostMapping("/logout/kakao")
    public ResponseEntity<?> kakaoLogout(HttpSession session) {
        String accessToken = (String) session.getAttribute("kakaoAccessToken");
        if (accessToken == null) {
            return ResponseEntity.badRequest().body("Access token not found in session");
        }

        // 카카오 unlink 요청
        String url = "https://kapi.kakao.com/v1/user/unlink";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        HttpEntity<?> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        System.out.println("카카오 연결 끊기 결과: " + response.getBody());

        session.invalidate();
        return ResponseEntity.ok("카카오 로그아웃 완료");
    }

    @PostMapping("/logout/naver")
    public ResponseEntity<?> logout2(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("result", "logout"));
    }

    @PostMapping("/logout/google")
    public ResponseEntity<?> logout3(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("result", "logout"));
    }
    
}

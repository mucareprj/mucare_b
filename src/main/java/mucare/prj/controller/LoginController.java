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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mucare.prj.domain.User;
import mucare.prj.service.UserService;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pb")
public class LoginController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CLIENT_ID = "70a5316ede9855bd6e30b4369e792aa1";
    private static final String REDIRECT_URI = "http://localhost:3000/oauth/kakao/callback";

    // Naver OAuth constants
    private static final String NAVER_CLIENT_ID = "LzQBtRpOwZ_C67N5GxsG";
    private static final String NAVER_CLIENT_SECRET = "BIHFPB7sNL";

    // Google OAuth constants
    private static final String GOOGLE_CLIENT_ID = "577683432334-gtn5qu78q0mqtuf5hh2mdc9r0qt4vrmu.apps.googleusercontent.com";
    private static final String GOOGLE_CLIENT_SECRET = "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";
    private static final String GOOGLE_REDIRECT_URI = "http://localhost:3000/oauth/google/callback";

    // Service Interface (shpark)
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        // String id = credentials.get("id");
        String email = credentials.get("email");
        String pw = credentials.get("pw");

        User user = userService.findByEmail(email);

        if (user == null || user.getPassword() == null) {
            log.info("존재하지 않는 사용자 : " + email );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("result", "fail", "message", "존재하지 않는 사용자입니다."));
                
        }
        
        if (!passwordEncoder.matches(pw, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("result", "fail", "message", "비밀번호가 일치하지 않습니다."));
        }

        userService.updateLastLogin(email);

        session.setAttribute("LOGIN_USER", user.getId());
        session.setAttribute("loginMethod", "normal");
        session.setAttribute("userId", user.getId());
        session.setAttribute("email", user.getEmail());

        session.setAttribute("lastLoginDateTime", user.getLastLogin());

        return ResponseEntity.ok(Map.of(
                "result", "success",
                "userId", user.getId(),
                "email", user.getEmail(),
                "loginMethod", "normal"));

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
                    String.class);

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
                    String.class);

            JsonNode userJson = objectMapper.readTree(userResponse.getBody());
            long kakaoUserId = userJson.get("id").asLong();

            JsonNode kakaoAccount = userJson.get("kakao_account");
            String email = null;
            if (kakaoAccount != null && kakaoAccount.has("email")) {
                email = kakaoAccount.get("email").asText();
            } else {
                // 이메일이 없으면 kakaoUserId를 이용해 임시 이메일 생성
                email = "kakao_" + kakaoUserId + "@noemail.com";
            }

            // JsonNode profile = kakaoAccount.get("profile");
            // String nickname = (profile != null && profile.has("nickname")) ?
            // profile.get("nickname").asText() : null;

            // 3. 세션에 사용자 정보 저장
            User user = userService.socialLoginOrRegister(email, "kakao", String.valueOf(kakaoUserId));

            userService.updateLastLoginByProvider("kakao", String.valueOf(kakaoUserId));

            session.setAttribute("LOGIN_USER", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("loginMethod", "kakao");
            session.setAttribute("kakaoAccessToken", accessToken);
            session.setAttribute("EMAIL", email);
            // session.setAttribute("NICKNAME", nickname);

            return ResponseEntity.ok(Map.of(
                    "result", "success",
                    "userId", user.getId(),
                    "loginMethod", "kakao"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "result", "fail",
                    "message", "카카오 로그인 중 오류 발생"));
        }
    }

    @PostMapping("/oauth/naver")
    public ResponseEntity<?> naverLogin(@RequestBody Map<String, String> body, HttpSession session) {
        try {
            String code = body.get("code");
            String state = body.get("state");

            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String tokenBody = "grant_type=authorization_code"
                    + "&client_id=" + NAVER_CLIENT_ID
                    + "&client_secret=" + NAVER_CLIENT_SECRET
                    + "&code=" + code;
            if (state != null) {
                tokenBody += "&state=" + state;
            }

            HttpEntity<String> tokenRequest = new HttpEntity<>(tokenBody, tokenHeaders);

            ResponseEntity<String> tokenResponse = restTemplate.postForEntity(
                    URI.create("https://nid.naver.com/oauth2.0/token"),
                    tokenRequest,
                    String.class);

            JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
            String accessToken = tokenJson.get("access_token").asText();

            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);

            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<String> userResponse = restTemplate.exchange(
                    URI.create("https://openapi.naver.com/v1/nid/me"),
                    HttpMethod.GET,
                    userRequest,
                    String.class);

            JsonNode userJson = objectMapper.readTree(userResponse.getBody());
            JsonNode responseNode = userJson.get("response");
            String naverUserId = responseNode.get("id").asText();

            session.setAttribute("LOGIN_USER", naverUserId);
            session.setAttribute("userId", naverUserId);
            session.setAttribute("loginMethod", "naver");
            session.setAttribute("naverAccessToken", accessToken);

            return ResponseEntity.ok(Map.of(
                    "result", "success",
                    "userId", naverUserId,
                    "loginMethod", "naver"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "result", "fail",
                    "message", "네이버 로그인 중 오류 발생"));
        }
    }

    @PostMapping("/oauth/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body, HttpSession session) {
        try {
            String code = body.get("code");

            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String tokenBody = "grant_type=authorization_code"
                    + "&client_id=" + GOOGLE_CLIENT_ID
                    + "&client_secret=" + GOOGLE_CLIENT_SECRET
                    + "&redirect_uri=" + GOOGLE_REDIRECT_URI
                    + "&code=" + code;

            HttpEntity<String> tokenRequest = new HttpEntity<>(tokenBody, tokenHeaders);

            ResponseEntity<String> tokenResponse = restTemplate.postForEntity(
                    URI.create("https://oauth2.googleapis.com/token"),
                    tokenRequest,
                    String.class);

            JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
            String accessToken = tokenJson.get("access_token").asText();

            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);

            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<String> userResponse = restTemplate.exchange(
                    URI.create("https://www.googleapis.com/oauth2/v1/userinfo?alt=json"),
                    HttpMethod.GET,
                    userRequest,
                    String.class);

            JsonNode userJson = objectMapper.readTree(userResponse.getBody());
            String googleUserId = userJson.get("id").asText();

            
            String email = userJson.get("email").asText();
            if (userJson.has("email")) {
                email = userJson.get("email").asText();
            } else {
                // email이 없는 경우 provider_id 기반 임시 이메일 생성
                email = "google_" + googleUserId + "@noemail.com";
            }

            User user = userService.socialLoginOrRegister(email, "google", String.valueOf(googleUserId));

            userService.updateLastLoginByProvider("google", String.valueOf(googleUserId));
            
            session.setAttribute("LOGIN_USER", googleUserId);
            session.setAttribute("userId", googleUserId);
            session.setAttribute("loginMethod", "google");
            session.setAttribute("googleAccessToken", accessToken);

            return ResponseEntity.ok(Map.of(
                    "result", "success",
                    "userId", googleUserId,
                    "loginMethod", "google"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "result", "fail",
                    "message", "구글 로그인 중 오류 발생"));
        }
    }

    @GetMapping("/session")
    public ResponseEntity<?> session(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String loginMethod = (String) session.getAttribute("loginMethod");
        boolean loggedIn = session.getAttribute("LOGIN_USER") != null;
        Object userId = session.getAttribute("userId");
        Object email = session.getAttribute("email");  
        System.out.println("로그인방식 : " + loginMethod + ", 사용자id : " + userId + ", 이메일 : " + email);

        response.put("loggedIn", loggedIn);
        if (userId != null)
            response.put("userId", userId);
        if (loginMethod != null)
            response.put("loginMethod", loginMethod);
        if (email != null) { 
            response.put("email", email);
        }
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
    public ResponseEntity<?> naverLogout(HttpSession session) {
        String accessToken = (String) session.getAttribute("naverAccessToken");
        if (accessToken == null) {
            session.invalidate();
            return ResponseEntity.badRequest().body("Access token not found in session");
        }

        try {
            String url = "https://nid.naver.com/oauth2.0/token?grant_type=delete"
                    + "&client_id=" + NAVER_CLIENT_ID
                    + "&client_secret=" + NAVER_CLIENT_SECRET
                    + "&access_token=" + accessToken
                    + "&service_provider=NAVER";

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println("네이버 연결 끊기 결과: " + response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }

        session.invalidate();
        return ResponseEntity.ok("네이버 로그아웃 완료");
    }

    @PostMapping("/logout/google")
    public ResponseEntity<?> googleLogout(HttpSession session) {
        String accessToken = (String) session.getAttribute("googleAccessToken");
        if (accessToken != null) {
            try {
                String url = "https://oauth2.googleapis.com/revoke?token=" + accessToken;
                restTemplate.postForEntity(url, null, String.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        session.invalidate();
        return ResponseEntity.ok("구글 로그아웃 완료");
    }

}

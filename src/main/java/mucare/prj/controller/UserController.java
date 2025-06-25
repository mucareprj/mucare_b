package mucare.prj.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mucare.prj.domain.User;
import mucare.prj.dto.SignupRequestDto;
import mucare.prj.service.UserService;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/users")
public class UserController {

    // [Controller] → [Service] → [Mapper 인터페이스] ↔ [Mapper XML] → [MySQL]

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signuo(@RequestBody SignupRequestDto dto) {
        {
            userService.signup(dto);
            log.info("회원가입 성공");
            return ResponseEntity.ok("회원가입 성공");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody SignupRequestDto dto) {
        User user = userService.login(dto.getEmail(), dto.getPassword());
        if(user != null){
            log.info("User logged in successfully : id={}, email={}", user.getId(), user.getEmail());
            return ResponseEntity.ok("로그인 성공");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
    }
    
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailDuplicate(@RequestParam("email") String email) {
        boolean exists = userService.isEmailExists(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

}

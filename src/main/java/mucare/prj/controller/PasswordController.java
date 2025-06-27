package mucare.prj.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mucare.prj.dto.PasswordChangeDTO;
import mucare.prj.dto.PasswordResetDTO;
import mucare.prj.service.PasswordService;
import mucare.prj.service.UserService;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/password")
public class PasswordController {

    private final PasswordService passwordService;
    private final UserService userService;
    @PostMapping("/change")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeDTO dto) {
        boolean success = passwordService.changePassword(dto);
        if (success) {
            return ResponseEntity.ok().body("비밀번호 변경 성공");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "기존 비밀번호가 일치하지 않습니다."));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> changePassword(@RequestBody PasswordResetDTO request) {
        boolean result = userService.updatePasswordByEmail(request.getEmail(), request.getNewPassword());
        if (result) {
            return ResponseEntity.ok("비밀번호가 변경되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 이메일을 가진 사용자가 존재하지 않습니다.");
        }
    }
}

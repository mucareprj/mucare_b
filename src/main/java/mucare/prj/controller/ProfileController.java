package mucare.prj.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mucare.prj.dto.ProfileDTO;
import mucare.prj.service.ProfileService;
import mucare.prj.service.UserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/myprofile")
public class ProfileController {

    private final ProfileService profileService;
    // private final UserService userService;

    @GetMapping("/load/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable("userId") Long userId) {
        try {
            // MyBatis Service 호출 (ProfileDTO 반환)
            ProfileDTO profile = profileService.getProfileByUserId(userId);
            log.info("로그인 유저 정보: {}", profile);
            if (profile != null) {
                return ResponseEntity.ok(profile);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("result", "fail", "message", "프로필을 찾을 수 없습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "fail", "message", "서버 오류가 발생했습니다."));
        }
    }

    @PostMapping("/save")
    public String saveProfile(@RequestBody ProfileDTO profileDTO) {
        try {
            profileService.saveProfile(profileDTO);
            return "프로필 저장 성공";
        } catch (Exception e) {
            e.printStackTrace();
            return "프로필 저장 실패";
        }
    }

}

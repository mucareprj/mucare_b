package mucare.prj.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mucare.prj.domain.User;
import mucare.prj.dto.ProfileDTO;
import mucare.prj.dto.UserProfileDTO;
import mucare.prj.service.ProfileService;
import mucare.prj.service.UserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/myprofile")
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    @GetMapping("/load/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable("userId") Long userId) {
        try {
            // MyBatis Service 호출 (ProfileDTO 반환)
            ProfileDTO profile = profileService.getProfileByUserId(userId);
            User user = userService.findById(userId);
            log.info("로그인 유저 정보: {}", user);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("result", "fail", "message", "사용자를 찾을 수 없습니다."));
            }

            UserProfileDTO response = new UserProfileDTO();
            response.setEmail(user.getEmail());

            if (profile != null) {
                response.setNickname(profile.getNickname());
                response.setName(profile.getName());
                response.setGender(profile.getGender());
                response.setBirthDate(profile.getBirthDate());
                response.setBirthTime(profile.getBirthTime());
                response.setBloodType(profile.getBloodType());
                response.setProfileImage(profile.getProfileImage());
                log.info("로그인 유저 프로필 : {}", response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "fail", "message", "서버 오류가 발생했습니다."));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveProfile(@RequestBody ProfileDTO profileDTO) {
        try {
            profileService.saveProfile(profileDTO);
            return ResponseEntity.ok("프로필 저장 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("프로필 저장 실패: " + e.getMessage());
        }
    }

    @PostMapping("/upload-image/{userId}")
    public ResponseEntity<?> uploadProfileImage(@PathVariable("userId") Long userId,
            @ModelAttribute ProfileDTO profileDTO) {

        try {
            profileDTO.setUserId(userId);
            MultipartFile imageFile = profileDTO.getImage();

            // 이미지 저장 로직 동일
            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            String rootPath = new File(".").getCanonicalPath();
            String uploadDir = rootPath + "/uploads/profile-images/";
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }

            Path savePath = Paths.get(uploadDir, fileName);
            Files.copy(imageFile.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);

            String imagePath = "/uploads/profile-images/" + fileName;

            // 프로필 정보 + 이미지 경로 업데이트 (userId와 profileDTO 포함)
            profileDTO.setProfileImage(imagePath);
            profileDTO.setUserId(userId);
            profileService.saveProfile(profileDTO);

            return ResponseEntity.ok(Map.of("result", "success", "imagePath", imagePath));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "fail", "message", "파일 업로드 실패"));
        }
    }
}

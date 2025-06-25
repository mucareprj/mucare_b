package mucare.prj.service;

import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mucare.prj.domain.User;
import mucare.prj.domain.UserAgreement;
import mucare.prj.dto.SignupRequestDto;
import mucare.prj.mapper.UserMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequestDto dto) {

        if (userMapper.selectUserByEmail(dto.getEmail()) != null) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        User user = new User();

        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setIsActive(true);
        user.setProvider("normal");
        userMapper.insertUser(user);

        // 약관동의
        UserAgreement agreement = new UserAgreement();
        agreement.setId(user.getId());
        agreement.setAgreeService(dto.getAgreeService());
        agreement.setAgreePrivacy(dto.getAgreePrivacy());
        agreement.setAgreeMarketing(dto.getAgreeMarketing());
        userMapper.insertAgreement(agreement);

    }

    public User login(String email, String password) {
        User user = userMapper.selectUserByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            userMapper.updateLastLogin(email);
            return user;
        }
        return null;

    }

    public User socialLoginOrRegister(String email, String provider, String providerId) {
        // 1) provider + providerId 로 기존 유저 조회
        User user = userMapper.findByProvider(Map.of("provider", provider, "providerId", providerId));

        if (user != null) {
            // 기존 유저가 있으면 로그인 처리용으로 반환
            return user;
        }

        // 2) 없으면 신규 가입 처리 (소셜로그인 시 비밀번호는 임의값 또는 null 처리)
        User newUser = new User();

        newUser.setEmail(email);
        newUser.setPassword(null); // 소셜 로그인은 비밀번호 없어도 됨
        newUser.setProvider(provider);
        newUser.setProviderId(providerId);

        newUser.setIsActive(true);

        userMapper.insertUser(newUser);

        return newUser;
    }

    public User findByEmail(String email) {
        return userMapper.selectUserByEmail(email);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void updateLastLogin(String email) {
        userMapper.updateLastLogin(email);
    }

    public int updateLastLoginByProvider(String provider, String providerId) {
        return userMapper.updateLastLoginByProvider(provider, providerId);
    }

    // ## 회원가입 이메일 중복 검사
    public boolean isEmailExists(String email) {
        return userMapper.selectUserByEmail(email) != null;
    }
}

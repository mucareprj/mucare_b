package mucare.prj.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import mucare.prj.dto.PasswordChangeDTO;
import mucare.prj.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class PasswordService {
    
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public boolean changePassword(PasswordChangeDTO dto){
        String storedPassword = userMapper.getPasswordByUserId(dto.getUserId());

        // 입력한 현재 비밀번호와 DB의 해시값 비교
        if (!passwordEncoder.matches(dto.getCurrentPassword(), storedPassword)) {
            return false;
        }

        // 새 비밀번호 해시 후 저장
        String newHashedPassword = passwordEncoder.encode(dto.getNewPassword());
        userMapper.updatePassword(dto.getUserId(), newHashedPassword);

        return true;
    }

}

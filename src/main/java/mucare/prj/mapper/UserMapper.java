package mucare.prj.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import mucare.prj.domain.User;

@Mapper
public interface UserMapper {

    int insertUser(User user); // 회원 정보 저장

    User selectUserByEmail(@Param("email") String email); // 이메일로 회원 조회

    int updateLastLogin(String email);

    User findByProvider(Map<String, Object> params);

    int updateLastLoginByProvider(@Param("provider") String provider, @Param("providerId") String providerId);

    User findById(Long userId);

    String getPasswordByUserId(Long userId);

    void updatePassword(@Param("userId") Long userId, @Param("newPassword") String newPassword);
    int updatePasswordByEmail(@Param("email") String email, @Param("newPassword") String newPassword);
    
    boolean existsByEmailAndPhoneNumber(@Param("email") String email, @Param("phoneNumber") String phoneNumber);
}

package mucare.prj.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import mucare.prj.dto.ProfileDTO;

@Mapper
public interface ProfileMapper {

    void insertProfile(ProfileDTO profile);

    int updateProfile(ProfileDTO profileDTO);

    ProfileDTO selectProfileByUserId(Long userId);

    int updateProfileImage(@Param("userId") Long userId, @Param("imagePath") String imagePath);

    int insertProfileImage(@Param("userId") Long userId, @Param("imagePath") String imagePath);

    boolean existsByUserId(Long userId);

}
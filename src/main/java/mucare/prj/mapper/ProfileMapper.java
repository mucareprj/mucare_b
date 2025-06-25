package mucare.prj.mapper;

import org.apache.ibatis.annotations.Mapper;

import mucare.prj.dto.ProfileDTO;

@Mapper
public interface ProfileMapper {
    void insertProfile(ProfileDTO profile);
    ProfileDTO selectProfileByUserId(Long userId);
} 
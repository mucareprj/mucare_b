package mucare.prj.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mucare.prj.dto.ProfileDTO;
import mucare.prj.mapper.ProfileMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {
    
    private final ProfileMapper profileMapper;

    public void saveProfile(ProfileDTO profileDTO){
        profileMapper.insertProfile(profileDTO);
    }

    public ProfileDTO getProfileByUserId(Long userId) {
        return profileMapper.selectProfileByUserId(userId);
    }
}

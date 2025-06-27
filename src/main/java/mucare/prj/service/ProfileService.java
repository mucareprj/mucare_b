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

    public void saveProfile(ProfileDTO profileDTO) {
            System.out.println("saveProfile userId: " + profileDTO.getUserId());
                System.out.println("saveProfile DTO: " + profileDTO);
        int updatedRows = profileMapper.updateProfile(profileDTO);
        if (updatedRows == 0) {
            profileMapper.insertProfile(profileDTO);
        }
    }

    public ProfileDTO getProfileByUserId(Long userId) {
        return profileMapper.selectProfileByUserId(userId);
    }

    public void updateProfileImage(Long userId, String imagePath) {
        int updatedRows = profileMapper.updateProfileImage(userId, imagePath);
        if (updatedRows == 0) {
            profileMapper.insertProfileImage(userId, imagePath);
        }
    }

    public boolean existsByUserId(Long userId) {
        return profileMapper.existsByUserId(userId);
    }

    public void saveOrUpdateProfile(ProfileDTO profileDTO) {
        boolean exists = profileMapper.existsByUserId(profileDTO.getUserId());
        if (exists) {
            profileMapper.updateProfile(profileDTO);
        } else {
            profileMapper.insertProfile(profileDTO);
        }
    }
}

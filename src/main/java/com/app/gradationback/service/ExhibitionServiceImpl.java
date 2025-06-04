package com.app.gradationback.service;

import com.app.gradationback.domain.*;
import com.app.gradationback.mapper.ExhibitionMapper;
import com.app.gradationback.repository.ExhibitionDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ExhibitionServiceImpl implements ExhibitionService {

    private final ExhibitionDAO exhibitionDAO;

    @Override
    public Optional<GradationExhibitionVO> getGradation() {
        return exhibitionDAO.findGradation();
    }

    @Override
    public List<GradationExhibitionImgVO> getGradationImgAll(Long gradationExhibitionId) {
        return exhibitionDAO.findGradationImgAll(gradationExhibitionId);
    }

    @Override
    public GradationExhibitionVO registerGradation(GradationExhibitionDTO gradationExhibitionDTO) {
        GradationExhibitionVO gradationExhibitionVO = new GradationExhibitionVO();
        gradationExhibitionVO.setGradationExhibitionTitle(gradationExhibitionDTO.getGradationExhibitionTitle());
        gradationExhibitionVO.setGradationExhibitionArt(gradationExhibitionDTO.getGradationExhibitionArt());
        gradationExhibitionVO.setGradationExhibitionCategory(gradationExhibitionDTO.getGradationExhibitionCategory());
        gradationExhibitionVO.setGradationExhibitionTime(gradationExhibitionDTO.getGradationExhibitionTime());
        gradationExhibitionVO.setGradationExhibitionFee(gradationExhibitionDTO.getGradationExhibitionFee());
        gradationExhibitionVO.setGradationExhibitionTel(gradationExhibitionDTO.getGradationExhibitionTel());
        gradationExhibitionVO.setGradationExhibitionAddress(gradationExhibitionDTO.getGradationExhibitionAddress());
        gradationExhibitionVO.setGradationExhibitionDate(gradationExhibitionDTO.getGradationExhibitionDate());
        exhibitionDAO.saveGradation(gradationExhibitionVO);

        List<Long> arts = exhibitionDAO.findTop50ArtId();
        if(arts != null && !arts.isEmpty()) {
            for(Long artId : arts) {
                Map<String, Object> params = new HashMap<>();
                params.put("artId", artId);
                params.put("gradationExhibitionId", gradationExhibitionVO.getId());
                exhibitionDAO.savePastExhibition(params);
            };
        }
        return gradationExhibitionVO;
    }

    @Override
    public void registerGradationImage(GradationExhibitionImgVO gradationExhibitionImgVO) {
        exhibitionDAO.saveGradationImage(gradationExhibitionImgVO);
    }

    @Override
    public void editGradation(GradationExhibitionVO gradationExhibitionVO) {
        exhibitionDAO.updateGradation(gradationExhibitionVO);
    }

    @Override
    public void removeGradationImage(Long id) {
        exhibitionDAO.deleteGradationImage(id);
    }

    @Override
    public List<GradationExhibitionVO> getRecentGradations() {
        return exhibitionDAO.findRecentGradations();
    }

    @Override
    public List<DisplayDTO> getTopLikedArts() {
        return exhibitionDAO.findTopLikedArts();
    }

    @Override
    public List<ExhibitionPastDTO> getPastExhibitions() {
        return exhibitionDAO.findPastExhibitions();
    }

    @Override
    public List<ExhibitionPastDTO> getExhibitionArtList(Map<String, Object> params) {
        return exhibitionDAO.findExhibitionArtList(params);
    }

    @Override
    public Integer getCountExhibitionArtList(Long exhibitionId) {
        return exhibitionDAO.findCountExhibitionArtList(exhibitionId);
    }


//    대학교 전시회
    @Override
    public void registerUniversity(UniversityExhibitionDTO universityExhibitionDTO) {
//        대학교 중복 확인(로고)
        Optional<UniversityVO> university = exhibitionDAO.findUniversityByName(universityExhibitionDTO.getUniversityName());

        if(university.isPresent()) {
            UniversityVO universityVO = university.get();
            universityExhibitionDTO.setId(universityVO.getId());
            universityExhibitionDTO.setUniversityLogoImgName(universityVO.getUniversityLogoImgName());
            universityExhibitionDTO.setUniversityLogoImgPath(universityVO.getUniversityLogoImgPath());
        } else {
            universityExhibitionDTO.setUniversityLogoImgName("default-logo.png");
            universityExhibitionDTO.setUniversityLogoImgPath("assets/images/university/logo");
            exhibitionDAO.saveUniversity(universityExhibitionDTO);
        }
//        학과 저장
        exhibitionDAO.saveMajor(universityExhibitionDTO);
        universityExhibitionDTO.setUniversityExhibitionRequestDate(new Date());
//        전시회 저장
        exhibitionDAO.saveUniversityExhibition(universityExhibitionDTO);
    }

    @Override
    public void registerUniversityImg(UniversityExhibitionDTO universityExhibitionDTO) {
        if(universityExhibitionDTO.getUniversityExhibitionImgPath() != null && universityExhibitionDTO.getUniversityExhibitionImgName() != null) {
            exhibitionDAO.saveUniversityExhibitionImg(universityExhibitionDTO);
        }
    }

//    대학 전시회 정보
    @Override
    public List<UniversityExhibitionDTO> getUniversity(Map<String, Object> params) {
        return exhibitionDAO.findUniversity(params).stream()
                .map((university) -> {
                    university.setUniversityExhibitionImgList(exhibitionDAO.findUniversityImgAll(university.getId()));

                    if(university.getUniversityLikeId() != null) {
                        university.setLiked(true);
                    } else {
                        university.setLiked(false);
                    }

                    return university;
                })
                .map(university -> {
                    Date now = new Date();
                    Date startDate = university.getUniversityExhibitionStartDate();

                    if(now.before(startDate)) {
                        university.setUniversityExhibitionState("전시 예정");
                    } else {
                        university.setUniversityExhibitionState("전시 중");
                    }
                    return university;
                }).toList();
    }

//    대학 전시회 사진
    @Override
    public List<UniversityExhibitionImgVO> getUniversityImgAll(Long universityExhibitionId) {
        return exhibitionDAO.findUniversityImgAll(universityExhibitionId);
    }

//    좋아요 등록
    @Override
    public void registerUniversityLike(UniversityLikeVO universityLikeVO) {
        exhibitionDAO.saveUniversityLike(universityLikeVO);
    }

//    좋아요 여부
    @Override
    public boolean getUniversityLike(UniversityLikeVO universityLikeVO) {
        if(exhibitionDAO.findUniversityLike(universityLikeVO) == 1) {
            return true;
        }
        return false;
    }

//    좋아요 취소
    @Override
    public void removeUniversityLike(UniversityLikeVO universityLikeVO) {
        exhibitionDAO.deleteUniversityLike(universityLikeVO);
    }

//      내가 신청한 전시회 승인내역 조회
    @Override
    public List<UniversityExhibitionDTO> getExhibitionStatus(Long userId){
        return exhibitionDAO.findMyExhibitionStatus(userId);
    }

//      내가 좋아요 한 전시회 목록
    @Override
    public List<UniversityExhibitionDTO> getLikedUniversityExhibition(Long userId) {
        return exhibitionDAO.findLikedUniversityExhibition(userId);
    }
}





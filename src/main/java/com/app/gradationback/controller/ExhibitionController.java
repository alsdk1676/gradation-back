package com.app.gradationback.controller;

import com.app.gradationback.domain.*;
import com.app.gradationback.service.ExhibitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/exhibitions/api/*")
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

//    gradation
//    전시회 정보 불러오기
    @Operation(summary = "현재 gradation 전시회 조회", description = "현재 gradation 전시회를 조회할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "현재 gradation 전시회 조회 성공")
    @GetMapping("gradation/current")
    public ResponseEntity<Map<String, Object>> getGradation() {
        Optional<GradationExhibitionVO> gradationExhibitionVO = exhibitionService.getGradation();
            Map<String, Object> response = new HashMap<>();

        if (gradationExhibitionVO.isPresent()) {
            GradationExhibitionVO gradationExhibition = gradationExhibitionVO.get();
            List<GradationExhibitionImgVO> images = exhibitionService.getGradationImgAll(gradationExhibition.getId());

            response.put("gradation", gradationExhibition);
            response.put("images", images);
            response.put("message", "전시회 조회를 성공하였습니다.");
            return ResponseEntity.ok(response);
        }
        response.put("message", "전시회 조회를 실패하였습니다.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

//    전시회 등록
    @Operation(summary = "전시회 등록", description = "전시회를 등록할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "전시회 등록 성공")
    @PostMapping("gradation/registration")
    public ResponseEntity<Map<String, Object>> registerGradation(@RequestBody GradationExhibitionDTO gradationExhibitionDTO) {
        Map<String, Object> response = new HashMap<>();

        try {
            GradationExhibitionVO gradationVO = exhibitionService.registerGradation(gradationExhibitionDTO);

            if (gradationVO != null) {
                response.put("message", "전시회 등록 성공");
                response.put("gradation", gradationVO);
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "전시회 등록 실패");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

        } catch (Exception e) {
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

//    전시회 정보 수정 0
    @Operation(summary = "전시회 정보 수정", description = "전시회 정보를 수정할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "전시회 정보 수정 성공")
    @Parameter(
            name = "id",
            description = "전시회 번호",
            schema = @Schema(type = "number"),
            in = ParameterIn.PATH,
            required = true
    )
    @PutMapping("modify/{id}")
    public GradationExhibitionVO modifyGradation(@PathVariable Long id, @RequestBody GradationExhibitionVO gradationExhibitionVO) {
        gradationExhibitionVO.setId(id);
        exhibitionService.editGradation(gradationExhibitionVO);
        Optional<GradationExhibitionVO> foundGradation = exhibitionService.getGradation();
        if (foundGradation.isPresent()) {
            return foundGradation.get();
        }
        return new GradationExhibitionVO();
    }

//    전시회 장소 이미지 삭제 0
    @Operation(summary = "전시관 이미지 삭제", description = "전시관 이미지를 삭제할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "이미지 삭제 성공")
    @Parameter(
            name = "id",
            description = "전시관 이미지 번호",
            schema = @Schema(type = "number"),
            in = ParameterIn.PATH,
            required = true
    )
    @DeleteMapping("gradation/image/{id}")
    public void deleteGradationImage(@PathVariable("id") Long id) {
        exhibitionService.removeGradationImage(id);
    }

//    최근 전시회 3개 조회
    @Operation(summary = "최근 전시회 3개 조회", description = "최근 전시회 3개를 조회할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "최근 전시회 조회 성공")
    @GetMapping("gradation/recent")
    public ResponseEntity<Map<String, Object>> getRecentGradation() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<GradationExhibitionVO> exhibitions = exhibitionService.getRecentGradations();

            List<Map<String, String>> exhibitionList = exhibitions
                    .stream()
                    .map(exhibition -> {
                        Map<String, String> map = new HashMap<>();
                        String exhibitionDate = exhibition.getGradationExhibitionDate().substring(0, 4);
                        map.put("id",String.valueOf(exhibition.getId()));
                        map.put("title", exhibitionDate + " " + exhibition.getGradationExhibitionTitle());
                        return map;
                    })
                    .collect(Collectors.toList());

            response.put("exhibitions", exhibitionList);
            response.put("message", "최근 전시회 조회 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

//    좋아요순 작품 50개
    @Operation(summary = "현재 인기 작품 조회", description = "현재 인기 작품을 조회할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "인기 작품 조회 성공")
    @GetMapping("gradation/top-liked-art")
    public ResponseEntity<List<DisplayDTO>> getTopLikedArts() {
        List<DisplayDTO> topLikedArts = exhibitionService.getTopLikedArts();

        if (topLikedArts.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(topLikedArts);
    }

    @Operation(summary = "지난 전시회 목록", description = "지난 전시회를 조회할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "지난 전시회 조회 성공")
    @GetMapping("gradation/past")
    public ResponseEntity<Map<String, Object>> getPastExhibitions() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<ExhibitionPastDTO> exhibitions = exhibitionService.getPastExhibitions();

            if(exhibitions != null) {
                response.put("message", "지난 전시회 목록 조회 성공");
                response.put("exhibitions", exhibitions);
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "지난 전시회 목록 조회 실패");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
        } catch (Exception e) {
            response.put("message", "서버 오류: " +  e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "지난 전시회 작품 목록", description = "지난 전시회에 전시되었던 작품목록을 조회할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "지난 전시회 작품 목록 조회 성공")
    @GetMapping("gradation/past/{exhibitionId}/arts")
    @Parameters({
            @Parameter(name = "exhibitionId", description = "전시회 ID", example = "1"),
            @Parameter(name = "cursor", description = "페이지", example = "1")
    })
    public ResponseEntity<Map<String, Object>> getExhibitionArtList(@PathVariable Long exhibitionId, @RequestParam Integer cursor) {
        Map<String, Object> response = new HashMap<>();
        response.put("exhibitionId", exhibitionId);
        response.put("cursor", cursor);

        try {
            List<ExhibitionPastDTO> exhibitionArts = exhibitionService.getExhibitionArtList(response);

            if(exhibitionArts != null) {
                response.put("arts", exhibitionArts);
                response.put("message", "지난 전시회 작품 목록 조회 성공");
                response.put("contents", exhibitionService.getCountExhibitionArtList(exhibitionId));
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "지난 전시회 작품 목록 조회 실패");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
        } catch (Exception e) {
            response.put("message", "서버 오류: " +  e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


//    University
//    대학교 전시회 등록
    @Operation(summary = "대학교 전시회 신청", description = "대학교 전시회를 신청할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "대학교 전시회 신청 성공")
    @PostMapping("university/register")
    public ResponseEntity<Map<String, Object>> registerUniversity(@RequestBody UniversityExhibitionDTO universityExhibitionDTO) {
        Map<String, Object> response = new HashMap<>();

        try {
            exhibitionService.registerUniversity(universityExhibitionDTO);
            response.put("message", "등록이 완료되었습니다");
            response.put("status", universityExhibitionDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            response.put("status", universityExhibitionDTO);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

//    대학교 전시회 불러오기
    @Operation(summary = "대학교 전시회 조회", description = "대학교 전시회를 조회할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "대학교 전시회 조회 성공")
    @PostMapping("university/list")
    public ResponseEntity<Map<String, Object>> getUniversity(@RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        response.put("university", exhibitionService.getUniversity(params));
        return ResponseEntity.ok(response);
    }

//    해당 대학교 전시회 이미지 조회
    @Operation(summary = "대학교 전시회 이미지 조회", description = "대학교 전시회에 등록된 이미지를 모두 조회할 수 있는 API.")
    @ApiResponse(responseCode = "200", description = "해당 대학교 이미지 조회 성공")
    @Parameter(
            name = "universityExhibitionId",
            description = "전시회 ID",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = "integer")
    )
    @GetMapping("university/{universityExhibitionId}/images")
    public ResponseEntity<Map<String, Object>> getUniversityImg(@PathVariable("universityExhibitionId") Long universityExhibitionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<UniversityExhibitionImgVO> images = exhibitionService.getUniversityImgAll(universityExhibitionId);
            if(images != null) {
                response.put("images", images);
                response.put("message", "이미지 조회 성공");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "이미지 조회 실패");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
        } catch (Exception e) {
            response.put("message", "서버 오류: " +  e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


//    대학교 좋아요 등록
    @Operation(summary = "대학교 전시회 좋아요", description = "대학교 전시회 좋아요를 할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "대학교 전시회 좋아요 성공")
    @PostMapping("university/like")
    public ResponseEntity<Map<String, Object>> registerUniversityLike(@RequestBody UniversityLikeVO universityLikeVO) {
        Map<String, Object> response = new HashMap<>();

        if(exhibitionService.getUniversityLike(universityLikeVO)) {
            response.put("message", "이미 좋아요 누름");
            response.put("status", universityLikeVO);
            response.put("isLiked", true);
            return ResponseEntity.ok(response);
        }
        try {
            exhibitionService.registerUniversityLike(universityLikeVO);
        } catch (Exception e) {
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        response.put("message", "좋아요 등록 성공");
        response.put("status", universityLikeVO);
        response.put("isLiked", false);
        return ResponseEntity.ok(response);
    }

//    좋아요 여부
    @Operation(summary = "대학교 좋아요 여부 조회", description = "대학교 좋아요 여부를 조회할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "대학교 좋아요 여부 조회 성공")
    @PostMapping("/liked")
    public ResponseEntity<Map<String, Object>> isLiked(@RequestBody UniversityLikeVO universityLikeVO) {
        Map<String, Object> response = new HashMap<>();
        boolean isLiked = exhibitionService.getUniversityLike(universityLikeVO);
        if (isLiked) {
            response.put("message", "좋아요 여부 true");
            response.put("isLiked", isLiked);
            return ResponseEntity.ok(response);
        }
        response.put("message", "좋아요 여부 false");
        response.put("isLiked", isLiked);
        return ResponseEntity.ok(response);
    }

//    좋아요 취소
    @Operation(summary = "대학교 전시회 좋아요 취소", description = "대학교 전시회 좋아요를 취소할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "대학교 전시회 좋아요 취소 성공")
    @DeleteMapping("university/unlike")
    public ResponseEntity<Map<String, Object>> removeUniversityLike(@RequestBody UniversityLikeVO universityLikeVO) {
        Map<String, Object> response = new HashMap<>();

        try {
            exhibitionService.removeUniversityLike(universityLikeVO);
        } catch (Exception e) {
            response.put("message", "서버 오류: " + e.getMessage());
            response.put("status", universityLikeVO);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        response.put("message", "좋아요 취소 성공");
        response.put("status", universityLikeVO);
        return ResponseEntity.ok(response);
    }

//    내 승인내역 조회
    @Operation(summary = "내 승인내역 조회", description = "내가 신청한 대학교 전시회 승인내역을 조회할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "승인내역 조회 성공")
    @Parameter(
            name = "userId",
            description = "사용자 ID",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = "integer")
    )
    @GetMapping("university/{userId}/exhibition-status")
    public ResponseEntity<Map<String, Object>> getExhibitionStatus(@PathVariable("userId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<UniversityExhibitionDTO> statusList = exhibitionService.getExhibitionStatus(userId);
            response.put("message", "승인내역 조회 성공");
            response.put("statusList", statusList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



//    내가 좋아요 누른 대학교 전시회 조회
    @Operation(summary = "좋아요 누른 대학교 전시회 조회", description = "내가 좋아요 누른 대학교 전시회를 조회할 수 있는 API")
    @ApiResponse(responseCode = "200", description = "좋아요한 전시회 조회 성공")
    @Parameter(
            name = "userId",
            description = "사용자 ID",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = "integer")
    )
    @GetMapping("university/{userId}/liked-exhibitions")
    public ResponseEntity<Map<String, Object>> getLikedUniversityExhibition(@PathVariable("userId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<UniversityExhibitionDTO> likedExhibitions = exhibitionService.getLikedUniversityExhibition(userId);
            response.put("message", "좋아요한 전시회 조회 성공");
            response.put("likedExhibitions", likedExhibitions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }






}





































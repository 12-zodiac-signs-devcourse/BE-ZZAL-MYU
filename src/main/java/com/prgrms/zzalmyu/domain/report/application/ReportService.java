package com.prgrms.zzalmyu.domain.report.application;

import com.prgrms.zzalmyu.core.properties.ErrorCode;
import com.prgrms.zzalmyu.domain.image.application.ImageRemoveService;
import com.prgrms.zzalmyu.domain.image.infrastructure.ImageRepository;
import com.prgrms.zzalmyu.domain.report.domain.entity.Report;
import com.prgrms.zzalmyu.domain.report.exception.ReportException;
import com.prgrms.zzalmyu.domain.report.infrastructure.ReportRepository;
import com.prgrms.zzalmyu.domain.report.presentation.dto.response.ReportDetailResponse;
import com.prgrms.zzalmyu.domain.report.presentation.dto.response.ReportResponse;
import com.prgrms.zzalmyu.domain.tag.domain.entity.Tag;
import com.prgrms.zzalmyu.domain.tag.presentation.dto.res.TagResponseDto;
import com.prgrms.zzalmyu.domain.user.application.UserService;
import com.prgrms.zzalmyu.domain.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserService userService;
    private final ImageRemoveService imageRemoveService;
    private final ImageRepository imageRepository;

    public void reportImage(Long userId, Long imageId) {
        if(!reportRepository.findByImageIdAndReportUserId(imageId, userId).isEmpty()) {
            throw new ReportException(ErrorCode.REPORT_ALREADY_EXIST_ERROR);
        }
        Report report = Report.builder()
                .reportUserId(userId)
                .imageId(imageId)
                .build();
        reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<ReportDetailResponse> getReportDetail(Long imageId) {
        List<Report> reports = reportRepository.findByImageId(imageId);
        if (reports.isEmpty()) {
            throw new ReportException(ErrorCode.REPORT_NOT_FOUND);
        }

        return reports.stream()
                .map(report -> {
                    User reportUser = userService.findUserById(report.getReportUserId());
                    List<TagResponseDto> tags = getTags(report.getImageId());
                    return ReportDetailResponse.of(report, reportUser, tags);
                })
                .toList();
    }

    public void deleteReportedImage(Long imageId) {
        int reportCount = reportRepository.countByImageId(imageId);
        if (reportCount >= 3) {
            imageRemoveService.deleteReportImage(imageId);
        } else {
            throw new ReportException(ErrorCode.IMAGE_DELETION_NOT_ALLOWED);
        }
    }

    private List<TagResponseDto> getTags(Long imageId) {
        List<Tag> tags = imageRepository.findTagsByImageId(imageId);
        return tags.stream()
                .map(tag -> new TagResponseDto(tag.getId(), tag.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getReports(Pageable pageable) {
        List<Long> reportedImageIds = reportRepository.getImageIdReportedOverThree(pageable);
        return reportedImageIds.stream()
                .map(imageId -> {
                    LocalDateTime lastReportDate = reportRepository.getLastReportAt(imageId);
                    int reportCount = reportRepository.countByImageId(imageId);
                    List<TagResponseDto> tags = getTags(imageId);
                    return ReportResponse.of(imageId, lastReportDate, reportCount, tags);
                })
                .toList();
    }
}

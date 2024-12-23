package atemos.everse.api.service;

import atemos.everse.api.dto.IotStatusHistoryDto;
import atemos.everse.api.entity.IotStatusHistory;
import atemos.everse.api.repository.CompanyRepository;
import atemos.everse.api.repository.IotStatusHistoryRepository;
import atemos.everse.api.specification.IotStatusHistorySpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * IotStatusHistoryServiceImpl 클래스는 IoT 상태 이력의 생성, 조회, 삭제 등의 기능을 제공하는 서비스 클래스입니다.
 * 이 클래스는 IoT 장치의 상태 변화를 추적하고, 특정 기간 동안의 IoT 상태 이력을 관리합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IotStatusHistoryServiceImpl implements IotStatusHistoryService {
    private final IotStatusHistoryRepository iotStatusHistoryRepository;
    private final CompanyRepository companyRepository;

    /**
     * 조건에 맞는 IoT 현황을 조회합니다.
     *
     * @param readIotHistoryRequestDto IoT 현황 조회를 위한 데이터 전송 객체입니다.
     * @param pageable 페이지 정보입니다.
     * @return 조건에 맞는 IoT 현황 목록과 현재 IoT 기기의 상태, 페이지 정보를 포함하는 응답 객체입니다.
     */
    @Override
    @Transactional(readOnly = true)
    public IotStatusHistoryDto.ReadIotHistoryPageResponse read(IotStatusHistoryDto.ReadIotHistoryRequest readIotHistoryRequestDto, Pageable pageable) {
        // 업체가 존재하는지 확인하고 타임존 가져오기
        var company = companyRepository.findById(readIotHistoryRequestDto.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("No such company."));
        var zoneId = company.getCountry().getZoneId();
        // 조건에 맞는 IoT 현황 목록을 페이징 처리하여 조회
        var iotHistoryPage = iotStatusHistoryRepository.findAll(
                IotStatusHistorySpecification.findWith(readIotHistoryRequestDto, zoneId),
                pageable);
        // 각 IoT 기기의 가장 최근 상태를 기준으로 상태별 Count 계산
        var iotStatus = iotHistoryPage.stream()
                .collect(Collectors.groupingBy(
                        IotStatusHistory::getIot,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(IotStatusHistory::getCreatedDate)),
                                optHistory -> optHistory.map(IotStatusHistory::getStatus).orElse(null))
                )).values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(status -> status, Collectors.counting()));
        // 응답 객체 반환
        return IotStatusHistoryDto.ReadIotHistoryPageResponse.builder()
                .iotHistoryList(iotHistoryPage.stream()
                        .map(IotStatusHistoryDto.ReadIotHistoryResponse::new)
                        .toList())
                .iotStatus(iotStatus)
                .totalElements(iotHistoryPage.getTotalElements())
                .totalPages(iotHistoryPage.getTotalPages())
                .build();
    }

    /**
     * 특정 업체의 특정 기간 내 시간별 IoT 상태 이력 조회 API.
     *
     * @param readIotHistoryRequestDto IoT 현황 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @return 특정 업체의 특정 날짜 범위에 해당하는 IoT 상태 이력 데이터
     */
    @Override
    @Transactional(readOnly = true)
    public IotStatusHistoryDto.ReadIotHistoryPageResponse readByCompanyId(IotStatusHistoryDto.ReadIotHistoryRequest readIotHistoryRequestDto) {
        // 업체가 존재하는지 확인
        var company = companyRepository.findById(readIotHistoryRequestDto.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("No such company."));
        // 국가 정보에서 타임존 가져오기
        var zoneId = company.getCountry().getZoneId();
        // 조회할 기간의 시작과 끝 시간 계산 (해당 업체의 타임존 고려)
        var startDateTime = LocalDate.now(zoneId).atStartOfDay().atZone(zoneId).toInstant();
        var endDateTime = LocalDate.now(zoneId).atTime(23, 59, 59).atZone(zoneId).toInstant();
        // 특정 기간에 해당하는 IoT 이력 조회 (UTC 기준으로 데이터베이스에서 조회)
        var iotHistories = iotStatusHistoryRepository.findByIot_Company_IdAndCreatedDateBetween(
                readIotHistoryRequestDto.getCompanyId(), startDateTime, endDateTime);
        // 시간대별 및 상태별로 데이터 그룹화
        var groupedByHourAndStatus = iotHistories.stream()
                .collect(Collectors.groupingBy(
                        iotHistory -> iotHistory.getCreatedDate().atZone(zoneId).truncatedTo(ChronoUnit.HOURS),
                        Collectors.groupingBy(IotStatusHistory::getStatus, Collectors.counting())));
        // IoT 현황 응답 리스트 생성
        var responseList = new ArrayList<IotStatusHistoryDto.ReadIotHistoryResponse>();
        for (var hour : groupedByHourAndStatus.keySet()) {
            var statusMap = groupedByHourAndStatus.get(hour);
            var hourlyResponse = IotStatusHistoryDto.ReadIotHistoryResponse.builder()
                    .referenceTime(hour.toLocalDateTime())  // 클라이언트의 시간대에 맞게 변환된 시간
                    .iotStatus(statusMap)
                    .build();
            responseList.add(hourlyResponse);
        }
        // 최종 응답 객체 생성 및 반환
        return IotStatusHistoryDto.ReadIotHistoryPageResponse.builder()
                .iotHistoryList(responseList)
                .build();
    }

    /**
     * IoT 현황을 삭제합니다.
     *
     * @param id 삭제할 IoT 현황의 ID입니다.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        // IoT 현황이 존재하면 삭제
        iotStatusHistoryRepository.findById(id).ifPresentOrElse(
                iotStatusHistoryRepository::delete,
                () -> {
                    throw new EntityNotFoundException("No such iotHistory.");
                }
        );
    }


}
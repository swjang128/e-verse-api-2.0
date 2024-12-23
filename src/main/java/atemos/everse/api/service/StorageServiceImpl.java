package atemos.everse.api.service;

import atemos.everse.api.dto.StorageDto;
import atemos.everse.api.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

/**
 * StorageServiceImpl는 StorageService 인터페이스를 구현한 클래스입니다.
 * 특정 업체의 데이터베이스 사용량 정보를 조회하는 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {
    private final JdbcTemplate jdbcTemplate;
    private final CompanyRepository companyRepository;

    @Value("${spring.datasource.database-name}")
    private String databaseName;

    /**
     * 주어진 업체 ID에 해당하는 데이터베이스 사용량을 조회합니다.
     *
     * @param companyId 조회할 업체의 ID
     * @return 업체의 데이터베이스 사용량 정보가 담긴 StorageResponse 객체
     */
    @Override
    @Transactional(readOnly = true)
    public StorageDto.StorageResponse getDataUsageByCompanyId(Long companyId) {
        // 주어진 업체 ID에 해당하는 업체가 존재하는지 확인
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such company."));
        // 각 테이블의 데이터 사용량을 조회하는 SQL 쿼리 정의
        String query = """
            SELECT table_name AS 'tableName',
                   data_length + index_length AS 'sizeInBytes'
            FROM information_schema.TABLES
            WHERE table_schema = ?
            AND table_name IN (
                SELECT DISTINCT table_name
                FROM (
                    SELECT 'alarm' AS table_name FROM alarm WHERE company_id = ?
                    UNION ALL
                    SELECT 'energy' AS table_name FROM energy WHERE iot_id IN (SELECT id FROM iot WHERE company_id = ?)
                    UNION ALL
                    SELECT 'api_call_log' AS table_name FROM api_call_log WHERE company_id = ?
                    UNION ALL
                    SELECT 'ai_forecast_energy' AS table_name FROM ai_forecast_energy WHERE company_id = ?
                    UNION ALL
                    SELECT 'anomaly' AS table_name FROM anomaly WHERE company_id = ?
                    UNION ALL
                    SELECT 'iot' AS table_name FROM iot WHERE company_id = ?
                    UNION ALL
                    SELECT 'iot_status_history' AS table_name FROM iot_status_history WHERE iot_id IN (SELECT id FROM iot WHERE company_id = ?)
                    UNION ALL
                    SELECT 'member' AS table_name FROM member WHERE company_id = ?
                    UNION ALL
                    SELECT 'metered_usage' AS table_name FROM metered_usage WHERE company_id = ?
                    UNION ALL
                    SELECT 'payment' AS table_name FROM payment WHERE company_id = ?
                    UNION ALL
                    SELECT 'subscription' AS table_name FROM subscription WHERE company_id = ?
                ) AS temp
            )
            """;
        // 테이블별 데이터 사용량 정보를 담는 리스트를 선언
        var tableStorageUsageList = new ArrayList<StorageDto.TableStorageUsage>();
        // 쿼리를 실행하고 결과를 TableStorageUsage 객체로 변환하여 리스트에 추가
        try {
            jdbcTemplate.query(query,
                    new Object[]{
                            databaseName,
                            companyId,
                            companyId,
                            companyId,
                            companyId,
                            companyId,
                            companyId,
                            companyId,
                            companyId,
                            companyId,
                            companyId,
                            companyId
                    },
                    rs -> {
                        var tableStorageUsage = StorageDto.TableStorageUsage.builder()
                                .tableName(rs.getString("tableName"))
                                .sizeInBytes(rs.getLong("sizeInBytes"))
                                .build();
                        tableStorageUsageList.add(tableStorageUsage);
                    });
        } catch (Exception e) {
            log.error("Error fetching data usage for company ID: {}", companyId, e);
            throw new RuntimeException("Error fetching data usage", e);
        }
        // 각 테이블의 데이터 사용량 정보를 종합하여 전체 사용량 계산
        var totalStorageUsage = tableStorageUsageList.stream()
                .mapToLong(StorageDto.TableStorageUsage::getSizeInBytes)
                .sum();
        // 최종 응답 DTO 생성 및 반환
        return StorageDto.StorageResponse.builder()
                .companyId(companyId)
                .companyName(company.getName())
                .tableStorageUsageList(tableStorageUsageList)
                .totalStorageUsage(totalStorageUsage)
                .build();
    }
}
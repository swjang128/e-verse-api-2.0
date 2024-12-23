package atemos.everse.api.specification;

import atemos.everse.api.dto.EnergyRateDto;
import atemos.everse.api.entity.EnergyRate;
import org.springframework.data.jpa.domain.Specification;

public class EnergyRateSpecification {
    /**
     * 주어진 EnergyRateDto.ReadEnergyRateRequest를 기반으로 EnergyRate 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readEnergyRateRequestDto 에너지 요금 조회 조건을 포함하는 데이터 전송 객체
     * @return 조건에 맞는 EnergyRate 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<EnergyRate> findWith(EnergyRateDto.ReadEnergyRateRequest readEnergyRateRequestDto) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            // 에너지 요금 ID 조건 추가
            if (readEnergyRateRequestDto.getEnergyRateId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readEnergyRateRequestDto.getEnergyRateId()));
            }
            // 국가 ID 조건 추가
            if (readEnergyRateRequestDto.getCountryId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("country").get("id").in(readEnergyRateRequestDto.getCountryId()));
            }
            // 산업용 전력 요금 최소/최대 조건 추가
            if (readEnergyRateRequestDto.getMinimumIndustrialRate() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("industrialRate"), readEnergyRateRequestDto.getMinimumIndustrialRate()));
            }
            if (readEnergyRateRequestDto.getMaximumIndustrialRate() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("industrialRate"), readEnergyRateRequestDto.getMaximumIndustrialRate()));
            }
            // 상업용 전력 요금 최소/최대 조건 추가
            if (readEnergyRateRequestDto.getMinimumCommercialRate() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("commercialRate"), readEnergyRateRequestDto.getMinimumCommercialRate()));
            }
            if (readEnergyRateRequestDto.getMaximumCommercialRate() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("commercialRate"), readEnergyRateRequestDto.getMaximumCommercialRate()));
            }
            // 피크 시간대 요금 증감율 최소/최대 조건 추가
            if (readEnergyRateRequestDto.getMinimumPeakMultiplier() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("peakMultiplier"), readEnergyRateRequestDto.getMinimumPeakMultiplier()));
            }
            if (readEnergyRateRequestDto.getMaximumPeakMultiplier() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("peakMultiplier"), readEnergyRateRequestDto.getMaximumPeakMultiplier()));
            }
            // 경피크 시간대 요금 증감율 최소/최대 조건 추가
            if (readEnergyRateRequestDto.getMinimumMidPeakMultiplier() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("midPeakMultiplier"), readEnergyRateRequestDto.getMinimumMidPeakMultiplier()));
            }
            if (readEnergyRateRequestDto.getMaximumMidPeakMultiplier() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("midPeakMultiplier"), readEnergyRateRequestDto.getMaximumMidPeakMultiplier()));
            }
            // 비피크(할인) 시간대 요금 증감율 최소/최대 조건 추가
            if (readEnergyRateRequestDto.getMinimumOffPeakMultiplier() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("offPeakMultiplier"), readEnergyRateRequestDto.getMinimumOffPeakMultiplier()));
            }
            if (readEnergyRateRequestDto.getMaximumOffPeakMultiplier() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("offPeakMultiplier"), readEnergyRateRequestDto.getMaximumOffPeakMultiplier()));
            }
            return predicate;
        };
    }
}
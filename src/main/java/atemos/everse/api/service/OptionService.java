package atemos.everse.api.service;

import atemos.everse.api.dto.OptionDto;

/**
 * 서비스 전반적인 설정 상태를 제어하는 서비스 인터페이스입니다.
 * 이 인터페이스는 서비스 전반적인 설정 상태를 제어하는 기능과 관련된 작업을 처리하는 메소드를 정의합니다.
 */
public interface OptionService {
    /**
     * 서비스 전반적인 설정 상태를 조회합니다.
     *
     * @return 서비스 전반적인 설정 상태
     */
    OptionDto.OptionResponse read();
}
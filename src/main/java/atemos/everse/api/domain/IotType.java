package atemos.everse.api.domain;

/**
 * IoT 장비의 유형을 나타내는 열거형입니다.
 */
public enum IotType {
    /**
     * 에어컨을 나타냅니다. 공기 조절 장비로 온도를 조절합니다.
     */
    AIR_CONDITIONER,
    /**
     * 모터를 나타냅니다. 회전 또는 운동을 발생시키는 기계적 장비입니다.
     */
    MOTOR,
    /**
     * 라디에이터를 나타냅니다. 열을 방출하여 공간을 난방하는 장비입니다.
     */
    RADIATOR,
    /**
     * 기타 장비를 나타냅니다. 위의 카테고리에 포함되지 않는 다른 IoT 장비를 의미합니다.
     */
    ETC
}
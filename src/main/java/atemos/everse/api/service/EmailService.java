package atemos.everse.api.service;

import jakarta.mail.MessagingException;

/**
 * EmailService는 이메일 전송과 관련된 기능을 제공하는 서비스 인터페이스입니다.
 *
 * 이 인터페이스는 수신자, 제목, 본문을 포함한 이메일을 발송하는 기능을 제공합니다.
 */
public interface EmailService {

    /**
     * 이메일을 발송합니다.
     *
     * @param email 수신자 이메일 주소입니다. 이메일을 받을 사용자의 이메일 주소를 지정합니다.
     * @param subject 이메일 제목입니다. 발송할 이메일의 제목을 설정합니다.
     * @param text 이메일 본문입니다. 이메일의 내용으로, 텍스트 형태의 메시지를 포함합니다.
     * @throws MessagingException 이메일 전송 중 발생할 수 있는 예외입니다.
     *                             이메일 서버와의 연결 문제, 인증 실패 등으로 인해 이메일 전송에 실패할 경우 발생할 수 있습니다.
     */
    void sendEmail(String email, String subject, String text) throws MessagingException;
}
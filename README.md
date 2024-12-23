# E-Verse 2.0

### **규칙**

모든 API 요청을 전송하는 기본 URL은 `localhost:8080/atemos`입니다.

E-Verse 2.0 API는 페이지와 데이터베이스 리소스에 대한 `GET`, `POST`, `PUT`, `DELETE` 요청을 통해 대부분 작업을 수행하는 등 가능한 한 RESTful 규칙을 따릅니다. 기능에 따라 RESTful 규칙을 위배할 수 있습니다. 요청과 응답 본문은 JSON으로 인코딩됩니다.

**매핑 규칙**
- Context path 이름은 `kebab-case` 를 사용합니다.

**테이블 규칙**
- 속성 이름은 `snake_case` 를 사용합니다.

**JSON 규칙**
- 속성 이름은 `Camel Case` 를 사용합니다.
- 시간 값(날짜와 일시)은 [ISO 8601](https://ko.wikipedia.org/wiki/ISO_8601) 문자열로 인코딩됩니다. 일시는 시간 값(`2020-08-12T02:12:33.231Z`)을 포함하며, 날짜는 날짜(`2020-08-12`)만 포함합니다.
- E-Verse 2.0 API는 빈 문자열을 지원하지 않습니다. 예를 들어, `url` [속성값 개체](https://developers.notion.com/reference/property-value-object)와 같은 속성의 문자열 값을 설정 해제하려면 `""` 대신 명시적인 `null`을 사용하세요.

---

## 프로젝트 실행 방법
별도로 첨부된 .env.{environment} 파일의 변수를 참조해주세요.

### 0. GitHub 소스 Clone
https://github.com/atemos01/e-verse-2.0.git
- *Branch*: `origin/develop`
  - git push는 백엔드 담당자에게 문의주세요.

### 1. 프로그램 설치
- Java 21
  - https://www.oracle.com/kr/java/technologies/downloads/#java21
- MySQL 8.0.37
  - https://dev.mysql.com/downloads/mysql/8.0.html
- IntelliJ Community
  - https://www.jetbrains.com/ko-kr/idea/download/download-thanks.html?platform=windows&code=IIC

### 2. 데이터베이스 생성
create database {DATABASE_NAME};

### 3. 환경설정 파일 세팅
위 설정 파일의 내용을 프로젝트 환경에 맞게 변경해주세요.

### 4. IntelliJ에서 스프링부트 메인 클래스 실행 설정
- 메인 클래스 EVerseApplication의 Edit Configurations.. 로 접근합니다.
- Environment variables 탭에서 파일을 선택합니다.
- 별도로 첨부된 .env.{environment} 파일을 선택합니다.
- 설정 Save 후 스프링부트 프로젝트를 실행합니다.

### 5. API 테스트 가이드
https://www.notion.so/API-a0aeefb8633d493097ff3cb181093d73

---


## 상태 코드

HTTP 응답 코드는 일반적인 성공과 오류 클래스를 나타내는 데 사용됩니다.

### 성공 코드

| HTTP 상태 | 설명 |
| --- | --- |
| 200 | 성공적으로 처리된 요청 |

### 오류 코드

오류 응답 본문의 `“status"`와 `"message"` 속성에서 오류에 대한 더 구체적인 세부 정보를 확인할 수 있습니다.

| HTTP 상태명 | status | message |
| --- | --- | --- |
| 400 | 400 | Bad request |
| 500 | 500 | Internal server error |



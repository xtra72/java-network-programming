== Quiz-13. Client에서 보내온 메시지를 접속한 모든 client 또는 특정 client에 전송할 수 있도록 multi-chatting client/server를 만들어 보자.

{empty} +

=== 요구 사항

==== Server

* 기본 기능
** 다중 접속 지원
** client는 고유 ID를 가짐
*** 서버 연결 후 첫 메시지로 전송
*** 서로 다른 연결에서 동일한 id로 접속시 중복 메시지 전송 후 연결 끊기
* Service Port
** Command line argument로 지정 (옵션 : -p <port>)
** 지정이 없는 경우, 1234
* 서버 관리 명령
** 서버 관리를 위한 콘솔을 지원
** client_list - 접속 사용자 목록
** deny add <client_id> - 사용자 접속 차단 등록
** deny del <client_id> - 사용자 접속 차단 해제
** monitor on/off - 사용자간 메시지 감시 설정/해제
** send_off <client_id> - 강퇴시키다.
** log show [s] [n] - 로그를 출력하다
*** 추가 옵션이 등을 경우, 최근 10개만 출력
*** 추가 옵션이 하나 있을 경우, 최근 n개만 출력
*** 추가 옵션이 둘 있을 경우, 최근 s개를 지나, n개만 출력
* 설정 정보
** 설정 정보를 json 형식으로 파일에 저장
** 실행시 읽고, 변경시 저장
** 접속 제한 목록, 접속 로그(log4j등을 이용한 로그 정보 아님) 등
*** client가 연결되거나 끊어지면, client 정보 출력(client의 ip와 port 남김)
*** client 연결 후, 전송되는 사용자 정보 출력(client id)
* Logging
** log4j, slf4j, logback등의 logging module을 이용해 trace를 위한 동작 정보를 파일에 남김

==== Client

* 기본 기능
** 서버 접속
*** command line argument를 이용해 호스트 (옵션 : -H)와 포트 (옵션 : -p <port>) 지정 가능하며, 설정 파일보다 우선함
*** 설정 파일을 통해 지정 가능
** 서버 접속 후 id 전송
*** Client마다 고유 id 부여

==== Message 형식

* JSON format 적용

**접속 요청**

** 요청
+
[source,json]
----
{ "id" : 1, "type" : "connect", "client_id" : "0000001"}
----
**** id - 메시지 id로 메시지 전송시마다 증가
**** type -  메시지 종류
** 응답 - 접속 허용
+
[source,json]
----
{ "id" : 1, "type" : "connect", "response" : "ok", "client_id" : "0000001"}
----
**** id - 요청에 대한 응답인 경우, 동일한 id사용
**** type -  메시지 종류
**** response - 성공 응답
** 응답 -접속 차단
+
[source,json]
----
{ "id" : 1, "type" : "connect", "response" : "deny", "client_id" : "0000001"}
----
**** id - 요청에 대한 응답인 경우, 동일한 id사용
**** type -  메시지 종류
**** response - 실패 응답

**메시지 전송**

** 전송 요청
+
[source,json]
----
{
    "id" : 2,
    "type" : "message",
    "target_id" : "0000002",
    "message" : "hello"
}
*** Client에서 서버에 전송
*** target_id - 메시지 전달 대상 client id(최종 수신자)
** 전달
+
{
    "id" : 2,
    "type" : "message",
    "client_id" : "0000001",
    "message" : "hello"
}
----
*** Server에서 대상 client로 전송
*** client_id - 메시지를 생성해서 보낸  client의 id(최초 발신자)

**접속자 명단 확인**

** 서버에 접속자 명단을 요청할 수 있다.

** 요청
+
[source,json]
----
{
    "id" : 3,
    "type" : "client_list"
}
----
** 응답
+
[source,json]
----
{
    "id" : 3,
    "cmd" : "client_list",
    "client_id" : [
        "1234567",
        "0000001",
        "0102030"
    ]
}
----

==== 설정 정보

** Client ID
** Server IP/Port
** 수신 메시지 차단 사용자 목록

---
link:../02.java_socket_Communication.adoc[돌아가기]
# SpringSecurity-twostagesauth
Customizing Spring Security for Multiple Stage Authentication


## Description
- 스프링 시큐리티에서는 N차 인증 기능을 기본적으로 제공하지 않음
- 이에 따라, 스프링 시큐리티의 기본 인증 아키텍처를 커스터마이징하여 다단계 인증 기능을 제공하는 필터를 개발함
- 예를 들어, 1차는 ID/PASSWORD, 2차는 SMS 코드를 인증하여 두단계를 거쳐야 로그인할 수 있는 기능 구현이 가능함


## 아키텍처 및 동작방식

![springsecurity-mutliauth](https://github.com/chrismrkr/springsecurity-twostagesauth/assets/62477958/158cdc65-55ac-498a-ba3b-c8933818992b)

```javascript
LoginRequestDTO: {
  "username": String, /* 로그인 아이디(식별자) */
  "password": String, /* 패스워드(인증 코드) */
  "authenticationProcessLevel": Integer /* 인증 요청 단계 */
}
```


- POST /login 요청을 보내면 FilterProxyChain을 통해 MultiStageAuthenticationFilter가 전달 받음
- 사용자가 어떤 인증 단계를 거쳐야 할지 확인함(Redis 저장소에 사용자의 현재까지 완료된 인증 단계가 저장됨)
  - 만약 사용자가 1단계 인증을 마쳤다면, Redis 저장소에는 {사용자1 -> "1"} 형태로 저장되어 있음
  - 사용자가 요청한 인증 단계(authenticationProcessLevel)가 해야할 인증 단계에 부합되지 않으면 Exception
    - 예를 들어, 현재 완료된 인증 단계는 1단계인데 3단계 인증을 요청하면 Exception 발생!
- MultiAuthenticationManager가 현재 인증 단계에 맞는 Authentication Provider를 호출함
  - 예를 들어, 2단계 인증(authenticationProccessLevel == 2)이면 두번째 Authentication Provider를 호출
- Authentication Provider에서 실질적인 인증 로직을 담당함
  - 예를 들어, 첫번째 Provider는 ID/PASSWORD, 두번째 Provider는 SMS 인증 코드 등으로 검증할 수 있음
  - 검증 성공 시 SuccessHandler, 실패 시 FailureHandler 호출
    - SuccessHandler : 현재 시도했던 인증 단계를 Redis 저장소에 Update 후 Return
    - FailureHandler : throw error








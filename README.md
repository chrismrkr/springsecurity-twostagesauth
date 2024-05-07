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


## 사용 방법
- 개발자는 Authentication Provider, SuccessHandler, FailureHandler를 구현해야함
  - 예를  들어, 1차 인증은 비밀번호 인증, 2차 인증은 이메일을 통한 코드 인증이 필요하면,
  - 비밀번호 인증을 처리하는 첫번째 Provider를 구현하고
  - 이메일 인증을 처리하는 두번째 Provider를 구현한다.
  - 그리고, 인증 성공 및 실패에 따른 처리를 담당할 Success 및 Failure Handler를 구현한다. 
- 필요한 것 구현을 마친 후 SecurityConfig 클래스에 아래와 같이 등록하면 됨(provider는 인증 단계와 List 순서가 동일해야 함)
```java
    @Bean
    public AuthenticationManager multiStageAuthenticationManager() throws Exception {
        List<AuthenticationProvider> providers = new ArrayList<>();
        providers.add(firstStepAuthenticationProvider);
        providers.add(secondStepAuthenticationProvider);
        return new MultiStageAuthenticationManager(providers);
    }
    @Bean
    public MultiStageAuthenticationFilter multiStageAuthenticationFilter() throws Exception {
        MultiStageAuthenticationFilter multiStageAuthenticationFilter = new MultiStageAuthenticationFilter(multiStageAuthenticationManager(), memberAuthenticationLevelRepository);
        multiStageAuthenticationFilter.setAuthenticationSuccessHandler(multiStageAuthenticationSuccessHandler);
        multiStageAuthenticationFilter.setAuthenticationFailureHandler(multiStageAuthenticationFailureHandler);
        return multiStageAuthenticationFilter;
```

## Demo in docker
- 소스에 구현된 데모는 2단계 인증이고, 아래 두단계를 거쳐서 로그인할 수 있음
  - 1단계 : ID/PASSWORD 인증
  - 2단계 : 인증코드 입력(0000 입력하면 통과하도록 간단히 구현됨)
 
- Step 1. git clone [url]
- Step 2. jar 파일 생성
- Step 3. docker compose up
  
- 회원 가입
```javascript
// POST /member/register
MemberRegisterRequestDto : {
  username: "user1"
  password: "passwd1"
}
```
- 1단계 인증
```javascript
// POST /login
LoginRequestDto : {
  username: "user1"
  password: "passwd1",
  authenticationProcessLevel: 1
}
```

- 2단계 인증
```javascript
// POST /login
LoginRequestDto : {
  useranme: "user1",
  password: "0000",
  authenticationProccessLevel: 2
}
```







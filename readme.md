# MultiFactor Authenticaion (MFA)

## 서비스 흐름도

### 1. GET /

![mfa1](https://user-images.githubusercontent.com/62477958/226382762-2c02acaa-f56b-4ddc-9e53-ccd1f4838382.png)

메인 페이지 접근을 위해 GET /를 호출하면 로그인이 되지 않은 상태에서는 권한이 없다. 즉, SecurityContext에 저장된 토큰은 AnonymousAuthenticationToken이다.

그러므로, /login 페이지로 redirect되도록 AuthenticationEntryPoint를 커스터마이징 해야 한다.

### 2. GET /login

![mfa2](https://user-images.githubusercontent.com/62477958/226382865-e6159ad6-95d5-4125-a5d9-083288aa8884.png)

/login은 모든 사용자가 권한이 없어도 접근이 가능해야 한다. 

이를 위해 **MfaFilterSecurityInterceptor를 커스터마이징** 하였다. MfaFilterSecurityInterceptor란 서블릿으로 들어오는 모든 요청을 받아내는 역할을 한다.

permitAllResource 리스트를 생성하여 특정 url은 인증 및 인가 절차없이 바로 접근할 수 있도록 커스터마이징 했다. 아래와 유사하게 동작한다.

```java
if(permitAllResources.contains(url)) {
      return;
}
super.beforeInvocation();
```

### 3. 인증 프로세스 시작

GET /login이 호출되면 인증 프로세스가 시작된다.

스프링 시큐리티에서 제공하는 1차 인증과 달리 2차 인증이 필요하므로 AuthenticationManager의 AuthenticationProvider 사용 방식을 변경하였다.

SecurityContext에 저장되어 있는 **AuthenticationToken의 Type에 따라 적절한 ProviderManager를 불러오도록 커스터마이징** 하였다.

현재 AuthenticationToken은 AnonymousAuthenticationToken이므로 1차 인증용 AuthenticationProvider가 작동한다.

인증이 완료되면 SecurityContext에 새롭게 생성된 MfaAuthenticationToken을 저장한 후, **SuccessHandler를 GET /second-login을 호출**하도록 하였다.



![mfa3](https://user-images.githubusercontent.com/62477958/226382956-dfee7f8c-c4f2-4ee2-99e5-bc72fc880488.png)

**AccesssDecisonManager를 커스터마이징**하는 것이 필요했다.

MfaAuthenticationToken의 AuthLevel이 1이고, request가 GET /second-login이라면 해당 페이지에 접근할 수 있도록 수정했다.

만약 MfaAuthenticationToken의 AuthLevel이 1이고, GET /second-login이외의 url에 접근하고자 한다면,

Exception을 발생시켜 현재 필요한 인증단계로 redirect한다.


![mfa4](https://user-images.githubusercontent.com/62477958/226383041-6351df60-57a4-4322-b129-0a934cef5337.png)

현재 MfaAuthenticationToken.authLevel은 1이다. 그러므로, 이에 맞는 Provider인 SubAuthenticaionProvider가 동작하도록 하였다.

해당 Provider를 통해 authLevel이 2인 MfaAuthenticationToken을 생성해 SecurityContext에 저장한 후,

SuccessHandler를 통해 메인 페이지로 redirect한다.(GET /)


모든 인증이 완료되었으므로 메인 페이지에 접근할 수 있다.


## 트러블 슈팅

### 1. HTTP Status 403 - Invalid CSRF Token 'null' was found on the request parameter '_csrf' or header 'X-CSRF-TOKEN'.

2차 인증(GMail 인증) 페이지에서 ajax 통신을 통해 PIN 발급 시, 아래의 에러가 발생하였다.

**HTTP Status 403 - Invalid CSRF Token 'null' was found on the request parameter '_csrf' or header 'X-CSRF-TOKEN'.**

찾아보니 CSRF 공격에 대한 방어가 안되어 있어서 발생한 문제였다. csrf 헤더와 토큰을 추가해서 해결하였다.

csrf 공격은 쿠키-세션 방식 인증에서 발생할 수 있는 보안 취약점이다.





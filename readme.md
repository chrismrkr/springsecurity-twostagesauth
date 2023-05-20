# MultiFactor Authenticaion (MFA)

## 서비스 흐름도

![mfa1](https://user-images.githubusercontent.com/62477958/226382762-2c02acaa-f56b-4ddc-9e53-ccd1f4838382.png)

메인 페이지 접근을 위해 GET /를 호출했다고 하자. **AuthenticationEntryPoint를 커스터마이징** 하는 것이 필요하였다.

현재 SecurityContext에 저장된 토큰은 AnonymousAuthenticationToken이므로 /login 페이지로 redirect되도록 AuthenticationEntryPoint를 커스터마이징 해야 한다.

![mfa2](https://user-images.githubusercontent.com/62477958/226382865-e6159ad6-95d5-4125-a5d9-083288aa8884.png)

/login 페이지는 모든 사용자가 접근이 가능해야 한다. 이를 위해 **MfaFilterSecurityInterceptor를 커스터마이징** 하였다.

특정 url은 인증 및 인가 절차없이 바로 접근할 수 있도록 수정하였다.


복수 인증이 필요하므로 AuthenticationManager는 다수의 AuthenticationProvider를 가지도록 수정하였다.

SecurityContext에 저장되어 있는 **AuthenticationToken의 Type에 따라 적절한 ProviderManager를 불러오도록 커스터마이징** 하였다.

현재 AuthenticationToken은 AnonymousAuthenticationToken이므로 FormAuthenticationProvider가 작동한다.

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

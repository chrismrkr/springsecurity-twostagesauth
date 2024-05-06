# SpringSecurity-twostagesauth
Customizing Spring Security for Multiple Stage Authentication


## Description
- 스프링 시큐리티에서는 여러 단계 인증 기능을 기본적으로 제공하지 않음
- 이에 따라, 스프링 시큐리티의 기본 인증 아키텍처를 커스터마이징하여 다단계 인증 기능을 제공하는 필터를 개발함
- 예를 들어, 1차는 ID/PASSWORD, 2차는 SMS 코드를 인증하여 두단계를 거쳐야 로그인할 수 있는 기능 구현이 가능함


## 아키텍처 및 동작방식





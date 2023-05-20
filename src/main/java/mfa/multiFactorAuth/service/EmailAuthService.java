package mfa.multiFactorAuth.service;

public interface EmailAuthService {
    String sendAuthEmail(String receiver) throws Exception;
}

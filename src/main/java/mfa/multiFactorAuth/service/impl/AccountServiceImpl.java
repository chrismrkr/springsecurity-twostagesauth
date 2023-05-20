package mfa.multiFactorAuth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfa.multiFactorAuth.domain.Account;
import mfa.multiFactorAuth.domain.AccountRole;
import mfa.multiFactorAuth.domain.Role;
import mfa.multiFactorAuth.repository.AccountRepository;
import mfa.multiFactorAuth.repository.AccountRoleRepository;
import mfa.multiFactorAuth.repository.RoleRepository;
import mfa.multiFactorAuth.service.AccountService;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.Set;

@Service("accountService")
@Slf4j
@RequiredArgsConstructor
@DependsOn("roleService")
public class AccountServiceImpl implements AccountService {
    private final AccountRepository userRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public void saveAccount(Account account) {
        userRepository.save(account);
        Role initRole = roleRepository.findByRoleName("ROLE_USER");

        AccountRole accountRole = AccountRole.builder().account(account).role(initRole).build();
        accountRoleRepository.save(accountRole);
    }


    @PostConstruct
    @Transactional
    void createInitUser() {
        Account account = Account.builder().username("user")
                .password(passwordEncoder.encode("1111"))
                .age(28)
                .build();
        saveAccount(account);
    }

}

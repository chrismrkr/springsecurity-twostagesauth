package mfa.multiFactorAuth.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfa.multiFactorAuth.domain.Account;
import mfa.multiFactorAuth.domain.AccountRole;
import mfa.multiFactorAuth.domain.Role;
import mfa.multiFactorAuth.repository.AccountRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Service
public class FormUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username);
        if(account == null) {
            throw new UsernameNotFoundException("UsernameNotFoundException");
        }

        Set<GrantedAuthority> roles = new HashSet<>();
        for(AccountRole accountRole : account.getAccountRoleSet()) {
            roles.add(new SimpleGrantedAuthority(accountRole.getRole().getRoleName()));
        }

        AccountContext accountContext = new AccountContext(account, roles);
        return accountContext;
    }
}

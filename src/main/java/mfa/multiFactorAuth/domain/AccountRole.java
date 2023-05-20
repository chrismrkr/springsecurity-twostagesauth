package mfa.multiFactorAuth.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
public class AccountRole {
    @Id
    @GeneratedValue
    @Column(name = "account_role_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    public static class Builder {
        private Account account;
        private Role role;
        public Builder account(Account account) {
            this.account = account;
            return this;
        }
        public Builder role(Role role) {
            this.role = role;
            return this;
        }
        public AccountRole build() {
            return new AccountRole(this);
        }
    }
    public static Builder builder() {
        return new Builder();
    }
    private AccountRole(Builder builder) {
        this.account = builder.account;
        this.role = builder.role;
    }
    protected AccountRole() {}
}

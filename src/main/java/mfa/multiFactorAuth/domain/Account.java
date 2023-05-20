package mfa.multiFactorAuth.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
public class Account {
    @Id
    @GeneratedValue
    @Column(name="user_id")
    private Long id;

    private String username;
    private String password;
    private int age;
    @OneToMany(mappedBy = "account")
    private Set<AccountRole> accountRoleSet = new HashSet<>();

    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private String username;
        private String password;
        private int age;
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        public Builder age(int age) {
            this.age = age;
            return this;
        }
        public Account build() {
            return new Account(this);
        }
    }
    private Account(Builder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.age = builder.age;
    }
    protected Account() {}
}

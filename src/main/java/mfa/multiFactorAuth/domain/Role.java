package mfa.multiFactorAuth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
public class Role {
    @Id
    @GeneratedValue
    @Column(name="role_id")
    private Long id;
    private String roleName;
    private String roleDescription;

    @OneToMany(mappedBy = "role")
    private Set<AccountRole> accountRoleSet = new HashSet<>();

    @OneToMany(mappedBy = "resource")
    private Set<RoleResource> roleResourceSet = new HashSet<>();


    public static class Builder {
        private String roleName;
        private String roleDescription;
        public Builder roleName(String roleName) {
            this.roleName = roleName;
            return this;
        }
        public Builder roleDescription(String roleDescription) {
            this.roleDescription = roleDescription;
            return this;
        }
        public Role build() {
            return new Role(this);
        }
    }
    private Role(Builder builder) {
        this.roleName = builder.roleName;
        this.roleDescription = builder.roleDescription;
    }
    protected Role() {}
    public static Builder builder() {
        return new Builder();
    }
}
// Role.builder().roleName(roleName).roleDescription(roleDescription).build();

package mfa.multiFactorAuth.domain;

import lombok.Getter;
import javax.persistence.*;

@Entity
@Getter
public class RoleResource {
    @Id
    @GeneratedValue
    @Column(name = "role_resource_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    public static class Builder {
        private Resource resource;
        private Role role;
        public Builder resource(Resource resource) {
            this.resource = resource;
            return this;
        }
        public Builder role(Role role) {
            this.role = role;
            return this;
        }
        public RoleResource build() {
            return new RoleResource(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    protected RoleResource() {}
    private RoleResource(Builder builder) {
        this.resource = builder.resource;
        this.role = builder.role;
    }
}

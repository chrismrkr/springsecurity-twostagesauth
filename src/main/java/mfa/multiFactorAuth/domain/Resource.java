package mfa.multiFactorAuth.domain;


import lombok.Getter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
public class Resource {
    @Id
    @GeneratedValue
    @Column(name = "resource_id")
    private Long id;
    private String resourceName;
    private Integer orderNum;

    @OneToMany(mappedBy = "resource")
    private Set<RoleResource> roleResourceSet = new HashSet<>();


    public static class Builder {
        private String resourceName;
        private Integer orderNum;
        public Builder resourceName(String resourceName) {
            this.resourceName = resourceName;
            return this;
        }
        public Builder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }
        public Resource build() {
            return new Resource(this);
        }
    }
    public static Builder builder() {
        return new Builder();
    }
    protected Resource() {}
    private Resource(Builder builder) {
        this.resourceName = builder.resourceName;
        this.orderNum = builder.orderNum;
    }
}

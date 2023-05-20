package mfa.multiFactorAuth.domain;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
public class Pin {
    @Id
    private String messengerId;
    private Long pinNumber;
    private Pin(Builder builder) {
        this.pinNumber = builder.pinNumber;
        this.messengerId = builder.messengerId;
    }
    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private String messengerId;
        private Long pinNumber;
        public Builder messengerId(String messengerId) {
            this.messengerId = messengerId;
            return this;
        }
        public Builder pinNumber(Long pinNumber) {
            this.pinNumber = pinNumber;
            return this;
        }
        public Pin build() {
            return new Pin(this);
        }
    }
    // Pin pin = Pin.builder().messengerId().pinNumber().build();
}

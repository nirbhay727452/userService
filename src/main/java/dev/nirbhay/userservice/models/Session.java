package dev.nirbhay.userservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;

@Entity
@Getter
@Setter
public class Session extends BaseModel {
    private String token;
    private Date expiringAt;
    @ManyToOne
    private User user;
    @Enumerated(EnumType.ORDINAL)
    private SessionStatus sessionStatus;

    @Column(name = "expiring_at")
    private Instant expiringAt1;

    @Column(name = "session_status")
    private Byte sessionStatus1;

}

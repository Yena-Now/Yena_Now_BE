package com.example.yenanow.gallery.entity;

import com.example.yenanow.film.entity.Frame;
import com.example.yenanow.users.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "relay")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Relay {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "relay_uuid", length = 36, nullable = false)
    private String relayUuid;

    @Column(name = "time_limit", nullable = false)
    private Integer timeLimit;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "take_count", nullable = false)
    private Integer takeCount;

    @Column(name = "cut_count", nullable = false)
    private Integer cutCount;

    @Column(name = "background_url", length = 200, nullable = false)
    private String backgroundUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frame_uuid")
    private Frame frame;
    
    @Builder.Default
    @OneToMany(mappedBy = "relay", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RelayParticipant> participants = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "relay", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RelayCut> cuts = new ArrayList<>();

    public void addParticipant(RelayParticipant participant) {
        this.participants.add(participant);
        participant.setRelay(this);
    }

    public void addCut(RelayCut cut) {
        this.cuts.add(cut);
        cut.setRelay(this);
    }
}

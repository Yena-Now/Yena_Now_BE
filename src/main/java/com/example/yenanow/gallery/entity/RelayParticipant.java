package com.example.yenanow.gallery.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "relay_cut")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RelayParticipant {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "cut_uuid", length = 36, nullable = false)
    private String cutUuid;

    @Column(name = "cut_url", length = 200)
    private String cutUrl;

    @Column(name = "cut_index", nullable = false)
    private Integer cutIndex;

    @Column(name = "is_taken", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isTaken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relay_uuid")
    private Relay relay;
}

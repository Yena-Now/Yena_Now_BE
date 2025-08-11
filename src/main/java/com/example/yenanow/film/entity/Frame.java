package com.example.yenanow.film.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "frame")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Frame {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "frame_uuid", length = 36, nullable = false)
    private String frameUuid;

    @Column(name = "frame_name", length = 50, nullable = false)
    private String frameName;

    @Column(name = "frame_url", length = 200, nullable = false)
    private String frameUrl;

    @Column(name = "frame_cut", nullable = false)
    private Integer frameCut;

    @Column(name = "frame_type", nullable = false)
    private Integer frameType;
}

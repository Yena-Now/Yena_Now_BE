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
@Table(name = "background")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Background {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "background_uuid", length = 36, nullable = false)
    private String backgroundUuid;

    @Column(name = "background_url", length = 200, nullable = false)
    private String backgroundUrl;
}

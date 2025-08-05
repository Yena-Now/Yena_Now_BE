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
@Table(name = "sticker")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sticker {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "sticker_uuid", length = 36, nullable = false)
    private String stickerUuid;
    @Column(name = "stickerName", length = 20, nullable = false)
    private String stickerName;
    @Column(name = "stickerUrl", length = 200, nullable = false)
    private String stickerUrl;
}
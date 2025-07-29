package com.example.yenanow.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "follow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "follow_uuid", length = 36, nullable = false)
  private String followUuid;

  @Column(name = "from_user", length = 36, nullable = false)
  private String fromUser;   // 팔로우를 건 사용자 UUID

  @Column(name = "to_user", length = 36, nullable = false)
  private String toUser;     // 팔로우 대상 사용자 UUID

  public Follow(String fromUser, String toUser) {
    this.fromUser = fromUser;
    this.toUser = toUser;
  }
}

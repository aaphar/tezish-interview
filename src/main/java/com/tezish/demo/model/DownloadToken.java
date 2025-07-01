package com.tezish.demo.model;

import com.tezish.demo.enums.EStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity(name = "download_tokens")
public class DownloadToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private String fileName;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EStatus status;

    private Long userId;
}

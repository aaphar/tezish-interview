package com.tezish.demo.repository;

import com.tezish.demo.model.DownloadToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadTokenRepository extends JpaRepository<DownloadToken, Long> {
    DownloadToken findByToken(String token);

}

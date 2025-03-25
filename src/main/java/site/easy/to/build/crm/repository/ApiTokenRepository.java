package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import site.easy.to.build.crm.entity.ApiToken;
import site.easy.to.build.crm.entity.User;

import java.util.Optional;

public interface ApiTokenRepository extends JpaRepository<ApiToken, Long> {
    Optional<ApiToken> findByTokenAndRevokedFalse(String token);
    
    @Query("SELECT t FROM ApiToken t WHERE t.token = :token AND t.revoked = false AND t.expiresAt > CURRENT_TIMESTAMP")
    Optional<ApiToken> findValidToken(String token);
    
    void deleteByUser(User user);
}

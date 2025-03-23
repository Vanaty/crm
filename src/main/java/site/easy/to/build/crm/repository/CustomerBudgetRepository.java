package site.easy.to.build.crm.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import site.easy.to.build.crm.entity.CustomerBudget;

@Repository
public interface CustomerBudgetRepository extends JpaRepository<CustomerBudget, Integer> {
    List<CustomerBudget> findByCustomerCustomerId(Integer customerId);
    
    @Query("SELECT SUM(b.amount) FROM CustomerBudget b WHERE b.customer.customerId = :customerId")
    BigDecimal getTotalBudgetByCustomerId(@Param("customerId") Integer customerId);
}

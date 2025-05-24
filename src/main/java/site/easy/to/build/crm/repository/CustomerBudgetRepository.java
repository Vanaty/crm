package site.easy.to.build.crm.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    @Query("SELECT FUNCTION('DATE_FORMAT', b.createdAt, '%Y-%m') as month, SUM(b.amount) FROM CustomerBudget b GROUP BY month")
    List<Object[]> findTotalBudgetsPerMonth();

    @Query("SELECT b.customer.name,SUM(b.amount) FROM CustomerBudget b GROUP BY b.customer.customerId")
    List<Object[]> findTotalBudgetByCustomer();

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM CustomerBudget b")
    BigDecimal getTotalBudget();
}

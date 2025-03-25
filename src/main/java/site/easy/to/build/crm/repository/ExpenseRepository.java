package site.easy.to.build.crm.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import site.easy.to.build.crm.entity.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.customer.customerId = :customerId")
    BigDecimal getTotalExpensesByCustomerId(@Param("customerId") Integer customerId);
    List<Expense> findByCustomerCustomerId(Integer customerId);
    
    List<Expense> findByTicketTicketId(Integer ticketId);
    
    List<Expense> findByLeadLeadId(Integer leadId);
    
    List<Expense> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m') as month, SUM(e.amount) FROM Expense e GROUP BY month")
    List<Object[]> findTotalExpensesPerMonth();
}
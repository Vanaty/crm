package site.easy.to.build.crm.service.expense;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    // Save an expense
    public Expense save(Expense expense) {
        if (expense.getCreatedAt() == null) {
            expense.setCreatedAt(LocalDateTime.now());
        }
        return expenseRepository.save(expense);
    }

    // Find expense by ID
    public Expense findById(Integer expenseId) {
        Optional<Expense> expense = expenseRepository.findById(expenseId);
        return expense.orElse(null);
    }

    // Get all expenses
    public List<Expense> findAll() {
        return expenseRepository.findAll();
    }

    // Delete an expense
    public void delete(Expense expense) {
        expenseRepository.delete(expense);
    }

    // Delete by ID
    public void deleteById(Integer expenseId) {
        expenseRepository.deleteById(expenseId);
    }

    // Find expenses by customer ID
    public List<Expense> findByCustomerId(Integer customerId) {
        return expenseRepository.findByCustomerCustomerId(customerId);
    }

    // Find expenses by ticket ID
    public List<Expense> findByTicketId(Integer ticketId) {
        return expenseRepository.findByTicketTicketId(ticketId);
    }

    // Find expenses by lead ID
    public List<Expense> findByLeadId(Integer leadId) {
        return expenseRepository.findByLeadLeadId(leadId);
    }

    // Calculate total expenses for a customer
    public BigDecimal getTotalExpensesByCustomer(Integer customerId) {
        List<Expense> expenses = findByCustomerId(customerId);
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Find expenses within a date range
    public List<Expense> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return expenseRepository.findByCreatedAtBetween(startDate, endDate);
    }

    // Update an existing expense
    public Expense update(Expense expense) {
        if (expense.getExpenseId() == null) {
            throw new IllegalArgumentException("Expense ID cannot be null for update operation");
        }
        return expenseRepository.save(expense);
    }

    public BigDecimal getTotalExpenses() {
        return expenseRepository.findAll().stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
package site.easy.to.build.crm.service.budget;

import org.springframework.stereotype.Service;

import site.easy.to.build.crm.dto.BudgetExpenseDTO;
import site.easy.to.build.crm.dto.BudgetExpenseTimelineDTO;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.repository.CustomerBudgetRepository;
import site.easy.to.build.crm.repository.ExpenseRepository;
import site.easy.to.build.crm.repository.CustomerRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BudgetExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CustomerBudgetRepository budgetRepository;
    private final CustomerRepository customerRepository;

    public BudgetExpenseService(ExpenseRepository expenseRepository, CustomerBudgetRepository budgetRepository, CustomerRepository customerRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.customerRepository = customerRepository;
    }

    public BudgetExpenseTimelineDTO getBudgetExpenseTimeline() {
        Map<String, BudgetExpenseDTO> timeline = new HashMap<>();

        List<Object[]> expenses = expenseRepository.findTotalExpensesPerMonth();
        for (Object[] row : expenses) {
            String month = (String) row[0];
            BigDecimal totalExpense = (BigDecimal) row[1];
            timeline.put(month, new BudgetExpenseDTO(null,totalExpense, BigDecimal.ZERO));
        }

        List<Object[]> budgets = budgetRepository.findTotalBudgetsPerMonth();
        for (Object[] row : budgets) {
            String month = (String) row[0];
            BigDecimal totalBudget = (BigDecimal) row[1];
            timeline.computeIfAbsent(month, k -> new BudgetExpenseDTO(null,BigDecimal.ZERO, totalBudget))
                    .setBudget(totalBudget);
        }

        return new BudgetExpenseTimelineDTO(timeline);
    }

    public List<BudgetExpenseDTO> getBudgetExpenseClients() {
        List<Object[]> results = expenseRepository.findTotalExpensesPerCustomer();
        List<BudgetExpenseDTO> budgetExpenses = new ArrayList<>();
        for (Object[] row : results) {
            Integer customerId = (Integer) row[0];
            Customer customer = customerRepository.findById(customerId).orElse(null);
            BigDecimal totalExpense = (BigDecimal) row[1];
            BigDecimal totalBudget = (BigDecimal) row[2];
            budgetExpenses.add(new BudgetExpenseDTO(customer, totalExpense, totalBudget));
        }
        return budgetExpenses;
    }
}

package site.easy.to.build.crm.service.budget;

import org.springframework.stereotype.Service;

import site.easy.to.build.crm.dto.BudgetExpenseDTO;
import site.easy.to.build.crm.dto.BudgetExpenseTimelineDTO;
import site.easy.to.build.crm.repository.CustomerBudgetRepository;
import site.easy.to.build.crm.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BudgetExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CustomerBudgetRepository budgetRepository;

    public BudgetExpenseService(ExpenseRepository expenseRepository, CustomerBudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }

    public BudgetExpenseTimelineDTO getBudgetExpenseTimeline() {
        Map<String, BudgetExpenseDTO> timeline = new HashMap<>();

        List<Object[]> expenses = expenseRepository.findTotalExpensesPerMonth();
        for (Object[] row : expenses) {
            String month = (String) row[0];
            BigDecimal totalExpense = (BigDecimal) row[1];
            timeline.put(month, new BudgetExpenseDTO(totalExpense, BigDecimal.ZERO));
        }

        List<Object[]> budgets = budgetRepository.findTotalBudgetsPerMonth();
        for (Object[] row : budgets) {
            String month = (String) row[0];
            BigDecimal totalBudget = (BigDecimal) row[1];
            timeline.computeIfAbsent(month, k -> new BudgetExpenseDTO(BigDecimal.ZERO, totalBudget))
                    .setBudget(totalBudget);
        }

        return new BudgetExpenseTimelineDTO(timeline);
    }
}

package site.easy.to.build.crm.service.budget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import site.easy.to.build.crm.entity.BudgetSettings;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerBudget;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.exception.BudgetExceededException;
import site.easy.to.build.crm.repository.BudgetSettingsRepository;
import site.easy.to.build.crm.repository.CustomerBudgetRepository;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.repository.ExpenseRepository;
import site.easy.to.build.crm.type.BudgetStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class BudgetService {
    @Autowired
    private CustomerBudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BudgetSettingsRepository settingsRepository;

    @Autowired
    private BudgetSettingsRepository budgetSettingsRepository;

    public List<CustomerBudget> getBudgetsByCustomer(Integer customerId) {
        return budgetRepository.findByCustomerCustomerId(customerId);
    }

    public CustomerBudget saveBudget(CustomerBudget budget) {
        return budgetRepository.save(budget);
    }

    public CustomerBudget getBudgetById(Integer budgetId) {
        return budgetRepository.findById(budgetId)
            .orElseThrow(() -> new RuntimeException("Budget not found"));
    }

    public BigDecimal getTotalBudget(Integer customerId) {
        BigDecimal total = budgetRepository.getTotalBudgetByCustomerId(customerId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalExpenses(Integer customerId) {
        BigDecimal total = expenseRepository.getTotalExpensesByCustomerId(customerId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BudgetStatus checkBudgetStatus(Integer customerId) {
        BigDecimal totalBudget = getTotalBudget(customerId);
        BigDecimal totalExpenses = getTotalExpenses(customerId);
        if (totalBudget.compareTo(BigDecimal.ZERO) == 0) return BudgetStatus.OK;

        BigDecimal alertThreshold = getAlertThreshold();
        BigDecimal percentageUsed = totalExpenses.divide(totalBudget, 2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        if (totalExpenses.compareTo(totalBudget) > 0) {
            return BudgetStatus.EXCEEDED;
        } else if (percentageUsed.compareTo(alertThreshold) >= 0) {
            return BudgetStatus.ALERT;
        }
        return BudgetStatus.OK;
    }

    @Transactional
    public Expense addExpense(Expense expense) {
        Integer customerId = expense.getCustomer().getCustomerId();
        BigDecimal newTotalExpenses = getTotalExpenses(customerId).add(expense.getAmount());
        BigDecimal totalBudget = getTotalBudget(customerId);

        if (newTotalExpenses.compareTo(totalBudget) > 0) {
            throw new BudgetExceededException("Cette dépense dépasserait le budget total du client");
        }

        return expenseRepository.save(expense);
    }

    public Customer getCustomerById(Integer customerId) {
        return customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public BigDecimal getAlertThreshold() {
        List<BudgetSettings> settings = settingsRepository.findAll();
        if (settings.isEmpty()) {
            BudgetSettings defaultSettings = new BudgetSettings();
            settingsRepository.save(defaultSettings);
            return defaultSettings.getAlertThreshold();
        }
        return settings.get(0).getAlertThreshold(); // On prend le premier enregistrement
    }

    @Transactional
    public void updateAlertThreshold(BigDecimal newThreshold) {
        List<BudgetSettings> settings = settingsRepository.findAll();
        BudgetSettings budgetSettings;
        if (settings.isEmpty()) {
            budgetSettings = new BudgetSettings();
        } else {
            budgetSettings = settings.get(0);
        }
        budgetSettings.setAlertThreshold(newThreshold);
        settingsRepository.save(budgetSettings);
    }
    public BudgetSettings getBudgetSettings() {
        return budgetSettingsRepository.findFirstByOrderByIdAsc();
    }

    public Map<String,BigDecimal> getBudgetsPerCustomer() {
        List<Object[]> results = budgetRepository.findTotalBudgetByCustomer();
        Map<String, BigDecimal> budgetMap = new HashMap<>();

        for (Object[] row : results) {
            String customerName = (String) row[0];
            BigDecimal totalBudget = (BigDecimal) row[1];
            budgetMap.put(customerName, totalBudget);
        }

        return budgetMap;
    }
}
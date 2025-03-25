package site.easy.to.build.crm.dto;

import java.math.BigDecimal;

public class BudgetExpenseDTO {
    private BigDecimal expenses;
    private BigDecimal budget;

    public BudgetExpenseDTO(BigDecimal expenses, BigDecimal budget) {
        this.expenses = expenses;
        this.budget = budget;
    }

    public BigDecimal getExpenses() {
        return expenses;
    }

    public void setExpenses(BigDecimal expenses) {
        this.expenses = expenses;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }
}
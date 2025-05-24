package site.easy.to.build.crm.dto;

import java.math.BigDecimal;

import site.easy.to.build.crm.entity.Customer;

public class BudgetExpenseDTO {
    private Customer customer;
    private BigDecimal expenses;
    private BigDecimal budget;

    public BudgetExpenseDTO(Customer customer, BigDecimal expenses, BigDecimal budget) {
        this.customer = customer;
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
package site.easy.to.build.crm.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.repository.ExpenseRepository;
import site.easy.to.build.crm.service.budget.BudgetExpenseService;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.expense.ExpenseService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.ticket.TicketService;
import site.easy.to.build.crm.type.BudgetStatus;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final ExpenseRepository expenseRepository;

    private final BudgetExpenseService budgetExpenseService;
    @Autowired 
    private BudgetService budgetService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private LeadService leadService;

    @Autowired
    private TicketService ticketService;

    DashboardApiController(BudgetExpenseService budgetExpenseService, ExpenseRepository expenseRepository) {
        this.budgetExpenseService = budgetExpenseService;
        this.expenseRepository = expenseRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("clientCount", customerService.count());
        stats.put("leadCount", leadService.count());
        stats.put("ticketCount", ticketService.count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/expenses/stats")
    public ResponseEntity<?> getExpensesStats() {
        return ResponseEntity.ok(budgetExpenseService.getBudgetExpenseTimeline());
    }

    @GetMapping("/budgets/stats")
    public ResponseEntity<?> getBudgetsStats() {
        return ResponseEntity.ok(Map.of("byClient",budgetService.getBudgetsPerCustomer()));
    }    

    @GetMapping("/tickets/stats")
    public ResponseEntity<?> getTicketsStats() {
        Map<String,Integer> rep = ticketService.countByPriority();
        return ResponseEntity.ok(Map.of("priorities", rep));
    }

    @GetMapping("/leads")
    public ResponseEntity<?> getLeadDetails() {
        List<Lead> leads = leadService.findAll();
        return ResponseEntity.ok(
            leads.stream().map((lead) -> {
                Map <String,Object> map = new HashMap<>();
                map.put("id", lead.getLeadId());
                map.put("name", lead.getName());
                map.put("customer", lead.getCustomer().getName());
                map.put("employee", lead.getEmployee().getUsername());
                map.put("createdAt",lead.getCreatedAt());
                map.put("phone",lead.getPhone());
                map.put("status",lead.getStatus());
                return map;
            }).collect(Collectors.toList())
        );
    }

    @GetMapping("/tickets")
    public ResponseEntity<?> getTicketDetails() {
        List<Ticket> tickets = ticketService.findAll();
        return ResponseEntity.ok(
            tickets.stream().map((lead) -> {
                Map <String,Object> map = new HashMap<>();
                map.put("id", lead.getTicketId());
                map.put("subject", lead.getSubject());
                map.put("customer", lead.getCustomer().getName());
                map.put("employee", lead.getEmployee().getUsername());
                map.put("createdAt",lead.getCreatedAt());
                map.put("status",lead.getStatus());
                map.put("priority", lead.getPriority());
                return map;
            }).collect(Collectors.toList())
        );
    }

    @PutMapping("/expense/{id}")
    public ResponseEntity<?> updateExpenseAmount(@PathVariable Integer id, @RequestBody Map<String, Double> request) {
        Double newAmount = request.get("amount");
        Double force = request.getOrDefault("force",Double.valueOf(0));
        Expense expense = expenseService.findById(id);
        Integer customerId = expense.getCustomer().getCustomerId();
        BigDecimal totalBudget = budgetService.getTotalBudget(customerId);
        BigDecimal totalExpenses = budgetService.getTotalExpenses(customerId).subtract(expense.getAmount());
        BigDecimal newTotalExpenses = totalExpenses.add(BigDecimal.valueOf(newAmount));
        if (newTotalExpenses.compareTo(totalBudget) > 0 && force == 0) {
            return ResponseEntity.ok(Map.of("message", "Expense amount exceeds budget","status", "validation"));
        } else if (force == 1) {
            expense.setAmount(BigDecimal.valueOf(newAmount));
        } else {
            expense.setAmount(BigDecimal.valueOf(newAmount));
        }
        expenseService.save(expense);
        if (budgetService.checkBudgetStatus(customerId) == BudgetStatus.ALERT) {
            return ResponseEntity.ok(Map.of("message", "Attention : seuil d'alerte de " + budgetService.getAlertThreshold() + "% atteint pour le budget total!","status", "warning"));
        }
        return ResponseEntity.ok(Map.of("message", "Expense updated successfully","status", "success"));
    }

    @DeleteMapping("/leads/{id}")
    public ResponseEntity<?> deleteLead(@PathVariable Integer id) {
        Lead lead =  leadService.findByLeadId(id);
        leadService.delete(lead);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable Integer id) {
        ticketService.delete(ticketService.findByTicketId(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/alerts/budget")
    public ResponseEntity<?> setBudgetAlert(@RequestBody Map<String, Double> request) {
        Double alertThreshold = request.get("threshold");
        budgetService.updateAlertThreshold(BigDecimal.valueOf(alertThreshold));
        return ResponseEntity.ok(Map.of("threshold", alertThreshold));
    }

    @GetMapping("/expenses")
    public ResponseEntity<?> expenses(@RequestParam(required = false) Integer leadId,
                            @RequestParam(required = false) Integer ticketId
                        ) {
        List<Expense> expenses;
        if (leadId != null) {
            expenses = expenseService.findByLeadId(leadId);
        } else if (ticketId != null) {
            expenses = expenseService.findByTicketId(ticketId);
        } else {
            expenses = expenseService.findAll();
        }

        return ResponseEntity.ok(
            expenses.stream().map((expense) -> {
                Map <String,Object> map = new HashMap<>();
                map.put("id", expense.getExpenseId());
                map.put("description", expense.getDescription());
                map.put("montant", expense.getAmount());
                return map;
            }).collect(Collectors.toList())
        );
    }    
}
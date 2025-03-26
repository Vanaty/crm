package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import site.easy.to.build.crm.entity.BudgetSettings;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.repository.ExpenseRepository;
import site.easy.to.build.crm.repository.LeadRepository;
import site.easy.to.build.crm.repository.TicketRepository;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.type.BudgetStatus;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/employee/customer/expenses")
public class ExpenseController {
    @Autowired
    private BudgetService budgetService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/add/{customerId}")
    public String showAddExpenseForm(@PathVariable Integer customerId,
                                    @RequestParam(required = false) Integer ticketId,
                                    @RequestParam(required = false) Integer leadId,
                                    Model model) {
        Customer customer = budgetService.getCustomerById(customerId);
        List<Ticket> tickets = ticketRepository.findByCustomerCustomerId(customerId);
        List<Lead> leads = leadRepository.findByCustomerCustomerId(customerId);

        model.addAttribute("expense", new Expense());
        model.addAttribute("customer", customer);
        model.addAttribute("tickets", tickets);
        model.addAttribute("leads", leads);
        model.addAttribute("ticketId", ticketId); // Passé dans l’URL
        model.addAttribute("leadId", leadId);
        if (ticketId != null) {
            Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
            model.addAttribute("preselectedTicket", ticket);
        } else if (leadId != null) {
            Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
            model.addAttribute("preselectedLead", lead);
        }
        return "expense/add";
    }

    @PostMapping("/add")
    public String addExpense(@Valid @ModelAttribute("expense") Expense expense,
                             BindingResult result,
                             @RequestParam Integer customerId,
                             @RequestParam(required = false) Integer ticketId,
                             @RequestParam(required = false) Integer leadId,
                             Model model) {
        Customer customer = budgetService.getCustomerById(customerId);
        List<Ticket> tickets = ticketRepository.findByCustomerCustomerId(customerId);
        List<Lead> leads = leadRepository.findByCustomerCustomerId(customerId);

        if (result.hasErrors() || (ticketId == null && leadId == null)) {
            model.addAttribute("customer", customer);
            model.addAttribute("tickets", tickets);
            model.addAttribute("leads", leads);
            if (ticketId == null && leadId == null) {
                model.addAttribute("selectionError", "Veuillez sélectionner un ticket ou un lead.");
            }
            return "expense/add";
        }

        BigDecimal totalBudget = budgetService.getTotalBudget(customerId);
        BigDecimal totalExpenses = budgetService.getTotalExpenses(customerId);
        BigDecimal newTotalExpenses = totalExpenses.add(expense.getAmount());

        if (newTotalExpenses.compareTo(totalBudget) > 0) {
            model.addAttribute("confirmation", "Cette dépense dépasserait le budget total du client. Confirmez-vous?");
            model.addAttribute("expense", expense);
            model.addAttribute("customer", customer);
            model.addAttribute("tickets", tickets);
            model.addAttribute("leads", leads);
            model.addAttribute("ticketId", ticketId);
            model.addAttribute("leadId", leadId);
            return "expense/confirmation";
        } else if (budgetService.checkBudgetStatus(customerId) == BudgetStatus.ALERT) {
            BudgetSettings settings = budgetService.getBudgetSettings();
            BigDecimal threshold = settings != null ? settings.getAlertThreshold() : budgetService.getAlertThreshold();
            model.addAttribute("alert", "Attention : seuil d'alerte de " + threshold + "% atteint pour le budget total!");
        }

        expense.setCustomer(customer);
        if (ticketId != null) {
            Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
            expense.setTicket(ticket);
        } else if (leadId != null) {
            Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
            expense.setLead(lead);
        }

        expenseRepository.save(expense);
        return "redirect:/employee/customer/budgets/" + customerId;
    }

    // Confirmer l’ajout malgré dépassement du budget
    @PostMapping("/add/confirm")
    public String confirmExpenseAdd(@ModelAttribute("expense") Expense expense,
                                   @RequestParam Integer customerId,
                                   @RequestParam(required = false) Integer ticketId,
                                   @RequestParam(required = false) Integer leadId) {
        Customer customer = budgetService.getCustomerById(customerId);
        expense.setCustomer(customer);

        if (ticketId != null) {
            Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
            expense.setTicket(ticket);
        } else if (leadId != null) {
            Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
            expense.setLead(lead);
        }

        expenseRepository.save(expense);
        return "redirect:/employee/customer/budgets/" + customerId;
    }
}

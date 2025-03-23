package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import site.easy.to.build.crm.entity.BudgetSettings;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerBudget;
import site.easy.to.build.crm.repository.BudgetSettingsRepository;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.type.BudgetStatus;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.AuthorizationUtil;

import java.math.BigDecimal;
import java.util.List;




@Controller
@RequestMapping("/employee/customer/budgets")
public class BudgetController {
    private BudgetService budgetService;

    private CustomerRepository customerRepository;

    private BudgetSettingsRepository budgetSettingsRepository;
    
    private final CustomerService customerService;

    private final AuthenticationUtils authenticationUtils;

    
    
    public BudgetController(BudgetService budgetService, CustomerRepository customerRepository,
            BudgetSettingsRepository budgetSettingsRepository, CustomerService customerService,
            AuthenticationUtils authenticationUtils) {
        this.budgetService = budgetService;
        this.customerRepository = customerRepository;
        this.budgetSettingsRepository = budgetSettingsRepository;
        this.customerService = customerService;
        this.authenticationUtils = authenticationUtils;
    }


    @GetMapping
    public String listBudgets(Model model, Authentication authentication){
        List<Customer> customers;
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        if(userId == -1) {
            return "error/not-found";
        }
        if (AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            customers = customerService.findAll();
        } else {
            customers = customerService.findByUserId(userId);
        }
        for (Customer customer : customers) {
            customer.setTotalBudget(budgetService.getTotalBudget(customer.getCustomerId()));
            customer.setTotalExpenses(budgetService.getTotalExpenses(customer.getCustomerId()));
        }
        model.addAttribute("customers", customers);
        return "budgets/list";
    }
    

    @GetMapping("/{customerId}")
    public String viewBudgets(@PathVariable Integer customerId, Model model) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        List<CustomerBudget> budgets = budgetService.getBudgetsByCustomer(customerId);
        BigDecimal totalBudget = budgetService.getTotalBudget(customerId);
        BigDecimal totalExpenses = budgetService.getTotalExpenses(customerId);
        BudgetStatus status = budgetService.checkBudgetStatus(customerId);

        model.addAttribute("budgets", budgets);
        model.addAttribute("customer", customer);
        model.addAttribute("totalBudget", totalBudget);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("alert", budgetService.getAlertThreshold());
        model.addAttribute("status", status);
        model.addAttribute("newBudget", new CustomerBudget());
        return "budgets/budget-list";
    }

    @GetMapping("/create")
    public String showCreateBudgetForm(Model model,Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        List<Customer> customers;
        if (AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            customers = customerService.findAll();
        } else {
            customers = customerService.findByUserId(userId);
        }
        model.addAttribute("budget", new CustomerBudget());
        model.addAttribute("customers", customers);
        return "budgets/create";
    }

    @PostMapping("/create")
    public String createBudget(@Valid @ModelAttribute("budget") CustomerBudget budget, 
                              BindingResult result, 
                              Model model) {
        if (result.hasErrors()) {
            List<Customer> customers = customerRepository.findAll();
            model.addAttribute("customers", customers);
            return "budgets/create";
        }

        budgetService.saveBudget(budget);
        return "redirect:/employee/customer/budgets/" + budget.getCustomer().getCustomerId();
    }
    

    @PostMapping("/add")
    public String addBudget(@ModelAttribute CustomerBudget newBudget,
                           @RequestParam Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        newBudget.setCustomer(customer);
        budgetService.saveBudget(newBudget);
        return "redirect:/employee/customer/budgets/" + customerId;
    }

    @PostMapping("/update-threshold")
    public String updateThreshold(@RequestParam BigDecimal alertThreshold) {
        budgetService.updateAlertThreshold(alertThreshold);
        return "redirect:/employee/customer/budgets/update-threshold";
    }

    @GetMapping("/update-threshold")
    public String showUpdateThresholdForm(Model model) {
        BudgetSettings settings = budgetSettingsRepository.findFirstByOrderByIdAsc();
        model.addAttribute("settings", settings != null ? settings : new BudgetSettings());
        return "budgets/settings";
    }
    
}

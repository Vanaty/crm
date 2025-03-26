package site.easy.to.build.crm.service.imports;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerBudget;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.repository.CustomerBudgetRepository;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.repository.ExpenseRepository;
import site.easy.to.build.crm.repository.LeadRepository;
import site.easy.to.build.crm.repository.TicketRepository;
import site.easy.to.build.crm.util.RandomnUtil;
import site.easy.to.build.crm.exception.ImportException;

@Service
public class ImportServiceImpl implements ImportService {

    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final TicketRepository ticketRepository;
    private final ExpenseRepository expenseRepository;
    private final CustomerBudgetRepository customerBudgetRepository;

    public ImportServiceImpl(CustomerRepository customerRepository,
                             LeadRepository leadRepository,
                             TicketRepository ticketRepository,
                             ExpenseRepository expenseRepository,
                             CustomerBudgetRepository customerBudgetRepository) {
        this.customerRepository = customerRepository;
        this.leadRepository = leadRepository;
        this.ticketRepository = ticketRepository;
        this.expenseRepository = expenseRepository;
        this.customerBudgetRepository = customerBudgetRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> processFile(String filename,User manager, BufferedReader reader) throws IOException {
        List<String> errors = new ArrayList<>();
        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (lineNumber == 1 && line.contains("customer_email")) continue;

            String[] parts = line.split(",", 5);
            if (parts.length < 5) {
                errors.add("Ligne " + lineNumber + ": colonnes insuffisantes");
                continue;
            }

            String email = parts[0].replace(    "\"","").trim();
            if (email.isBlank() || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                throw new RuntimeException("Email invalide : " + email);
            }            
            String subjectOrName = parts[1].trim();
            String type = parts[2].trim().toLowerCase();
            String status = parts[3].trim();
            String expenseRaw = parts[4]
                    .replace("\"", "")
                    .replace(" ", "")
                    .replace(",", ".")
                    .trim();

            try {
                BigDecimal expenseAmount = new BigDecimal(expenseRaw);
                if (expenseAmount.compareTo(BigDecimal.ZERO) < 0) {
                    throw new RuntimeException("Montant négatif non autorisé");
                }

                Customer customer = customerRepository.findByEmail(email);
                if (customer == null) {
                    customer = new Customer();
                    customer.setEmail(email);
                    customer.setUser(manager);
                    customer.setName("Anonymous " + email);
                    customer.setCountry("Unknown");
                    customer = customerRepository.save(customer);
                }

                if (type.equals("lead")) {
                    if (!List.of("meeting-to-schedule","scheduled","archived","success","assign-to-sales").contains(status)) {
                        // throw new RuntimeException("Statut invalide pour lead: " + status);
                        status = "meeting-to-schedule";
                    }

                    Lead lead = new Lead();
                    lead.setCustomer(customer);
                    lead.setStatus(status);
                    lead.setName(subjectOrName);
                    lead.setCreatedAt(LocalDateTime.now());
                    lead.setManager(manager);
                    lead.setEmployee(manager);
                    lead = leadRepository.save(lead);

                    createExpense(customer.getCustomerId(), lead.getLeadId(), null, expenseAmount);

                } else if (type.equals("ticket")) {
                    if (!List.of("open","assigned","on-hold","in-progress","resolved","closed","reopened","pending-customer-response","escalated","archived").contains(status)) {
                        // throw new RuntimeException("Statut invalide pour ticket: " + status);
                        status = "open";
                    }

                    Ticket ticket = new Ticket();
                    ticket.setCustomer(customer);
                    ticket.setStatus(status);
                    ticket.setSubject(subjectOrName);
                    ticket.setManager(manager);
                    ticket.setEmployee(manager);
                    ticket.setPriority(RandomnUtil.randomObject(List.of("low", "medium", "high","closed","urgent","critical")).toString());
                    ticket.setCreatedAt(LocalDateTime.now());
                    ticket = ticketRepository.save(ticket);

                    createExpense(customer.getCustomerId(), null, ticket.getTicketId(), expenseAmount);

                } else {
                    throw new RuntimeException("Type invalide : " + type);
                }

            } catch (Exception e) {
                errors.add("Ligne " + lineNumber + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new ImportException("Import annulé. " + errors.size() + " erreur(s) détectée(s).", errors);
        }        

        return errors;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> processCustomerNameFile(String filename,User manager, BufferedReader reader) throws IOException {
        List<String> errors = new ArrayList<>();
        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (lineNumber == 1 && line.contains("customer_email")) continue;

            String[] parts = line.split(",", -1);
            if (parts.length < 2) {
                errors.add("Ligne " + lineNumber + ": colonnes manquantes");
                continue;
            }

            String email = parts[0].trim();
            String name = parts[1].trim();

            try {
                Customer customer = customerRepository.findByEmail(email);
                if (customer == null) {
                    Customer newCustomer = new Customer();
                    newCustomer.setEmail(email);
                    newCustomer.setUser(manager);
                    newCustomer.setName(name);
                    newCustomer.setCountry("Non spécifié");
                    customerRepository.save(newCustomer);
                } else {
                    if (customer.getName() == null || customer.getName().isBlank() || customer.getName().equals("Client inconnu")) {
                        customer.setName(name);
                        customerRepository.save(customer);
                    }
                }

            } catch (Exception e) {
                errors.add("Ligne " + lineNumber + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new ImportException("Import noms clients annulé. " + errors.size() + " erreur(s).", errors);
        }        
        return errors;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> processCustomerBudgetFile(String filename, BufferedReader reader) throws IOException {
        List<String> errors = new ArrayList<>();
        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (lineNumber == 1 && line.toLowerCase().contains("customer_email")) continue;

            String[] parts = line.split(",", 2);
            if (parts.length < 2) {
                errors.add("Ligne " + lineNumber + " : colonnes manquantes");
                continue;
            }

            String email = parts[0].trim();
            String amountRaw = parts[1]
                    .replace("\"", "")
                    .replace(" ", "")
                    .replace(",", ".")
                    .trim();

            try {
                BigDecimal amount = new BigDecimal(amountRaw);
                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    throw new RuntimeException("Montant budget négatif interdit");
                }

                Customer customer = customerRepository.findByEmail(email);
                if (customer == null) {
                    throw new RuntimeException("Client non trouvé pour l'email : " + email);
                }

                CustomerBudget budget = new CustomerBudget();
                budget.setCustomer(customer);
                budget.setAmount(amount);
                budget.setName("Budget import");
                budget.setCreatedAt(RandomnUtil.getRandomDateTime());

                customerBudgetRepository.save(budget);

            } catch (Exception e) {
                errors.add("Ligne " + lineNumber + " : " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new ImportException("Import budgets annulé. " + errors.size() + " erreur(s).", errors);
        }
        return errors;
    }

    private void createExpense(Integer customerId, Integer leadId, Integer ticketId, BigDecimal amount) {
        Expense expense = new Expense();

        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        expense.setCustomer(customer);

        if (leadId != null) {
            Lead lead = new Lead();
            lead.setLeadId(leadId);
            expense.setLead(lead);
        }

        if (ticketId != null) {
            Ticket ticket = new Ticket();
            ticket.setTicketId(ticketId);
            expense.setTicket(ticket);
        }

        expense.setAmount(amount);
        expense.setCreatedAt(RandomnUtil.getRandomDateTime());
        expenseRepository.save(expense);
    }
}

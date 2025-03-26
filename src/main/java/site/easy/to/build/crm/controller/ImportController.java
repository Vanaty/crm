package site.easy.to.build.crm.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import site.easy.to.build.crm.service.imports.ImportService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.exception.ImportException;

@Controller
@RequestMapping("/imports")
public class ImportController {

    private final ImportService importService;
    
    private final AuthenticationUtils authenticationUtils;

    private final UserService userService;

    public ImportController(ImportService importService,
                            UserService userService,
                            AuthenticationUtils authenticationUtils
                ) {
        this.importService = importService;
        this.authenticationUtils = authenticationUtils;
        this.userService = userService;
    }

    @GetMapping
    public String showImportPage() {
        return "import";
    }

    @PostMapping
    public String handleCsvImport(
            @RequestParam("leadsTicketsFile") MultipartFile leadsTicketsFile,
            @RequestParam(value = "customersFile", required = false) MultipartFile customersFile,
            @RequestParam(value = "budgetsFile", required = false) MultipartFile budgetsFile,
            Authentication authentication,
            Model model) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User manager = userService.findById(userId);
        Map<String, List<String>> errorMap = new HashMap<>();

        // === Fichier Clients ===
        if (customersFile != null && !customersFile.isEmpty()) {
            processFile(customersFile, errorMap,manager, model);
        }

        // === Fichier Budgets ===
        if (budgetsFile != null && !budgetsFile.isEmpty()) {
            processFile(budgetsFile, errorMap,manager, model);
        }

        // === Fichier Leads & Tickets ===
        if (leadsTicketsFile != null && !leadsTicketsFile.isEmpty()) {
            processFile(leadsTicketsFile, errorMap,manager, model);
        }

        if (errorMap.isEmpty() && !model.containsAttribute("globalError")) {
            model.addAttribute("success", "Import réussi !");
        }

        model.addAttribute("errors", errorMap);
        return "import";
    }

    private void processFile(MultipartFile file, Map<String, List<String>> errorMap,User manager, Model model) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            if (reader.markSupported()) {
                reader.mark(1000);
                String header = reader.readLine();
                reader.reset();

                List<String> errors;

                if (header != null && header.toLowerCase().contains("customer_name")) {
                    errors = importService.processCustomerNameFile(file.getOriginalFilename(),manager, reader);
                } else if (header != null && header.toLowerCase().contains("budget")) {
                    errors = importService.processCustomerBudgetFile(file.getOriginalFilename(), reader);
                } else {
                    errors = importService.processFile(file.getOriginalFilename(),manager, reader);
                }

                if (!errors.isEmpty()) {
                    errorMap.put(file.getOriginalFilename(), errors);
                }

            } else {
                errorMap.put(file.getOriginalFilename(), List.of("Le flux ne supporte pas mark/reset"));
            }

        } catch (ImportException e) {
            model.addAttribute("globalError", file.getOriginalFilename() + " : " + e.getMessage());
            errorMap.put(file.getOriginalFilename(), e.getErrors());
        } catch (Exception e) {
            errorMap.put(file.getOriginalFilename(), List.of("Erreur : " + e.getMessage()));
        }
    }
}

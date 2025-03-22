package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import site.easy.to.build.crm.service.data.DeleteAllService;

@Controller
@RequestMapping("/delete-all")
public class DeleteAllController {

    @Autowired
    private DeleteAllService deleteAllService;

    @GetMapping
    public String showDeleteConfirmation(Model model) {
        model.addAttribute("warning", "This action will delete all data from all tables!");
        return "delete-confirmation";
    }

    @PostMapping("/confirm")
    public String deleteAllData(Model model) {
        try {
            deleteAllService.deleteAllTables();
            model.addAttribute("message", "All tables have been successfully deleted");
            return "delete-result";
        } catch (Exception e) {
            model.addAttribute("error", "Error deleting tables: " + e.getMessage());
            return "delete-result";
        }
    }
}
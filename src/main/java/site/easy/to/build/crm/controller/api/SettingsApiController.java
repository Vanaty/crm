package site.easy.to.build.crm.controller.api;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import site.easy.to.build.crm.service.budget.BudgetService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/settings")
public class SettingsApiController {
    @Autowired
    BudgetService budgetService;

    @GetMapping
    public ResponseEntity<?> getSettings() {
        return ResponseEntity.ok(Map.of("budget_alert",budgetService.getAlertThreshold()));
        // Map<String,Integer> rep = .countByPriority();
        // return ResponseEntity.ok(Map.of("priorities", rep));
    }

    @PutMapping("/budget-alert")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String,Double> req) {
        // System.out.println("\n\n\n" + req.get("alert_value"));
        budgetService.updateAlertThreshold(BigDecimal.valueOf(req.get("alert_value")));
        return ResponseEntity.ok(Map.of("budget_alert",budgetService.getAlertThreshold()));
        // Map<String,Integer> rep = .countByPriority();
        // return ResponseEntity.ok(Map.of("priorities", rep));
    }


}

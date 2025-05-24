package site.easy.to.build.crm.controller.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.cassandra.CassandraProperties.Request;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import site.easy.to.build.crm.entity.ApiToken;
import site.easy.to.build.crm.repository.ApiTokenRepository;
import site.easy.to.build.crm.service.imports.ImportService;


@RestController
@RequestMapping("/api/expenses")
public class ExpenseApiController {

    private final ImportService importService;

    private final ApiTokenRepository apiTokenRepository;

    public ExpenseApiController(ImportService importService,ApiTokenRepository apiTokenRepository) {
        this.importService = importService;
        this.apiTokenRepository = apiTokenRepository;
    }
    @PostMapping("/import")
    public ResponseEntity postMethodName(@RequestBody List<Map<String,Object>> data,HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token =  token.substring(7);
            }
            ApiToken apiToken = apiTokenRepository.findByTokenAndRevokedFalse(token).orElse(null);
            if (apiToken == null) {
                return ResponseEntity.ok(Map.of("status","error","message", "Invalid token"));
            }
            List<String> errorMap = new ArrayList<>();
            
            importService.importMap(data,errorMap,apiToken.getUser());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("status","error","message", e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("status","success","message", "Import réussi"));
    }
}

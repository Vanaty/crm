package site.easy.to.build.crm.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import site.easy.to.build.crm.service.data.EntityScannerService;
import site.easy.to.build.crm.service.data.GenericCsvImportService;

@Controller
@RequestMapping("/csv")
public class CsvController {

    @Autowired
    private GenericCsvImportService csvImportService;

    @Autowired
    private EntityScannerService entityScannerService;

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        model.addAttribute("entityClasses", entityScannerService.getAllEntityClassesWithReflections());
        model.addAttribute("message", "");
        model.addAttribute("importedData", new ArrayList<>());
        model.addAttribute("errors", new HashMap<>());
        return "upload";
    }

    @PostMapping("/upload")
    public String handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityClass") String entityClassName,
            Model model) {
        try {
            if (file.isEmpty()) {
                model.addAttribute("message", "Veuillez sélectionner un fichier CSV");
                return prepareModel(model);
            }

            Class<?> entityClass = Class.forName(entityClassName);
            GenericCsvImportService.ImportResult<?> result = csvImportService.importCsv(file, entityClass);
            List<?> importedData = result.getImportedEntities();
            Map<Integer, List<String>> errors = result.getErrors();

            if (errors.isEmpty()) {
                model.addAttribute("message", "Importation réussie : " + importedData.size() + " enregistrements");
            } else {
                model.addAttribute("message", "Importation partielle : " + importedData.size() + 
                    " enregistrements réussis, " + errors.size() + " erreurs");
            }
            model.addAttribute("importedData", importedData);
            model.addAttribute("errors", errors);

        } catch (Exception e) {
            model.addAttribute("message", "Erreur lors de l'importation : " + e.getMessage());
            model.addAttribute("errors", new HashMap<>());
        }
        return prepareModel(model);
    }

    private String prepareModel(Model model) {
        model.addAttribute("entityClasses", entityScannerService.getAllEntityClasses());
        return "upload";
    }
}
package site.easy.to.build.crm.service.data;

import com.opencsv.CSVReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GenericCsvImportService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private Validator validator;

    public <T> ImportResult<T> importCsv(MultipartFile file, Class<T> entityClass) throws Exception {
        List<T> validEntities = new ArrayList<>();
        Map<Integer, List<String>> errors = new HashMap<>();

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVReader csvReader = new CSVReader(reader);
            String[] headers = csvReader.readNext();

            // noms de colonnes
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim().toLowerCase(), i);
            }

            // Eto ra misy nom customisena
            Map<String, Field> fieldMap = new HashMap<>();
            for (Field field : entityClass.getDeclaredFields()) {
                field.setAccessible(true);
                fieldMap.put(field.getName().toLowerCase(), field);
            }

            // Lire les lignes et créer les entités
            String[] line;
            int lineNumber = 1;
            while ((line = csvReader.readNext()) != null) {
                T entity = entityClass.getDeclaredConstructor().newInstance();

                for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
                    String columnName = entry.getKey();
                    Field field = entry.getValue();
                    Integer columnIndex = headerMap.getOrDefault(columnName,null);

                    if (columnIndex != null && columnIndex < line.length) {
                        String value = line[columnIndex].trim();
                        setFieldValue(field, entity, value);
                    }
                }

                Set<ConstraintViolation<T>> violations = validator.validate(entity);
                if (violations.isEmpty()) {
                    validEntities.add(entity);
                } else {
                    List<String> errorMessages = new ArrayList<>();
                    for (ConstraintViolation<T> violation : violations) {
                        errorMessages.add(violation.getPropertyPath() + ": " + violation.getMessage());
                    }
                    errors.put(lineNumber, errorMessages);
                }
                lineNumber++;
            }

            saveAllWithBatch(validEntities);
            return new ImportResult<>(validEntities, errors);
        }
    }

    private void setFieldValue(Field field, Object entity, String value) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        if (value.isEmpty()) return;

        if (fieldType == String.class) {
            field.set(entity, value);
        } else if (fieldType == Integer.class || fieldType == int.class) {
            field.set(entity, Integer.parseInt(value));
        } else if (fieldType == Long.class || fieldType == long.class) {
            field.set(entity, Long.parseLong(value));
        } else if (fieldType == Double.class || fieldType == double.class) {
            field.set(entity, Double.parseDouble(value));
        }
    }

    private <T> void saveAllWithBatch(List<T> entities) {
        int batchSize = 50;
        for (int i = 0; i < entities.size(); i++) {
            entityManager.persist(entities.get(i));
            if (i % batchSize == 0 || i == entities.size() - 1) {
                entityManager.flush();
                entityManager.clear();
            }
        }
    }

    public static class ImportResult<T> {
        private final List<T> importedEntities;
        private final Map<Integer, List<String>> errors;
    
        public ImportResult(List<T> importedEntities, Map<Integer, List<String>> errors) {
            this.importedEntities = importedEntities;
            this.errors = errors;
        }
    
        public List<T> getImportedEntities() { return importedEntities; }
        public Map<Integer, List<String>> getErrors() { return errors; }
    }
}
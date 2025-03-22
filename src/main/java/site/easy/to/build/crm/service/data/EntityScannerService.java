package site.easy.to.build.crm.service.data;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EntityScannerService {

    @Autowired
    private ApplicationContext applicationContext;

    public List<Class<?>> getAllEntityClasses() {
        List<Class<?>> entityClasses = new ArrayList<>();
    
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Entity.class);
        
        for (Object bean : beans.values()) {
            Class<?> entityClass = bean.getClass();
            if (entityClass.getAnnotation(Entity.class) != null) {
                entityClasses.add(entityClass);
            } else {
                Class<?> superclass = entityClass.getSuperclass();
                if (superclass.getAnnotation(Entity.class) != null) {
                    entityClasses.add(superclass);
                }
            }
        }
        
        return entityClasses;
    }

    // Version alternative avec Reflections (si vous ne voulez pas dépendre du contexte Spring)
    public List<Class<?>> getAllEntityClassesWithReflections() {
        Reflections reflections = new Reflections("site.easy.to.build.crm");
        return new ArrayList<>(reflections.getTypesAnnotatedWith(Entity.class));
    }
}

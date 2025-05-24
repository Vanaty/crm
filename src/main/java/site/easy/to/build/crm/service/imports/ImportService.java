package site.easy.to.build.crm.service.imports;

import java.io.*;
import java.util.*;

import site.easy.to.build.crm.entity.User;

public interface ImportService {
    List<String> processFile(String filename,User manager, BufferedReader reader) throws java.io.IOException;
    public List<String> processCustomerNameFile(String filename,User manager, BufferedReader reader) throws IOException, java.io.IOException;
    public List<String> processCustomerBudgetFile(String filename, BufferedReader reader) throws IOException, java.io.IOException;
    void importMap(List<Map<String,Object>> data, List<String> errorMap,User manager);
}

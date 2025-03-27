package site.easy.to.build.crm.service.imports;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.expense.ExpenseService;

@Service
public class ExportService {

    private final ExpenseService expenseService;
    private final static String CSV_SEPARATOR = ",";
    @Autowired
    private BudgetService budgetService;

    ExportService(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }
    private void writeHead(List<String> heads,BufferedWriter bw) throws IOException {
        StringBuffer head = new StringBuffer();
        head.append(heads.get(0));
        for (int i = 1; i < heads.size(); i++) {
            head.append(CSV_SEPARATOR);
            head.append(heads.get(i));   
        }
        bw.write(head.toString());
        bw.newLine();
    }
    private  void writeToCSVCustomer(Customer customer,String fileOut)
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut), "UTF-8"));
            StringBuffer oneLine = new StringBuffer();
            this.writeHead(List.of("customer_name", "customer_email"), bw);
            oneLine.append(customer.getName());
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(customer.getEmail());
            bw.write(oneLine.toString());
            bw.newLine();
            bw.flush();
            bw.close();
        }
        catch (UnsupportedEncodingException e) {}
        catch (FileNotFoundException e){}
        catch (IOException e){}
    }

    public  void writeCustomer(Customer customer,String fileOut) throws IOException {
        List<String> head =  List.of("customer_email","customer_name","subject_or_name","type","status","createdAt","employee","expense");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut), "UTF-8"));
        this.writeHead(head, bw);
        for (Lead lead : customer.getLeads())
        {
            StringBuffer oneLine = new StringBuffer();
            oneLine.append(customer.getEmail());
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(customer.getName());
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(lead.getName().trim().length() == 0? "" : lead.getName());
            oneLine.append(CSV_SEPARATOR);
            oneLine.append("lead");
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(lead.getStatus());
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(lead.getCreatedAt());
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(lead.getEmployee().getId());
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(this.decimalToString(expenseService.getTotalExpensesByLead(lead.getLeadId())));
            bw.write(oneLine.toString());
            bw.newLine();
        }

        for (Ticket lead : customer.getTickets())
        {
            StringBuffer oneLine = new StringBuffer();
            oneLine.append(customer.getEmail());
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(customer.getName());
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(lead.getSubject().trim().length() == 0? "" : lead.getSubject());
            oneLine.append(CSV_SEPARATOR);
            oneLine.append("ticket");
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(lead.getStatus());
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(lead.getCreatedAt());
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(lead.getEmployee().getId());
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(this.decimalToString(expenseService.getTotalExpensesByTicket(lead.getTicketId())));
            bw.write(oneLine.toString());
            bw.newLine();
        }
        bw.flush();
        bw.close();
    }

    public String decimalToString(BigDecimal bd) {
        if (bd == null) {
            return "";
        }
        return bd.compareTo(BigDecimal.ZERO) < 0  ? "" : "\""+bd.toString() +"\"";
    }
}

package site.easy.to.build.crm.dto;

import java.util.Map;

public class BudgetExpenseTimelineDTO {
    private Map<String, BudgetExpenseDTO> timeline;

    public BudgetExpenseTimelineDTO(Map<String, BudgetExpenseDTO> timeline) {
        this.timeline = timeline;
    }

    public Map<String, BudgetExpenseDTO> getTimeline() {
        return timeline;
    }

    public void setTimeline(Map<String, BudgetExpenseDTO> timeline) {
        this.timeline = timeline;
    }
}

package org.javlo.component.links;

public class JsonPageBean {

    private String title;
    private String description;
    private String url;
    private String startDate;
    private String endDate;

    public JsonPageBean(SmartPageBean pageBean) throws Exception {
        title = pageBean.getTitle();
        description = pageBean.getDescription();
        url = pageBean.getLinkOn();
        startDate = pageBean.getTimeRange().getStartDateBean().getSortableDate();
        endDate = pageBean.getTimeRange().getEndDateBean().getSortableDate();
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}

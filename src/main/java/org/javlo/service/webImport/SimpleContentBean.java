package org.javlo.service.webImport;

import org.javlo.component.image.ImageBean;

import java.time.LocalDate;

public class SimpleContentBean {

    private String title;
    private LocalDate date;
    private String content;
    private ImageBean image;

    public SimpleContentBean(String title, LocalDate date, String content, ImageBean image) {
        this.title = title;
        this.date = date;
        this.content = content;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ImageBean getImage() {
        return image;
    }

    public void setImage(ImageBean image) {
        this.image = image;
    }

}

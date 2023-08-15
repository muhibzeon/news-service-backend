package com.magazine.backend.models;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long newsID;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String text;

    private LocalDate creationDate;

    private LocalDate validFrom;

    private LocalDate validTo;

    private boolean published;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @OneToOne(mappedBy = "news", fetch = FetchType.EAGER)
    private Picture pictures;

    public News() {
    }

    public News(String title, String text, LocalDate creationDate, LocalDate validFrom, LocalDate validTo, User author) {
        this.title = title;
        this.text = text;
        this.creationDate = creationDate;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.author = author;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Long getNewsID() {
        return newsID;
    }

    public void setNewsID(Long newsID) {
        this.newsID = newsID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Picture getPictures() {
        return pictures;
    }

    public void setPictures(Picture pictures) {
        this.pictures = pictures;
    }
}
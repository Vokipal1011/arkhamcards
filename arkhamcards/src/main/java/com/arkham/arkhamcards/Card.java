package com.arkham.arkhamcards;

import jakarta.persistence.*;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CardSource source;

    @Enumerated(EnumType.STRING)
    private CardType type;

    private String subtype;

    private String title;

    private String aspect;

    private String imagePath;

    public Card() {
    }

    // Сеттеры
    public void setSource(CardSource source) {
        this.source = source;
    }

    public void setType(CardType type) {
        this.type = type;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAspect(String aspect) {
        this.aspect = aspect;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Геттеры
    public CardSource getSource() {
        return source;
    }

    public CardType getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getTitle() {
        return title;
    }

    public String getAspect() {
        return aspect;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Long getId() {
        return id;
    }
}

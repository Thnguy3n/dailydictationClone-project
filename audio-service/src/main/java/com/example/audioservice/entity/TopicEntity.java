package com.example.audioservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "topics")
public class TopicEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title")
    private String title;
    @Column(name = "levels")
    private String levels;
    @Column(name = "description")
    private String description;
    @Column(name = "image")
    private String image;
    @Column(name = "premium_topic")
    private Integer premiumTopic;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL,mappedBy = "topicEntity")
    List<SectionEntity> sectionEntities = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "categoryId")
    private CategoryEntity categoryEntity;

}

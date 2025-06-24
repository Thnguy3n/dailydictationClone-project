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
@Table(name = "lesson")
public class LessonEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title")
    private String title;
    @Column(name = "audioPath")
    private String audioPath;
    @Column(name = "transcript",length = 5000)
    private String transcript;
    @ManyToOne
    @JoinColumn(name = "section_id")
    private SectionEntity sectionEntity;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL,mappedBy = "lesson")
    List<ChallengeEntity> challengeEntities = new ArrayList<>() ;

}

package com.example.audioservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "challenge")
public class ChallengeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "answer_key")
    private String answerKey;
    @Column(name = "isPass")
    private Integer isPass;
    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private LessonEntity lesson;
}

package com.example.audioservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


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
    @Column(name = "full_sentence")
    private String fullSentence;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "word_data", columnDefinition = "JSON")
    private String wordData;
    @Column(name = "strat_time")
    private Double startTime;
    @Column(name = "end_time")
    private Double endTime;
    @Column(name = "order_index")
    private Integer orderIndex;
    @Column(name = "isPass")
    private Integer isPass;
    @Column(name = "audio_segment")
    private String audioSegmentUrl;
    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private LessonEntity lesson;
}

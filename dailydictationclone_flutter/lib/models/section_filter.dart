class SectionFilter {
  final String? level;
  final String? lessonTitle;
  final String? challengeProgress;

  SectionFilter({
    this.level,
    this.lessonTitle,
    this.challengeProgress,
  });

  Map<String, dynamic> toJson() {
    return {
      'level': level,
      'lessonTitle': lessonTitle,
      'challenge_progress': challengeProgress,
    };
  }

  factory SectionFilter.fromJson(Map<String, dynamic> json) {
    return SectionFilter(
      level: json['level'],
      lessonTitle: json['lessonTitle'],
      challengeProgress: json['challenge_progress'],
    );
  }

  bool get isEmpty {
    return (level == null || level!.isEmpty) &&
        (lessonTitle == null || lessonTitle!.isEmpty) &&
        (challengeProgress == null || challengeProgress!.isEmpty);
  }

  SectionFilter copyWith({
    String? level,
    String? lessonTitle,
    String? challengeProgress,
  }) {
    return SectionFilter(
      level: level ?? this.level,
      lessonTitle: lessonTitle ?? this.lessonTitle,
      challengeProgress: challengeProgress ?? this.challengeProgress,
    );
  }

  @override
  String toString() {
    return 'SectionFilter(level: $level, lessonTitle: $lessonTitle, challengeProgress: $challengeProgress)';
  }
}
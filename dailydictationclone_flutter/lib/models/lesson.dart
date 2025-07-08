class LessonResponse {
  final int id;
  final String title;
  final int countChallenge;

  LessonResponse({
    required this.id,
    required this.title,
    required this.countChallenge,
  });

  factory LessonResponse.fromJson(Map<String, dynamic> json) {
    return LessonResponse(
      id: json['id'] ?? 0,
      title: json['title'] ?? '',
      countChallenge: json['countChallenge'] ?? 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'countChallenge': countChallenge,
    };
  }
}

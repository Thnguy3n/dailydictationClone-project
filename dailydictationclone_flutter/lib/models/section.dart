
class Section {
  final int id;
  final String title;
  final String level;

  Section({
    required this.id,
    required this.title,
    required this.level,
  });

  factory Section.fromJson(Map<String, dynamic> json) {
    return Section(
      id: json['id'],
      title: json['title'],
      level: json['level'],
    );
  }
}
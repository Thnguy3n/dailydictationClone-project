class Topic {
  final int id;
  final String title;
  final String levels;
  final String description;
  final String image;
  final String categoryTitle;
  final int premiumTopic;

  Topic({
    required this.id,
    required this.title,
    required this.levels,
    required this.description,
    required this.image,
    required this.categoryTitle,
    required this.premiumTopic,
  });

  factory Topic.fromJson(Map<String, dynamic> json) {
    return Topic(
      id: json['id'],
      title: json['title'],
      levels: json['levels'],
      description: json['description'],
      image: json['image'],
      categoryTitle: json['categoryTitle'],
      premiumTopic: json['premiumTopic'] ?? 0,
    );
  }
}
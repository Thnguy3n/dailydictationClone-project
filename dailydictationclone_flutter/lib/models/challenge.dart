import 'dart:convert';
import 'dart:ffi';

class Challenge {
  final int orderIndex;
  final String fullSentence;
  final WordData wordData;
  final double startTime;
  final double endTime;
  final String audioSegmentUrl;
  final int isPass;

  Challenge({
    required this.orderIndex,
    required this.fullSentence,
    required this.wordData,
    required this.startTime,
    required this.endTime,
    required this.audioSegmentUrl,
    required this.isPass,
  });

  factory Challenge.fromJson(Map<String, dynamic> json) {
    return Challenge(
      orderIndex: json['orderIndex'] ?? 0,
      fullSentence: json['fullSentence'] ?? '',
      wordData: WordData.fromJson(jsonDecode(json['wordData'] ?? '{}')),
      startTime: json['startTime'] ?? 0,
      endTime: json['endTime'] ?? 0,
      audioSegmentUrl: json['audioSegmentUrl'] ?? '',
      isPass: json['isPass'] ?? 0,
    );
  }

  // Helper method to get duration in seconds
  double get durationInSeconds => (endTime - startTime) / 1000.0;

  // Helper method to check if challenge is passed
  bool get isPassed => isPass == 1;
}

class WordData {
  final List<WordInfo> words;

  WordData({required this.words});

  factory WordData.fromJson(Map<String, dynamic> json) {
    final wordsList = json['words'] as List? ?? [];
    return WordData(
      words: wordsList.map((word) => WordInfo.fromJson(word)).toList(),
    );
  }
}

class WordInfo {
  final int index;
  final List<String> acceptableAnswers;

  WordInfo({
    required this.index,
    required this.acceptableAnswers,
  });

  factory WordInfo.fromJson(Map<String, dynamic> json) {
    final answers = json['acceptableAnswers'] as List? ?? [];
    return WordInfo(
      index: json['index'] ?? 0,
      acceptableAnswers: answers.map((answer) => answer.toString()).toList(),
    );
  }
}

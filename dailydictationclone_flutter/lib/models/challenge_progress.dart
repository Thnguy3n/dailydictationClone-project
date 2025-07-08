import 'package:flutter/material.dart';

class ChallengeProgress {
  final int challengeId;
  final String fullSentence;
  final int orderIndex;
  final int isCompleted;
  final int attempts;
  final DateTime? completedAt;
  final bool currentlyPassed;

  ChallengeProgress({
    required this.challengeId,
    required this.fullSentence,
    required this.orderIndex,
    required this.isCompleted,
    required this.attempts,
    this.completedAt,
    required this.currentlyPassed,
  });

  factory ChallengeProgress.fromJson(Map<String, dynamic> json) {
    return ChallengeProgress(
      challengeId: _parseInt(json['challengeId']),
      fullSentence: json['fullSentence']?.toString() ?? '',
      orderIndex: _parseInt(json['orderIndex']),
      isCompleted: _parseInt(json['isCompleted']),
      attempts: _parseInt(json['attempts']),
      completedAt: _parseDateTime(json['completedAt']),
      currentlyPassed: json['currentlyPassed'] == true,
    );
  }

  // Helper methods để parse data an toàn
  static int _parseInt(dynamic value) {
    if (value == null) return 0;
    if (value is int) return value;
    if (value is String) return int.tryParse(value) ?? 0;
    if (value is double) return value.toInt();
    return 0;
  }

  static DateTime? _parseDateTime(dynamic value) {
    if (value == null) return null;
    if (value is String) {
      try {
        return DateTime.parse(value);
      } catch (e) {
        print('Error parsing DateTime: $value, error: $e');
        return null;
      }
    }
    return null;
  }

  Map<String, dynamic> toJson() {
    return {
      'challengeId': challengeId,
      'fullSentence': fullSentence,
      'orderIndex': orderIndex,
      'isCompleted': isCompleted,
      'attempts': attempts,
      'completedAt': completedAt?.toIso8601String(),
      'currentlyPassed': currentlyPassed,
    };
  }

  // Helper methods để hiển thị
  String get formattedCompletedAt {
    if (completedAt == null) return 'Not completed';
    return '${completedAt!.day}/${completedAt!.month}/${completedAt!.year} ${completedAt!.hour}:${completedAt!.minute.toString().padLeft(2, '0')}';
  }

  // Status helpers
  bool get isComplete => this.isCompleted == 1;
  bool get isFailed => this.isCompleted == -1;
  bool get isNotStarted => this.isCompleted == 0;

  String get statusSentence {
    if (isComplete) return '$fullSentence';
    if (isFailed) return 'Challenge: $orderIndex';
    return 'Challenge: $orderIndex';
  }

  String get statusText {
    if (isComplete) return 'Completed';
    if (isFailed) return 'Incorrect';
    return 'Not Started';
  }

  Color get statusColor {
    if (isComplete) return Colors.green;
    if (isFailed) return Colors.orangeAccent;
    return Colors.grey;
  }

  IconData get statusIcon {
    if (isComplete) return Icons.check_circle;
    if (isFailed) return Icons.cancel;
    return Icons.radio_button_unchecked;
  }
}
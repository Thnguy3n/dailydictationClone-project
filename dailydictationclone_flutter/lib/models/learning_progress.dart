import 'dart:ui';

import 'package:flutter/material.dart';

class LearningProgress {
  final int lessonId;
  final String lessonTitle;
  final int totalChallenges;
  final int attemptedChallenges;
  final int passedChallenges;
  final double progressPercentage;
  final double passPercentage;
  final String status;
  final DateTime? lastAccessedAt;
  final DateTime? completedAt;

  LearningProgress({
    required this.lessonId,
    required this.lessonTitle,
    required this.totalChallenges,
    required this.attemptedChallenges,
    required this.passedChallenges,
    required this.progressPercentage,
    required this.passPercentage,
    required this.status,
    this.lastAccessedAt,
    this.completedAt,
  });

  factory LearningProgress.fromJson(Map<String, dynamic> json) {
    return LearningProgress(
      lessonId: _parseInt(json['lessonId']),
      lessonTitle: json['lessonTitle']?.toString() ?? '',
      totalChallenges: _parseInt(json['totalChallenges']),
      attemptedChallenges: _parseInt(json['attemptedChallenges']),
      passedChallenges: _parseInt(json['passedChallenges']),
      progressPercentage: _parseDouble(json['progressPercentage']),
      passPercentage: _parseDouble(json['passPercentage']),
      status: json['status']?.toString() ?? 'UNKNOWN',
      lastAccessedAt: _parseDateTime(json['lastAccessedAt']),
      completedAt: _parseDateTime(json['completedAt']),
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

  static double _parseDouble(dynamic value) {
    if (value == null) return 0.0;
    if (value is double) return value;
    if (value is int) return value.toDouble();
    if (value is String) return double.tryParse(value) ?? 0.0;
    return 0.0;
  }

  static DateTime? _parseDateTime(dynamic value) {
    if (value == null) return null;
    if (value is String) {
      try {
        // Handle LocalDateTime format từ Java (ISO 8601)
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
      'lessonId': lessonId,
      'lessonTitle': lessonTitle,
      'totalChallenges': totalChallenges,
      'attemptedChallenges': attemptedChallenges,
      'passedChallenges': passedChallenges,
      'progressPercentage': progressPercentage,
      'passPercentage': passPercentage,
      'status': status,
      'lastAccessedAt': lastAccessedAt?.toIso8601String(),
      'completedAt': completedAt?.toIso8601String(),
    };
  }

  // Helper methods để hiển thị
  String get formattedProgressPercentage {
    return '${progressPercentage.toStringAsFixed(1)}%';
  }

  String get formattedPassPercentage {
    return '${passPercentage.toStringAsFixed(1)}%';
  }

  String get challengesSummary {
    return '$passedChallenges/$totalChallenges';
  }

  String get formattedLastAccessed {
    if (lastAccessedAt == null) return 'Chưa truy cập';
    final now = DateTime.now();
    final difference = now.difference(lastAccessedAt!);

    if (difference.inDays > 0) {
      return '${difference.inDays} days ago';
    } else if (difference.inHours > 0) {
      return '${difference.inHours} hours ago';
    } else if (difference.inMinutes > 0) {
      return '${difference.inMinutes} minutes ago';
    } else {
      return 'Vừa xong';
    }
  }

  String get formattedCompletedAt {
    if (completedAt == null) return 'Unfinished';
    return '${completedAt!.day}/${completedAt!.month}/${completedAt!.year}';
  }

  // Status helpers
  bool get isCompleted => status.toLowerCase() == 'completed';
  bool get isInProgress => status.toLowerCase() == 'in_progress';
  bool get isNotStarted => status.toLowerCase() == 'not_started';

  Color get statusColor {
    switch (status.toLowerCase()) {
      case 'completed':
        return Colors.green;
      case 'in_progress':
        return Colors.orange;
      case 'not_started':
        return Colors.grey;
      default:
        return Colors.grey;
    }
  }

  IconData get statusIcon {
    switch (status.toLowerCase()) {
      case 'completed':
        return Icons.check_circle;
      case 'in_progress':
        return Icons.play_circle;
      case 'not_started':
        return Icons.radio_button_unchecked;
      default:
        return Icons.help_outline;
    }
  }
}
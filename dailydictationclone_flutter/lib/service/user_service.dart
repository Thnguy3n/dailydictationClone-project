import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/challenge_progress.dart';
import '../models/learning_progress.dart';
import 'auth_service.dart';

class UserService {
  // Replace with your actual user service URL
  static const String userServiceUrl = 'http://10.0.2.2:8181/api';

  static Future<ProfileResult> getUserProfile() async {
    try {
      final token = await AuthService.getToken();
      if (token == null) {
        return ProfileResult(
          success: false,
          message: 'No authentication token found',
        );
      }

      final response = await http.get(
        Uri.parse('$userServiceUrl/users/profile'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return ProfileResult(
          success: true,
          message: 'Profile loaded successfully',
          profile: ProfileResponse.fromJson(data),
        );
      } else if (response.statusCode == 401) {
        await AuthService.removeToken();
        return ProfileResult(
          success: false,
          message: 'Session expired. Please sign in again.',
          errorCode: 401,
        );
      } else {
        return ProfileResult(
          success: false,
          message: 'Failed to load profile',
          errorCode: response.statusCode,
        );
      }
    } catch (e) {
      return ProfileResult(
        success: false,
        message: 'Network error. Please check your connection.',
        errorCode: 0,
      );
    }
  }

  // Update user profile
  static Future<UserResult> updateProfile({
    required String fullName,
    required String email,
    required String phone,
  }) async {
    try {
      final token = await AuthService.getToken();
      if (token == null) {
        return UserResult(
          success: false,
          message: 'No authentication token found',
        );
      }

      final response = await http.post(
        Uri.parse('$userServiceUrl/users/update-profile'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
        body: jsonEncode({
          'fullName': fullName,
          'email': email,
          'phone': phone,
        }),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return UserResult(
          success: true,
          message: 'Profile updated successfully',
          data: data,
        );
      } else if (response.statusCode == 401) {
        await AuthService.removeToken();
        return UserResult(
          success: false,
          message: 'Session expired. Please sign in again.',
          errorCode: 401,
        );
      } else if (response.statusCode == 400) {
        final errorData = jsonDecode(response.body);
        return UserResult(
          success: false,
          message: errorData['message'] ,
          errorCode: 400,
        );
      } else {
        final errorData = jsonDecode(response.body);
        return UserResult(
          success: false,
          message: errorData['message'] ?? 'Failed to update profile',
          errorCode: response.statusCode,
        );
      }
    } catch (e) {
      return UserResult(
        success: false,
        message: 'Failed to update profile',
        errorCode: 0,
      );
    }
  }

  static Future<UserResult> updatePassword({
    required String oldPassword,
    required String newPassword,
  }) async {
    try {
      final token = await AuthService.getToken();
      if (token == null) {
        return UserResult(
          success: false,
          message: 'No authentication token found',
        );
      }

      final response = await http.post(
        Uri.parse('$userServiceUrl/users/update-password'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
        body: jsonEncode({
          'oldPassword': oldPassword,
          'newPassword': newPassword,
        }),
      );
      final data = jsonDecode(response.body);
      if (response.statusCode == 200) {
        return UserResult(
          success: true,
          message: data['message'],
        );
      } else if (response.statusCode == 401) {
        await AuthService.removeToken();
        return UserResult(
          success: false,
          message: 'Session expired. Please sign in again.',
          errorCode: 401,
        );
      } else if (response.statusCode == 400) {
        return UserResult(
          success: false,
          message: data['message'],
          errorCode: 400,
        );
      } else {
        return UserResult(
          success: false,
          message: 'Failed to update password',
          errorCode: response.statusCode,
        );
      }
    } catch (e) {
      return UserResult(
        success: false,
        message: 'Network error. Please check your connection.',
        errorCode: 0,
      );
    }
  }

  static Future<UserResult> getPasswordStatus() async {
    try {
      final token = await AuthService.getToken();
      if (token == null) {
        return UserResult(
          success: false,
          message: 'No authentication token found',
        );
      }

      final response = await http.get(
        Uri.parse('$userServiceUrl/users/password-status'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return UserResult(
          success: true,
          data: data['hasPassword'],
          message: 'Password status retrieved successfully',
        );
      } else if (response.statusCode == 401) {
        await AuthService.removeToken();
        return UserResult(
          success: false,
          message: 'Session expired. Please sign in again.',
          errorCode: 401,
        );
      } else {
        return UserResult(
          success: false,
          message: 'Failed to get password status',
          errorCode: response.statusCode,
        );
      }
    } catch (e) {
      return UserResult(
        success: false,
        message: 'Network error. Please check your connection.',
        errorCode: 0,
      );
    }
  }


  static Future<LearningProgressResult> getLearningProgressDetails() async {
    try {
      final token = await AuthService.getToken();
      if (token == null) {
        return LearningProgressResult(
          success: false,
          message: 'No authentication token found',
        );
      }

      final url = '$userServiceUrl/user-progress/lessons';

      final response = await http.get(
        Uri.parse(url),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final rawData = jsonDecode(response.body);

        List<dynamic> progressData;
        if (rawData is List) {
          progressData = rawData;
        } else if (rawData is Map<String, dynamic>) {
          progressData = rawData['data'] as List<dynamic>? ?? [];
        } else {
          throw Exception('Unexpected response format');
        }

        final progressList = progressData
            .map((item) => LearningProgress.fromJson(item as Map<String, dynamic>))
            .toList();

        return LearningProgressResult(
          success: true,
          message: 'Learning progress loaded successfully',
          progressList: progressList,
        );
      } else if (response.statusCode == 401) {
        await AuthService.removeToken();
        return LearningProgressResult(
          success: false,
          message: 'Session expired. Please sign in again.',
          errorCode: 401,
        );
      } else if (response.statusCode == 404) {
        return LearningProgressResult(
          success: true,
          message: 'No progress data found',
          progressList: [],
        );
      } else {
        try {
          final errorData = jsonDecode(response.body);
          return LearningProgressResult(
            success: false,
            message: errorData['message'] ?? 'Failed to load learning progress',
            errorCode: response.statusCode,
          );
        } catch (e) {
          return LearningProgressResult(
            success: false,
            message: 'Failed to load learning progress',
            errorCode: response.statusCode,
          );
        }
      }
    } catch (e) {
      print('Error in getLearningProgressDetails: $e');
      return LearningProgressResult(
        success: false,
        message: 'Network error. Please check your connection.',
        errorCode: 0,
      );
    }
  }

  static Future<ChallengeProgressResult> getLessonChallengeDetails(int lessonId) async {
    try {
      final token = await AuthService.getToken();
      if (token == null) {
        return ChallengeProgressResult(
          success: false,
          message: 'No authentication token found',
        );
      }

      final url = '$userServiceUrl/user-progress/lessons/$lessonId/detail';

      final response = await http.get(
        Uri.parse(url),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final rawData = jsonDecode(response.body);

        List<dynamic> challengeData;
        if (rawData is Map<String, dynamic>) {
          challengeData = rawData['challengeDetails'] as List<dynamic>? ?? [];
        } else {
          throw Exception('Unexpected response format');
        }

        final challengeList = challengeData
            .map((item) => ChallengeProgress.fromJson(item as Map<String, dynamic>))
            .toList();

        // Sort by orderIndex
        challengeList.sort((a, b) => a.orderIndex.compareTo(b.orderIndex));

        return ChallengeProgressResult(
          success: true,
          message: 'Challenge details loaded successfully',
          challengeList: challengeList,
        );
      } else if (response.statusCode == 401) {
        await AuthService.removeToken();
        return ChallengeProgressResult(
          success: false,
          message: 'Session expired. Please sign in again.',
          errorCode: 401,
        );
      } else if (response.statusCode == 404) {
        return ChallengeProgressResult(
          success: true,
          message: 'No challenge data found',
          challengeList: [],
        );
      } else {
        try {
          final errorData = jsonDecode(response.body);
          return ChallengeProgressResult(
            success: false,
            message: errorData['message'] ?? 'Failed to load challenge details',
            errorCode: response.statusCode,
          );
        } catch (e) {
          return ChallengeProgressResult(
            success: false,
            message: 'Failed to load challenge details',
            errorCode: response.statusCode,
          );
        }
      }
    } catch (e) {
      print('Error in getLessonChallengeDetails: $e');
      return ChallengeProgressResult(
        success: false,
        message: 'Network error. Please check your connection.',
        errorCode: 0,
      );
    }
  }
}
class ChallengeProgressResult {
  final bool success;
  final String message;
  final List<ChallengeProgress>? challengeList;
  final int? errorCode;

  ChallengeProgressResult({
    required this.success,
    required this.message,
    this.challengeList,
    this.errorCode,
  });
}

class LearningProgressResult {
  final bool success;
  final String message;
  final List<LearningProgress>? progressList;
  final int? errorCode;

  LearningProgressResult({
    required this.success,
    required this.message,
    this.progressList,
    this.errorCode,
  });
}
class ProfileResponse {
  final String fullName;
  final String email;
  final String phone;

  ProfileResponse({
    required this.fullName,
    required this.email,
    required this.phone,
  });

  factory ProfileResponse.fromJson(Map<String, dynamic> json) {
    return ProfileResponse(
      fullName: json['fullName'] ?? '',
      email: json['email'] ?? '',
      phone: json['phone'] ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'fullName': fullName,
      'email': email,
      'phone': phone,
    };
  }

  // Helper method to get display name
  String get displayName {
    return fullName.isNotEmpty ? fullName : 'User';
  }

  // Helper method to check if profile is complete
  bool get isComplete {
    return fullName.isNotEmpty && email.isNotEmpty && phone.isNotEmpty;
  }
}

class ProfileResult {
  final bool success;
  final String message;
  final ProfileResponse? profile;
  final int? errorCode;

  ProfileResult({
    required this.success,
    required this.message,
    this.profile,
    this.errorCode,
  });
}

class UserResult {
  final bool success;
  final String message;
  final int? errorCode;
  final dynamic data; // Thêm field này để lưu data

  UserResult({
    required this.success,
    required this.message,
    this.errorCode,
    this.data,
  });
}
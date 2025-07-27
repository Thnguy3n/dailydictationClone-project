import 'package:dio/dio.dart';

import '../models/challenge.dart';
import 'auth_service.dart';

class ChallengeService {
  final Dio _dio;

  ChallengeService(this._dio);

  Future<Challenge> getCurrentChallenge(int lessonId) async {
    try {
      String? token = await AuthService.getToken();

      Map<String, dynamic> headers = {
        'Content-Type': 'application/json',
      };

      if (token != null && token.trim().isNotEmpty) {
        headers['Authorization'] = 'Bearer $token';
      }

      final response = await _dio.get(
        '/challenge/continue-to-challenge',
        queryParameters: {'lessonId': lessonId},
        options: Options(headers: headers),
      );

      if (response.data != null) {
        return Challenge.fromJson(response.data);
      } else {
        throw Exception('Invalid response format');
      }
    } on DioException catch (e) {
      throw Exception('Failed to load challenge: ${e.response?.data['message'] ?? e.message}');
    } catch (e) {
      throw Exception('Failed to load challenge: $e');
    }
  }

  Future<Challenge> getNextChallenge(int lessonId, int currentOrderIndex) async {
    try {
      final response = await _dio.get(
        '/challenge/next-challenge',
        queryParameters: {
          'lessonId': lessonId,
          'orderIndex': currentOrderIndex,
        },
      );
      if (response.data != null) {
        return Challenge.fromJson(response.data);
      } else {
        throw Exception('No next challenge available');
      }
    } on DioException catch (e) {
      throw Exception('Failed to load next challenge: ${e.response?.data['message'] ?? e.message}');
    } catch (e) {
      throw Exception('Failed to load next challenge: $e');
    }
  }

  Future<Challenge> getPreviousChallenge(int lessonId, int currentOrderIndex) async {
    try {
      final response = await _dio.get(
        '/challenge/previous-challenge',
        queryParameters: {
          'lessonId': lessonId,
          'orderIndex': currentOrderIndex,
        },
      );

      if (response.data != null) {
        return Challenge.fromJson(response.data);
      } else {
        throw Exception('No previous challenge available');
      }
    } on DioException catch (e) {
      throw Exception('Failed to load previous challenge: ${e.response?.data['message'] ?? e.message}');
    } catch (e) {
      throw Exception('Failed to load previous challenge: $e');
    }
  }
  Future<Map<String, dynamic>> checkChallenge(int orderIndex, int lessonId, List<String> userAnswers) async {
    try {
      String? token = await AuthService.getToken();

      Map<String, dynamic> headers = {
        'Content-Type': 'application/json',
      };

      if (token != null && token.isNotEmpty) {
        headers['Authorization'] = 'Bearer $token';
      }

      final checkRequest = {
        'orderIndex': orderIndex,
        'lessonId': lessonId,
        'userAnswers': userAnswers,
      };

      final response = await _dio.post(
        '/challenge/check',
        data: checkRequest,
        options: Options(headers: headers),
      );
      Map<String, dynamic> result = response.data as Map<String, dynamic>;
      result['isCorrect'] = result['allCorrect'] as bool;

      return result;
    } on DioException catch (e) {
      throw Exception('Failed to check challenge: ${e.response?.data['message'] ?? e.message}');
    } catch (e) {
      throw Exception('Failed to check challenge: $e');
    }
  }

  String buildHintSentence(List<dynamic> wordResults) {
    StringBuffer hint = StringBuffer();
    bool foundFirstIncorrect = false;
    bool isFirstWord = true;

    for (var word in wordResults) {
      if (!isFirstWord) {
        hint.write(' ');
      } else {
        isFirstWord = false;
      }

      if (!foundFirstIncorrect && !word['correct']) {
        if (word['acceptableAnswers'].isNotEmpty) {
          hint.write(word['acceptableAnswers'][0]);
        }
        foundFirstIncorrect = true;
      } else if (foundFirstIncorrect) {
        int length = word['acceptableAnswers'].isNotEmpty
            ? word['acceptableAnswers'][0].length
            : 3;
        hint.write('*' * length);
      } else {
        hint.write(word['userAnswer']);
      }
    }
    return hint.toString();
  }
}

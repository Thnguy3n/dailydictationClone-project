import 'package:dio/dio.dart';
import '../models/section.dart';
import '../models/section_filter.dart';
import 'auth_service.dart';

class SectionService {
  final Dio _dio;

  SectionService(this._dio);

  Future<SectionResult> getSections(int topicId) async {
    try {
      String? token = await AuthService.getToken();

      Map<String, dynamic> headers = {
        'Content-Type': 'application/json',
      };

      if (token != null && token.trim().isNotEmpty) {
        headers['Authorization'] = 'Bearer $token';
      }

      final response = await _dio.get(
        '/sections/list',
        queryParameters: {'topicId': topicId},
        options: Options(headers: headers),
      );

      if (response.statusCode == 200) {
        if (response.data is List) {
          final sections = (response.data as List)
              .map((item) => Section.fromJson(item))
              .toList();
          return SectionResult(sections: sections);
        } else {
          throw Exception('Invalid response format from /sections/list: Expected a List.');
        }
      }

      if (response.statusCode == 403) {
        return SectionResult(
          sections: [],
          isPremium: true,
          message: 'This topic is premium, please subscribe to access it.',
        );
      }

      throw Exception('Failed to load sections: ${response.statusCode}');

    } on DioException catch (e) {
      if (e.response?.statusCode == 403) {
        return SectionResult(
          sections: [],
          isPremium: true,
          message: 'This topic is premium, please subscribe to access it.',
        );
      }
      throw Exception('Failed to load sections: ${e.response?.data['message'] ?? e.message}');
    }
  }

  Future<SectionResult> getFilteredSections(int topicId, SectionFilter filter) async {
    try {
      String? token = await AuthService.getToken();

      Map<String, dynamic> headers = {
        'Content-Type': 'application/json',
      };

      if (token != null && token.trim().isNotEmpty) {
        headers['Authorization'] = 'Bearer $token';
      }

      Map<String, dynamic> requestBody = filter.toJson();

      final response = await _dio.post(
        '/sections/filter',
        data: requestBody,
        queryParameters: {'topicId': topicId},
        options: Options(headers: headers),
      );

      if (response.statusCode == 200) {
        if (response.data is List) {
          final sections = (response.data as List)
              .map((item) => Section.fromJson(item))
              .toList();
          return SectionResult(sections: sections);
        } else {
          // You might want to log this error internally for debugging
          throw Exception('Invalid response format from /sections/filter: Expected a List.');
        }
      }

      if (response.statusCode == 403) {
        return SectionResult(
          sections: [],
          isPremium: true,
          message: 'This topic is premium, please subscribe to access it.',
        );
      }

      throw Exception('Failed to filter sections: ${response.statusCode}');

    } on DioException catch (e) {
      if (e.response?.statusCode == 403) {
        return SectionResult(
          sections: [],
          isPremium: true,
          message: 'This topic is premium, please subscribe to access it.',
        );
      }
      throw Exception('Failed to filter sections: ${e.response?.data['message'] ?? e.message}');
    }
  }
}

class SectionResult {
  final List<Section> sections;
  final bool isPremium;
  final String? message;

  SectionResult({
    required this.sections,
    this.isPremium = false,
    this.message,
  });
}
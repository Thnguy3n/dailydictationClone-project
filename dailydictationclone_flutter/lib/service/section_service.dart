import 'package:dio/dio.dart';
import '../models/section.dart';
import 'auth_service.dart';

class SectionService {
  final Dio _dio;

  SectionService(this._dio); // Dependency injection

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
        final sections = (response.data as List)
            .map((item) => Section.fromJson(item))
            .toList();
        return SectionResult(sections: sections);
      }

      if (response.statusCode == 403) {
        // Return premium result instead of throwing exception
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
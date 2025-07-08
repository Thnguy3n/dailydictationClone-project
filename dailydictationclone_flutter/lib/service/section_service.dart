// lib/services/section_service.dart
import 'package:dio/dio.dart';
import '../models/section.dart';

class SectionService {
  final Dio _dio;

  SectionService(this._dio); // Dependency injection

  Future<List<Section>> getSections(int topicId) async {
    try {
      final response = await _dio.get(
        '/sections/list',
        queryParameters: {'topicId': topicId},
      );

      return (response.data as List)
          .map((item) => Section.fromJson(item))
          .toList();
    } on DioException catch (e) {
      throw Exception('Failed to load sections: ${e.response?.data['message'] ?? e.message}');
    }
  }
}
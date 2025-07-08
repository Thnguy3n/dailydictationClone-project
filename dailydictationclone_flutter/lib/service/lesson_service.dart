import 'package:dio/dio.dart';
import '../models/lesson.dart';

class LessonService {
  final Dio _dio;

  LessonService(this._dio);

  Future<List<LessonResponse>> getLessons(int sectionId) async {
    try {
      final response = await _dio.get(
        '/lessons/list',
        queryParameters: {'sectionId': sectionId},
      );

      if (response.data is List) {
        return (response.data as List)
            .map((item) => LessonResponse.fromJson(item))
            .toList();
      } if(response.statusCode==404){
        throw Exception('No lessons found in this section');
      } else {
        throw Exception('Unexpected response format');
      }
    } on DioException catch (e) {
      throw Exception('No lessons found in this section');
    } catch (e) {
      throw Exception('No lessons found in this section');
    }
  }
}

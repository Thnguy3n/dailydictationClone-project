import 'package:dio/dio.dart';
import '../models/topic.dart';

class TopicService {
  final Dio _dio = Dio(BaseOptions(
    baseUrl: 'http://10.0.2.2:8181/api',
    connectTimeout: const Duration(seconds: 5),
  ));

  Future<List<Topic>> getTopics() async {
    try {
      final response = await _dio.get('/topics/list');
      return (response.data as List)
          .map((item) => Topic.fromJson(item))
          .toList();
    } on DioException catch (e) {
      throw Exception('Failed to load topics: ${e.message}');
    }
  }
}
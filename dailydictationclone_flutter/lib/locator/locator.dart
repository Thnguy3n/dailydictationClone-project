// lib/core/locator.dart
import 'package:get_it/get_it.dart';
import 'package:dio/dio.dart';
import '../service/auth_service.dart';
import '../service/challenge_service.dart';
import '../service/lesson_service.dart';
import '../service/topic_service.dart';
import '../service/section_service.dart';

final getIt = GetIt.instance;

void setupLocator() {
  getIt.registerSingleton<Dio>(Dio(BaseOptions(
    baseUrl: 'http://10.0.2.2:8181/api',
    connectTimeout: const Duration(seconds: 10),
  )));

  // Đăng ký các service
  getIt.registerSingleton(TopicService());
  getIt.registerSingleton(SectionService(getIt<Dio>()));
  getIt.registerSingleton(AuthService);
  getIt.registerLazySingleton<LessonService>(() => LessonService(getIt<Dio>()));
  getIt.registerLazySingleton<ChallengeService>(() => ChallengeService(getIt<Dio>()));

}
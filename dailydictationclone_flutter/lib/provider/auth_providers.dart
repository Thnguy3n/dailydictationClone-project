import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../service/auth_service.dart';
final authStatusProvider = FutureProvider<bool>((ref) async {
  return await AuthService.isAuthenticated();
});
import 'dart:async';
import 'dart:convert';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class AuthService {
  static const String baseUrl = 'http://10.0.2.2:8181/api';
  static const String gatewayUrl = 'http://10.0.2.2:8181'; // API Gateway URL

  static final GoogleSignIn _googleSignIn = GoogleSignIn(
    scopes: ['email', 'profile'],
  );

  // Token management
  static Future<void> saveToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('jwt_token', token);
  }

  static Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('jwt_token');
  }

  static Future<void> removeToken() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('jwt_token');
  }

  static Future<bool> isAuthenticated() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('jwt_token');
    print('AuthService.isAuthenticated() - Token: $token');
    final bool result = token != null && token.isNotEmpty;
    print('AuthService.isAuthenticated() - Is authenticated: $result');
    return result;
  }

  static Future<void> clearStoredData() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
  }

  // Sign In API call
  static Future<AuthResult> signIn({
    required String username,
    required String password,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/auth/login'),
        headers: {
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'username': username,
          'password': password,
        }),
      );
      final Map<String, dynamic> data = jsonDecode(response.body);
      if (response.statusCode == 200) {
        if (data['token'] != null) {
          await saveToken(data['token']);
        }
        return AuthResult(
          success: true,
          message: data['message'],
          data: {'token': data['token']},
        );
      } else if (response.statusCode == 401) {
        return AuthResult(
          success: false,
          message: data['message'],
          errorCode: 401,
        );
      }
      return AuthResult(
        success: false,
        message: 'An error occurred. Please try again later.',
        errorCode: response.statusCode,
      );
    } catch (e) {
      return AuthResult(
        success: false,
        message: 'Network error. Please check your connection.',
        errorCode: 0,
      );
    }
  }

  // Send OTP API call
  static Future<OtpResult> sendOtp({
    required String email,
    String otpType = 'EMAIL_VERIFICATION',
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/auth/send-otp'),
        headers: {
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'email': email,
          'otpType': otpType,
        }),
      );

      final Map<String, dynamic> data = jsonDecode(response.body);

      if (response.statusCode == 200) {
        return OtpResult(
          success: true,
          message: data['message'] ?? 'OTP sent successfully',
          expiresAt: data['expiresAt'],
        );
      } else {
        return OtpResult(
          success: false,
          message: data['message'] ?? 'Failed to send OTP',
          errorCode: response.statusCode,
        );
      }
    } catch (e) {
      return OtpResult(
        success: false,
        message: 'Network error. Please check your connection.',
        errorCode: 0,
      );
    }
  }

  // Verify OTP API call
  static Future<OtpResult> verifyOtp({
    required String email,
    required String otpCode,
    String otpType = 'EMAIL_VERIFICATION',
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/auth/verify-otp'),
        headers: {
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'email': email,
          'otpCode': otpCode,
          'otpType': otpType,
        }),
      );

      final Map<String, dynamic> data = jsonDecode(response.body);

      if (response.statusCode == 200) {
        return OtpResult(
          success: true,
          message: data['message'] ?? 'OTP verified successfully',
        );
      } else {
        return OtpResult(
          success: false,
          message: data['message'] ?? 'Invalid OTP code',
          errorCode: response.statusCode,
        );
      }
    } catch (e) {
      return OtpResult(
        success: false,
        message: 'Network error. Please check your connection.',
        errorCode: 0,
      );
    }
  }

  // Sign Up API call with OTP
  static Future<AuthResult> signUp({
    required String username,
    required String password,
    required String fullName,
    required String phone,
    required String email,
    required String otpCode,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/auth/register'),
        headers: {
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'username': username,
          'password': password,
          'fullName': fullName,
          'phone': phone,
          'email': email,
          'otpCode': otpCode,
        }),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        final data = jsonDecode(response.body);
        return AuthResult(
          success: true,
          message: '${data['fullName']} registered successfully',
          data: data,
        );
      } else if (response.statusCode == 400) {
        final errorData = jsonDecode(response.body);
        return AuthResult(
          success: false,
          message: errorData['message'],
          errorCode: 400,
        );
      } else {
        return AuthResult(
          success: false,
          message: 'An error occurred. Please try again later.',
          errorCode: response.statusCode,
        );
      }
    } catch (e) {
      return AuthResult(
        success: false,
        message: 'Network error. Please check your connection.',
        errorCode: 0,
      );
    }
  }

  static Future<void> signOut() async {
    await removeToken();
  }

  static Future<AuthResult> signInWithGoogle() async {
    try {
      final GoogleSignInAccount? currentUser = _googleSignIn.currentUser;

      if (currentUser != null) {
        await _googleSignIn.signOut();
      }

      final GoogleSignInAccount? googleUser = await _googleSignIn.signIn();

      if (googleUser == null) {
        return AuthResult(
          success: false,
          message: 'Google Sign In was canceled',
          errorCode: 0,
        );
      }

      final String email = googleUser.email;
      final String displayName = googleUser.displayName ?? '';

      final authResult = await _authenticateWithGoogleUserInfo(email, displayName);

      if (authResult.success) {
        if (authResult.data != null && authResult.data!['token'] != null) {
          await saveToken(authResult.data!['token']);
        }
        return authResult;
      } else {
        return authResult;
      }

    } catch (e, stackTrace) {
      return AuthResult(
        success: false,
        message: 'Google Sign In failed. Please try again. Error: $e',
        errorCode: 0,
      );
    }
  }

  static Future<AuthResult> _authenticateWithGoogleUserInfo(String email, String displayName) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/auth/oauth2-login'),
        headers: {
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'email': email,
          'displayName': displayName,
        }),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        final data = jsonDecode(response.body);
        if (data['token'] != null) {
          await saveToken(data['token']);
        }
        return AuthResult(
          success: true,
          message: 'Google sign in successful',
          data: data,
        );
      } else {
        final errorData = jsonDecode(response.body);
        return AuthResult(
          success: false,
          message: errorData['message'] ?? 'Authentication failed',
          errorCode: response.statusCode,
        );
      }
    } catch (e) {
      return AuthResult(
        success: false,
        message: 'Network error. Please check your connection.',
        errorCode: 0,
      );
    }
  }
}

class AuthResult {
  final bool success;
  final String message;
  final Map<String, dynamic>? data;
  final int? errorCode;

  AuthResult({
    required this.success,
    required this.message,
    this.data,
    this.errorCode,
  });
}

class OtpResult {
  final bool success;
  final String message;
  final String? expiresAt;
  final int? errorCode;

  OtpResult({
    required this.success,
    required this.message,
    this.expiresAt,
    this.errorCode,
  });
}
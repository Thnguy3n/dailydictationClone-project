import 'package:dailydictationclone_flutter/screen/onboarding_screen.dart';
import 'package:dailydictationclone_flutter/screen/profile_screen.dart';
import 'package:dailydictationclone_flutter/screen/sign_up_screen.dart';
import 'package:dailydictationclone_flutter/screen/topic_list.dart';
import 'package:dailydictationclone_flutter/screen/upgrade_premium_screen.dart';
import 'package:dailydictationclone_flutter/service/auth_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'locator/locator.dart';
import 'package:dailydictationclone_flutter/screen/sign_in_screen.dart';
void main() async {
  setupLocator();
  WidgetsFlutterBinding.ensureInitialized();
  await AuthService.clearStoredData();
  runApp(
    const ProviderScope(
      child: MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: const OnboardingScreen(),
      debugShowCheckedModeBanner: false,
      routes: {
        '/topics': (context) => const TopicListPage(),
        '/signIn': (context) => const SignInScreen(),
        '/signUp': (context) => const SignUpScreen(),
        '/profile': (context) => const ProfileScreen(),
        '/upgradePremium': (context) => const UpgradePremiumScreen(),
      },
    );
  }
}
class AuthWrapper extends StatefulWidget {
  const AuthWrapper({super.key});

  @override
  State<AuthWrapper> createState() => _AuthWrapperState();
}

class _AuthWrapperState extends State<AuthWrapper> {
  bool _isLoading = true;
  bool _isAuthenticated = false;

  @override
  void initState() {
    super.initState();
    _checkAuthStatus();
  }

  Future<void> _checkAuthStatus() async {
    final isAuthenticated = await AuthService.isAuthenticated();

    setState(() {
      _isAuthenticated = isAuthenticated;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(
        backgroundColor: Color(0xFF2C3E50),
        body: Center(
          child: CircularProgressIndicator(
            valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF7FB3D3)),
          ),
        ),
      );
    }

    return _isAuthenticated ? const ProfileScreen() : const SignInScreen();
  }
}

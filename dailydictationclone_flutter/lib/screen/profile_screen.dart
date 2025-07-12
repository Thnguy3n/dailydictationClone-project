import 'package:dailydictationclone_flutter/screen/update_password_screen.dart';
import 'package:flutter/material.dart';
import '../nav/app_bottom_navigation.dart';
import '../service/auth_service.dart';
import '../service/user_service.dart';
import 'edit_profile_screen.dart';
import 'learning_progress_screen.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  ProfileResponse? _userProfile;
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadUserProfile();
  }

  Future<void> _loadUserProfile() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    final result = await UserService.getUserProfile();

    setState(() {
      _isLoading = false;
    });

    if (result.success && result.profile != null) {
      setState(() {
        _userProfile = result.profile;
      });
    } else {
      setState(() {
        _errorMessage = result.message;
      });

      if (result.errorCode == 401) {
        // Token expired, redirect to sign in
        if (mounted) {
          Navigator.pushReplacementNamed(context, '/signIn');
        }
      }
    }
  }

  Future<void> _signOut() async {
    await AuthService.signOut();
    if (mounted) {
      Navigator.pushReplacementNamed(context, '/signIn');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF2C3E50),
      body: SafeArea(
        child: _isLoading
            ? const Center(
          child: CircularProgressIndicator(
            valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF7FB3D3)),
          ),
        )
            : _errorMessage != null
            ? Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                _errorMessage!,
                style: const TextStyle(
                  color: Colors.red,
                  fontSize: 16,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: _loadUserProfile,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF7FB3D3),
                  foregroundColor: const Color(0xFF2C3E50),
                ),
                child: const Text('Retry'),
              ),
            ],
          ),
        )
            : SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // User Info Header
              Text(
                _userProfile?.displayName ?? 'User',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 32,
                  fontWeight: FontWeight.bold,
                ),
              ),

              const SizedBox(height: 8),

              Text(
                _userProfile?.email ?? '',
                style: const TextStyle(
                  color: Color(0xFF7FB3D3),
                  fontSize: 16,
                ),
              ),

              const SizedBox(height: 16),

              // Show phone if available
              if (_userProfile?.phone.isNotEmpty == true)
                Text(
                  'Phone: ${_userProfile!.phone}',
                  style: TextStyle(
                    color: Colors.white.withOpacity(0.7),
                    fontSize: 16,
                  ),
                ),

              const SizedBox(height: 40),

              // Your Account Section
              Text(
                'Your Account',
                style: TextStyle(
                  color: Colors.white.withOpacity(0.7),
                  fontSize: 18,
                  fontWeight: FontWeight.w500,
                ),
              ),

              const SizedBox(height: 20),

              // Menu Items
              _buildMenuItem(
                icon: Icons.workspace_premium,
                title: 'Upgrade PRO - Remove Ads',
                subtitle: '🔥',
                onTap: () async {
                  Navigator.pushNamed(context, '/upgradePremium');
                },
              ),

              const SizedBox(height: 16),

              _buildMenuItem(
                icon: Icons.person_outline,
                title: 'Edit Profile',
                onTap: () async {
                  final result = await Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => EditProfileScreen(
                        userProfile: _userProfile!,
                      ),
                    ),
                  );

                  if (result == true) {
                    _loadUserProfile(); // Refresh profile data
                  }
                },
              ),

              const SizedBox(height: 16),

              _buildMenuItem(
                icon: Icons.lock_outline,
                title: 'Update password',
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const UpdatePasswordScreen(),
                    ),
                  );
                },
              ),

              const SizedBox(height: 16),

              _buildMenuItem(
                icon: Icons.history,
                title: 'Learning Progress',
                onTap: () async {
                  await Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const LearningProgressScreen(),
                    ),
                  );
                },
              ),

              const SizedBox(height: 16),

              _buildMenuItem(
                icon: Icons.note_outlined,
                title: 'Notes',
                onTap: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('Notes feature coming soon!'),
                      behavior: SnackBarBehavior.floating,
                    ),
                  );
                },
              ),

              const SizedBox(height: 40),

              // Log Out Button
              Center(
                child: SizedBox(
                  width: 200,
                  height: 50,
                  child: ElevatedButton(
                    onPressed: _signOut,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF7FB3D3),
                      foregroundColor: const Color(0xFF2C3E50),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(25),
                      ),
                    ),
                    child: const Text(
                      'Log Out',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ),
              ),

              const SizedBox(height: 10),

            ],
          ),
        ),
      ),
      bottomNavigationBar: AppBottomNavigation(
        currentIndex: 2,
        context: context,
      ),
    );
  }

  Widget _buildMenuItem({
    required IconData icon,
    required String title,
    String? subtitle,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: const Color(0xFF34495E),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: Colors.white.withOpacity(0.1),
            width: 1,
          ),
        ),
        child: Row(
          children: [
            Icon(
              icon,
              color: const Color(0xFF7FB3D3),
              size: 24,
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Row(
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  if (subtitle != null) ...[
                    const SizedBox(width: 8),
                    Text(
                      subtitle,
                      style: const TextStyle(fontSize: 16),
                    ),
                  ],
                ],
              ),
            ),
            Icon(
              Icons.arrow_forward_ios,
              color: Colors.white.withOpacity(0.5),
              size: 16,
            ),
          ],
        ),
      ),
    );
  }
}

// lib/nav/app_bottom_navigation.dart
import 'package:flutter/material.dart';

import '../service/auth_service.dart';

class AppBottomNavigation extends StatefulWidget {
  final int currentIndex;
  final BuildContext context;

  const AppBottomNavigation({
    super.key,
    required this.currentIndex,
    required this.context,
  });

  @override
  State<AppBottomNavigation> createState() => _AppBottomNavigationState();
}

class _AppBottomNavigationState extends State<AppBottomNavigation> {
  bool _isNavigating = false;

  Future<void> _handleNavigation(int index) async {
    // Prevent multiple rapid taps
    if (_isNavigating) return;

    setState(() {
      _isNavigating = true;
    });

    try {
      switch (index) {
        case 0: // Home
          Navigator.pushReplacementNamed(widget.context, '/');
          break;

        case 1: // Practice
          Navigator.pushNamed(widget.context, '/topics');
          break;

        case 2: // Profile
          await _handleProfileNavigation();
          break;

        case 3: // More
          break;
      }
    } finally {
      if (mounted) {
        setState(() {
          _isNavigating = false;
        });
      }
    }
  }


  Future<void> _handleProfileNavigation() async {
    final isAuthenticated = await AuthService.isAuthenticated();

    if (isAuthenticated) {
      Navigator.pushNamed(widget.context, '/profile');
    } else {
      Navigator.pushNamed(widget.context, '/signIn');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(10),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const SizedBox(height: 10),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              _buildNavIcon(
                icon: Icons.home,
                index: 0,
                label: 'Home',
              ),
              _buildNavIcon(
                icon: Icons.headphones,
                index: 1,
                label: 'Practice',
              ),
              _buildNavIcon(
                icon: Icons.person,
                index: 2,
                label: 'Profile',
              ),
              _buildNavIcon(
                icon: Icons.more_horiz,
                index: 3,
                label: 'More',
              ),
            ],
          ),
          const SizedBox(height: 15),
        ],
      ),
    );
  }

  Widget _buildNavIcon({
    required IconData icon,
    required int index,
    required String label,
  }) {
    final isSelected = widget.currentIndex == index;
    final color = isSelected
        ? const Color(0xFF3498DB)
        : Colors.white.withOpacity(0.6);

    return GestureDetector(
      onTap: () => _handleNavigation(index),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, color: color, size: 28),
          const SizedBox(height: 4),
          Text(
            label,
            style: TextStyle(
              color: color,
              fontSize: 12,
            ),
          ),
        ],
      ),
    );
  }
}
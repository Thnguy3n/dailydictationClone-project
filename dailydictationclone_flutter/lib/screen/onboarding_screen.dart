import 'package:flutter/material.dart';
import 'dart:math' as math;

import '../nav/app_bottom_navigation.dart';

class OnboardingScreen extends StatefulWidget {
  const OnboardingScreen({super.key});

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen>
    with TickerProviderStateMixin {
  late PageController _pageController;

  // Multiple animation controllers for different effects
  late AnimationController _mainAnimationController;
  late AnimationController _backgroundController;
  late AnimationController _floatingController;
  late AnimationController _pulseController;

  // Main content animations
  late Animation<double> _fadeAnimation;
  late Animation<Offset> _slideAnimation;
  late Animation<double> _scaleAnimation;

  // Background animations
  late Animation<double> _backgroundRotation;
  late Animation<double> _backgroundScale;

  // Floating elements animations
  late Animation<double> _floatingAnimation;
  late Animation<double> _pulseAnimation;

  // Staggered animations for individual elements
  late Animation<double> _titleFadeAnimation;
  late Animation<Offset> _titleSlideAnimation;
  late Animation<double> _descriptionFadeAnimation;
  late Animation<Offset> _descriptionSlideAnimation;
  late Animation<double> _illustrationScaleAnimation;
  late Animation<double> _illustrationRotateAnimation;
  late Animation<double> _buttonFadeAnimation;
  late Animation<Offset> _buttonSlideAnimation;

  int _currentNavIndex = 0;
  int _currentPage = 0;
  final int _totalPages = 5;

  @override
  void initState() {
    super.initState();
    _pageController = PageController();
    _initializeAnimations();
    _startAnimations();
  }

  void _initializeAnimations() {
    // Main animation controller - 1.2 seconds for smoother transitions
    _mainAnimationController = AnimationController(
      duration: const Duration(milliseconds: 1200),
      vsync: this,
    );

    // Background animation controller - continuous slow rotation
    _backgroundController = AnimationController(
      duration: const Duration(seconds: 20),
      vsync: this,
    );

    // Floating elements controller - continuous up/down motion
    _floatingController = AnimationController(
      duration: const Duration(seconds: 3),
      vsync: this,
    );

    // Pulse controller for attention-grabbing elements
    _pulseController = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    );

    // Main content animations with different curves and timings
    _fadeAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _mainAnimationController,
      curve: const Interval(0.0, 0.6, curve: Curves.easeOutCubic),
    ));

    _slideAnimation = Tween<Offset>(
      begin: const Offset(0, 0.5),
      end: Offset.zero,
    ).animate(CurvedAnimation(
      parent: _mainAnimationController,
      curve: const Interval(0.1, 0.7, curve: Curves.elasticOut),
    ));

    _scaleAnimation = Tween<double>(
      begin: 0.8,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _mainAnimationController,
      curve: const Interval(0.0, 0.8, curve: Curves.elasticOut),
    ));

    // Background animations
    _backgroundRotation = Tween<double>(
      begin: 0,
      end: 2 * math.pi,
    ).animate(CurvedAnimation(
      parent: _backgroundController,
      curve: Curves.linear,
    ));

    _backgroundScale = Tween<double>(
      begin: 1.0,
      end: 1.1,
    ).animate(CurvedAnimation(
      parent: _backgroundController,
      curve: Curves.easeInOut,
    ));

    // Floating animations
    _floatingAnimation = Tween<double>(
      begin: -10,
      end: 10,
    ).animate(CurvedAnimation(
      parent: _floatingController,
      curve: Curves.easeInOut,
    ));

    _pulseAnimation = Tween<double>(
      begin: 1.0,
      end: 1.1,
    ).animate(CurvedAnimation(
      parent: _pulseController,
      curve: Curves.easeInOut,
    ));

    // Staggered element animations
    _titleFadeAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _mainAnimationController,
      curve: const Interval(0.2, 0.6, curve: Curves.easeOut),
    ));

    _titleSlideAnimation = Tween<Offset>(
      begin: const Offset(-0.3, 0),
      end: Offset.zero,
    ).animate(CurvedAnimation(
      parent: _mainAnimationController,
      curve: const Interval(0.2, 0.7, curve: Curves.elasticOut),
    ));

    _descriptionFadeAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _mainAnimationController,
      curve: const Interval(0.4, 0.8, curve: Curves.easeOut),
    ));

    _descriptionSlideAnimation = Tween<Offset>(
      begin: const Offset(0.3, 0),
      end: Offset.zero,
    ).animate(CurvedAnimation(
      parent: _mainAnimationController,
      curve: const Interval(0.4, 0.9, curve: Curves.elasticOut),
    ));

    _illustrationScaleAnimation = Tween<double>(
      begin: 0.5,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _mainAnimationController,
      curve: const Interval(0.0, 0.8, curve: Curves.bounceOut),
    ));

    _illustrationRotateAnimation = Tween<double>(
      begin: -0.1,
      end: 0.0,
    ).animate(CurvedAnimation(
      parent: _mainAnimationController,
      curve: const Interval(0.3, 1.0, curve: Curves.elasticOut),
    ));

    _buttonFadeAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _mainAnimationController,
      curve: const Interval(0.6, 1.0, curve: Curves.easeOut),
    ));

    _buttonSlideAnimation = Tween<Offset>(
      begin: const Offset(0, 0.5),
      end: Offset.zero,
    ).animate(CurvedAnimation(
      parent: _mainAnimationController,
      curve: const Interval(0.6, 1.0, curve: Curves.bounceOut),
    ));
  }

  void _startAnimations() {
    _mainAnimationController.forward();
    _backgroundController.repeat();
    _floatingController.repeat(reverse: true);
    _pulseController.repeat(reverse: true);
  }

  @override
  void dispose() {
    _pageController.dispose();
    _mainAnimationController.dispose();
    _backgroundController.dispose();
    _floatingController.dispose();
    _pulseController.dispose();
    super.dispose();
  }

  void _onPageChanged(int page) {
    setState(() {
      _currentPage = page;
    });

    // Smooth restart animation with slight delay for better effect
    _mainAnimationController.reset();
    Future.delayed(const Duration(milliseconds: 100), () {
      if (mounted) {
        _mainAnimationController.forward();
      }
    });
  }

  void _nextPage() {
    if (_currentPage < _totalPages - 1) {
      _pageController.nextPage(
        duration: const Duration(milliseconds: 500),
        curve: Curves.easeInOutCubic,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF2C3E50),
      body: SafeArea(
        child: Stack(
          children: [
            // Animated background elements
            _buildAnimatedBackground(),

            Column(
              children: [
                Expanded(
                  child: PageView(
                    controller: _pageController,
                    onPageChanged: _onPageChanged,
                    children: [
                      _buildPage0(),
                      _buildPage1(),
                      _buildPage2(),
                      _buildPage3(),
                      _buildPage4(),
                    ],
                  ),
                ),
                _buildPageIndicator(),
                AppBottomNavigation(
                  currentIndex: _currentNavIndex,
                  context: context,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAnimatedBackground() {
    return AnimatedBuilder(
      animation: _backgroundController,
      builder: (context, child) {
        return Positioned.fill(
          child: Transform.scale(
            scale: _backgroundScale.value,
            child: Transform.rotate(
              angle: _backgroundRotation.value,
              child: Container(
                decoration: BoxDecoration(
                  gradient: RadialGradient(
                    center: Alignment.topRight,
                    radius: 1.5,
                    colors: [
                      const Color(0xFF2C3E50).withOpacity(0.1),
                      const Color(0xFF34495E).withOpacity(0.05),
                      Colors.transparent,
                    ],
                  ),
                ),
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _buildPageIndicator() {
    return AnimatedBuilder(
      animation: _fadeAnimation,
      builder: (context, child) {
        return FadeTransition(
          opacity: _fadeAnimation,
          child: Container(
            padding: const EdgeInsets.only(top: 20),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(_totalPages, (index) {
                return AnimatedContainer(
                  duration: const Duration(milliseconds: 300),
                  width: index == _currentPage ? 24 : 8,
                  height: 8,
                  margin: const EdgeInsets.symmetric(horizontal: 4),
                  decoration: BoxDecoration(
                    color: index == _currentPage
                        ? const Color(0xFF5DADE2)
                        : Colors.white.withOpacity(0.3),
                    borderRadius: BorderRadius.circular(4),
                  ),
                );
              }),
            ),
          ),
        );
      },
    );
  }

  // Enhanced Page 0 with sophisticated animations
  Widget _buildPage0() {
    return AnimatedBuilder(
      animation: _mainAnimationController,
      builder: (context, child) {
        return Stack(
          children: [
            // Floating background elements
            _buildFloatingElements(),

            Padding(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  // Animated logo with multiple effects
                  Transform.scale(
                    scale: _illustrationScaleAnimation.value,
                    child: Transform.rotate(
                      angle: _illustrationRotateAnimation.value,
                      child: AnimatedBuilder(
                        animation: _pulseController,
                        builder: (context, child) {
                          return Transform.scale(
                            scale: _pulseAnimation.value,
                            child: _buildEnhancedLogo(),
                          );
                        },
                      ),
                    ),
                  ),

                  const SizedBox(height: 40),

                  // Staggered title animation
                  FadeTransition(
                    opacity: _titleFadeAnimation,
                    child: SlideTransition(
                      position: _titleSlideAnimation,
                      child: const Text(
                        'Practice English with dictation exercises',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 28,
                          fontWeight: FontWeight.bold,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),

                  const SizedBox(height: 24),

                  // Staggered description animation
                  FadeTransition(
                    opacity: _descriptionFadeAnimation,
                    child: SlideTransition(
                      position: _descriptionSlideAnimation,
                      child: const Text(
                        'Dictation is a method to learn languages by listening and writing down what you hear. It is a highly effective method!',
                        style: TextStyle(
                          color: Color(0xFFBDC3C7),
                          fontSize: 18,
                          height: 1.5,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),

                  const SizedBox(height: 40),

                  // Animated "How?" button
                  FadeTransition(
                    opacity: _buttonFadeAnimation,
                    child: SlideTransition(
                      position: _buttonSlideAnimation,
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          GestureDetector(
                            onTap: _nextPage,
                            child: AnimatedBuilder(
                              animation: _floatingController,
                              builder: (context, child) {
                                return Transform.translate(
                                  offset: Offset(_floatingAnimation.value * 0.3, 0),
                                  child: Row(
                                    children: [
                                      Text(
                                        'How?',
                                        style: TextStyle(
                                          color: Colors.white.withOpacity(0.7),
                                          fontSize: 18,
                                          fontWeight: FontWeight.w500,
                                        ),
                                      ),
                                      const SizedBox(width: 8),
                                      Icon(
                                        Icons.arrow_forward_ios,
                                        color: Colors.white.withOpacity(0.7),
                                        size: 18,
                                      ),
                                    ],
                                  ),
                                );
                              },
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),

                  const SizedBox(height: 60),

                  // Animated action buttons
                  FadeTransition(
                    opacity: _buttonFadeAnimation,
                    child: SlideTransition(
                      position: _buttonSlideAnimation,
                      child: _buildAnimatedButtons(),
                    ),
                  ),
                ],
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildEnhancedLogo() {
    return Container(
      width: 150,
      height: 150,
      margin: const EdgeInsets.only(bottom: 60),
      child: Stack(
        alignment: Alignment.center,
        children: [
          // Outer glow effect
          Container(
            width: 200,
            height: 200,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(25),
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFF3498DB).withOpacity(0.3),
                  blurRadius: 30,
                  spreadRadius: 5,
                ),
              ],
            ),
          ),
          // Main logo container
          Container(
            width: 180,
            height: 180,
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  Color(0xFF5DADE2),
                  Color(0xFF3498DB),
                ],
              ),
              borderRadius: BorderRadius.circular(20),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.2),
                  blurRadius: 15,
                  offset: const Offset(0, 8),
                ),
              ],
            ),
            child: const Center(
              child: Text(
                'D',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 120,
                  fontWeight: FontWeight.bold,
                  shadows: [
                    Shadow(
                      color: Colors.black26,
                      offset: Offset(2, 2),
                      blurRadius: 4,
                    ),
                  ],
                ),
              ),
            ),
          ),
          // Inner highlight
          Positioned(
            child: Container(
              width: 140,
              height: 140,
              decoration: BoxDecoration(
                border: Border.all(
                  color: Colors.white.withOpacity(0.3),
                  width: 3,
                ),
                borderRadius: BorderRadius.circular(15),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFloatingElements() {
    return AnimatedBuilder(
      animation: _floatingController,
      builder: (context, child) {
        return Stack(
          children: [
            // Floating circles
            Positioned(
              top: 100 + _floatingAnimation.value,
              left: 50,
              child: Container(
                width: 20,
                height: 20,
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.1),
                  shape: BoxShape.circle,
                ),
              ),
            ),
            Positioned(
              top: 200 - _floatingAnimation.value * 0.5,
              right: 80,
              child: Container(
                width: 15,
                height: 15,
                decoration: BoxDecoration(
                  color: const Color(0xFF5DADE2).withOpacity(0.3),
                  shape: BoxShape.circle,
                ),
              ),
            ),
            Positioned(
              bottom: 150 + _floatingAnimation.value * 0.8,
              left: 100,
              child: Container(
                width: 25,
                height: 25,
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.05),
                  shape: BoxShape.circle,
                ),
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildAnimatedButtons() {
    return Column(
      children: [
        // Practice now button with hover effect
        AnimatedBuilder(
          animation: _pulseController,
          builder: (context, child) {
            return Transform.scale(
              scale: 1.0 + (_pulseAnimation.value - 1.0) * 0.02,
              child: Container(
                width: double.infinity,
                height: 56,
                margin: const EdgeInsets.only(bottom: 16),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(28),
                  boxShadow: [
                    BoxShadow(
                      color: const Color(0xFF7FB3D3).withOpacity(0.3),
                      blurRadius: 15,
                      offset: const Offset(0, 5),
                    ),
                  ],
                ),
                child: ElevatedButton(
                  onPressed: () {
                    Navigator.pushNamed(context, '/topics');
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF7FB3D3),
                    foregroundColor: const Color(0xFF2C3E50),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(28),
                    ),
                    elevation: 0,
                  ),
                  child: const Text(
                    'Practice now!',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ),
            );
          },
        ),

        // Upgrade PRO button with enhanced styling
        Container(
          width: double.infinity,
          height: 56,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(28),
            gradient: const LinearGradient(
              colors: [Color(0xFF7FB3D3), Color(0xFF5DADE2)],
            ),
            boxShadow: [
              BoxShadow(
                color: const Color(0xFF7FB3D3).withOpacity(0.3),
                blurRadius: 15,
                offset: const Offset(0, 5),
              ),
            ],
          ),
          child: ElevatedButton(
            onPressed: () {
              // Handle upgrade
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.transparent,
              foregroundColor: const Color(0xFF2C3E50),
              shadowColor: Colors.transparent,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(28),
              ),
            ),
            child: const Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text('🔥', style: TextStyle(fontSize: 18)),
                SizedBox(width: 8),
                Text(
                  'Upgrade PRO',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                SizedBox(width: 8),
                Text('🔥', style: TextStyle(fontSize: 18)),
              ],
            ),
          ),
        ),
      ],
    );
  }

  // Enhanced Page 1 with dynamic animations
  Widget _buildPage1() {
    return AnimatedBuilder(
      animation: _mainAnimationController,
      builder: (context, child) {
        return Stack(
          children: [
            _buildFloatingElements(),
            Padding(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  // Enhanced illustration with multiple animation layers
                  Transform.scale(
                    scale: _illustrationScaleAnimation.value,
                    child: Transform.rotate(
                      angle: _illustrationRotateAnimation.value * 0.1,
                      child: _buildEnhancedPersonWithHeadphones(),
                    ),
                  ),

                  const SizedBox(height: 40),

                  FadeTransition(
                    opacity: _titleFadeAnimation,
                    child: SlideTransition(
                      position: _titleSlideAnimation,
                      child: const Text(
                        '1. Listen to the audio',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 32,
                          fontWeight: FontWeight.bold,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),

                  const SizedBox(height: 24),

                  FadeTransition(
                    opacity: _descriptionFadeAnimation,
                    child: SlideTransition(
                      position: _descriptionSlideAnimation,
                      child: const Text(
                        'Through the exercises, you will have to listen a lot; that\'s the key to improving your listening skills in any learning method.',
                        style: TextStyle(
                          color: Color(0xFFBDC3C7),
                          fontSize: 18,
                          height: 1.5,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),

                  const SizedBox(height: 60),

                  FadeTransition(
                    opacity: _buttonFadeAnimation,
                    child: SlideTransition(
                      position: _buttonSlideAnimation,
                      child: _buildAnimatedNextButton(),
                    ),
                  ),
                ],
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildEnhancedPersonWithHeadphones() {
    return Container(
      width: 200,
      height: 200,
      margin: const EdgeInsets.only(bottom: 60),
      child: Stack(
        alignment: Alignment.center,
        children: [
          // Person with enhanced styling
          Container(
            width: 120,
            height: 140,
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [Color(0xFFE8B4A0), Color(0xFFD4A574)],
              ),
              borderRadius: BorderRadius.circular(20),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.2),
                  blurRadius: 10,
                  offset: const Offset(0, 5),
                ),
              ],
            ),
            child: Stack(
              children: [
                // Enhanced hair with gradient
                Positioned(
                  top: 0,
                  left: 10,
                  right: 10,
                  child: Container(
                    height: 40,
                    decoration: const BoxDecoration(
                      gradient: LinearGradient(
                        colors: [Color(0xFFB8860B), Color(0xFF8B6914)],
                      ),
                      borderRadius: BorderRadius.only(
                        topLeft: Radius.circular(20),
                        topRight: Radius.circular(20),
                      ),
                    ),
                  ),
                ),
                // Animated eyes
                AnimatedBuilder(
                  animation: _floatingController,
                  builder: (context, child) {
                    return Positioned(
                      top: 50,
                      left: 30,
                      child: Transform.scale(
                        scale: 1.0 + math.sin(_floatingController.value * 2 * math.pi) * 0.1,
                        child: Container(
                          width: 8,
                          height: 8,
                          decoration: const BoxDecoration(
                            color: Colors.black,
                            shape: BoxShape.circle,
                          ),
                        ),
                      ),
                    );
                  },
                ),
                AnimatedBuilder(
                  animation: _floatingController,
                  builder: (context, child) {
                    return Positioned(
                      top: 50,
                      right: 30,
                      child: Transform.scale(
                        scale: 1.0 + math.sin(_floatingController.value * 2 * math.pi) * 0.1,
                        child: Container(
                          width: 8,
                          height: 8,
                          decoration: const BoxDecoration(
                            color: Colors.black,
                            shape: BoxShape.circle,
                          ),
                        ),
                      ),
                    );
                  },
                ),
                // Enhanced shirt
                Positioned(
                  bottom: 0,
                  left: 0,
                  right: 0,
                  child: Container(
                    height: 40,
                    decoration: const BoxDecoration(
                      gradient: LinearGradient(
                        colors: [Color(0xFF5DADE2), Color(0xFF3498DB)],
                      ),
                      borderRadius: BorderRadius.only(
                        bottomLeft: Radius.circular(20),
                        bottomRight: Radius.circular(20),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
          // Enhanced headphones with glow effect
          Positioned(
            top: 20,
            child: Container(
              width: 140,
              height: 100,
              decoration: BoxDecoration(
                border: Border.all(color: const Color(0xFF7FB3D3), width: 8),
                borderRadius: BorderRadius.circular(50),
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFF7FB3D3).withOpacity(0.3),
                    blurRadius: 15,
                    spreadRadius: 2,
                  ),
                ],
              ),
            ),
          ),
          // Animated music notes
          AnimatedBuilder(
            animation: _floatingController,
            builder: (context, child) {
              return Positioned(
                top: 40 + _floatingAnimation.value * 0.5,
                right: 20,
                child: Transform.rotate(
                  angle: _floatingAnimation.value * 0.1,
                  child: const Icon(
                    Icons.music_note,
                    color: Color(0xFF5DADE2),
                    size: 24,
                  ),
                ),
              );
            },
          ),
          AnimatedBuilder(
            animation: _floatingController,
            builder: (context, child) {
              return Positioned(
                top: 20 - _floatingAnimation.value * 0.3,
                right: 40,
                child: Transform.rotate(
                  angle: -_floatingAnimation.value * 0.15,
                  child: const Icon(
                    Icons.music_note,
                    color: Color(0xFF5DADE2),
                    size: 20,
                  ),
                ),
              );
            },
          ),
        ],
      ),
    );
  }

  Widget _buildAnimatedNextButton() {
    return AnimatedBuilder(
      animation: _pulseController,
      builder: (context, child) {
        return GestureDetector(
          onTap: _nextPage,
          child: Transform.scale(
            scale: _pulseAnimation.value,
            child: Container(
              width: 60,
              height: 60,
              decoration: BoxDecoration(
                gradient: RadialGradient(
                  colors: [
                    Colors.white.withOpacity(0.2),
                    Colors.white.withOpacity(0.1),
                  ],
                ),
                shape: BoxShape.circle,
                boxShadow: [
                  BoxShadow(
                    color: Colors.white.withOpacity(0.1),
                    blurRadius: 20,
                    spreadRadius: 5,
                  ),
                ],
              ),
              child: const Icon(
                Icons.arrow_forward_ios,
                color: Colors.white,
                size: 24,
              ),
            ),
          ),
        );
      },
    );
  }

  // Similar enhancements would be applied to pages 2, 3, and 4
  // For brevity, I'll show the structure for page 2
  Widget _buildPage2() {
    return AnimatedBuilder(
      animation: _mainAnimationController,
      builder: (context, child) {
        return Stack(
          children: [
            _buildFloatingElements(),
            Padding(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Transform.scale(
                    scale: _illustrationScaleAnimation.value,
                    child: Transform.rotate(
                      angle: _illustrationRotateAnimation.value * 0.05,
                      child: _buildEnhancedComputerIllustration(),
                    ),
                  ),
                  const SizedBox(height: 40),
                  FadeTransition(
                    opacity: _titleFadeAnimation,
                    child: SlideTransition(
                      position: _titleSlideAnimation,
                      child: const Text(
                        '2. Type what you hear',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 32,
                          fontWeight: FontWeight.bold,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
                  const SizedBox(height: 24),
                  FadeTransition(
                    opacity: _descriptionFadeAnimation,
                    child: SlideTransition(
                      position: _descriptionSlideAnimation,
                      child: const Text(
                        'Typing what you hear forces you to focus on every detail which helps you become better at pronunciation, spelling and writing.',
                        style: TextStyle(
                          color: Color(0xFFBDC3C7),
                          fontSize: 18,
                          height: 1.5,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
                  const SizedBox(height: 60),
                  FadeTransition(
                    opacity: _buttonFadeAnimation,
                    child: SlideTransition(
                      position: _buttonSlideAnimation,
                      child: _buildAnimatedNextButton(),
                    ),
                  ),
                ],
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildEnhancedComputerIllustration() {
    return Container(
      width: 200,
      height: 200,
      margin: const EdgeInsets.only(bottom: 60),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Enhanced monitor with glow effect
          Container(
            width: 140,
            height: 100,
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [Color(0xFF85C1E9), Color(0xFF5DADE2)],
              ),
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: const Color(0xFF5DADE2), width: 3),
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFF5DADE2).withOpacity(0.3),
                  blurRadius: 15,
                  spreadRadius: 2,
                ),
              ],
            ),
            child: Container(
              margin: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Color(0xFF5DADE2), Color(0xFF3498DB)],
                ),
                borderRadius: BorderRadius.circular(4),
              ),
              child: Column(
                children: [
                  Container(
                    height: 20,
                    margin: const EdgeInsets.all(8),
                    child: Row(
                      children: [
                        Container(
                          width: 20,
                          height: 16,
                          decoration: BoxDecoration(
                            gradient: const LinearGradient(
                              colors: [Color(0xFF3498DB), Color(0xFF2980B9)],
                            ),
                            borderRadius: BorderRadius.circular(2),
                          ),
                        ),
                        const SizedBox(width: 4),
                        Expanded(
                          child: Column(
                            children: [
                              Container(
                                height: 4,
                                decoration: BoxDecoration(
                                  color: const Color(0xFF3498DB),
                                  borderRadius: BorderRadius.circular(2),
                                ),
                              ),
                              const SizedBox(height: 2),
                              Container(
                                height: 4,
                                width: 60,
                                decoration: BoxDecoration(
                                  color: const Color(0xFF3498DB),
                                  borderRadius: BorderRadius.circular(2),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                  // Animated typing cursor
                  AnimatedBuilder(
                    animation: _pulseController,
                    builder: (context, child) {
                      return Container(
                        height: 2,
                        margin: const EdgeInsets.symmetric(horizontal: 8),
                        decoration: BoxDecoration(
                          color: Color.lerp(
                            const Color(0xFF3498DB),
                            Colors.white,
                            _pulseAnimation.value - 1.0,
                          ),
                          borderRadius: BorderRadius.circular(1),
                        ),
                      );
                    },
                  ),
                ],
              ),
            ),
          ),
          // Enhanced monitor stand
          Container(
            width: 20,
            height: 20,
            margin: const EdgeInsets.only(top: 4),
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                colors: [Color(0xFFBDC3C7), Color(0xFF95A5A6)],
              ),
              borderRadius: BorderRadius.only(
                bottomLeft: Radius.circular(10),
                bottomRight: Radius.circular(10),
              ),
            ),
          ),
          const SizedBox(height: 8),
          // Enhanced keyboard with animated hands
          Container(
            width: 160,
            height: 40,
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [Color(0xFFE8B4A0), Color(0xFFD4A574)],
              ),
              borderRadius: BorderRadius.circular(8),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.1),
                  blurRadius: 5,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Container(
              margin: const EdgeInsets.all(4),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Color(0xFFBDC3C7), Color(0xFF95A5A6)],
                ),
                borderRadius: BorderRadius.circular(4),
              ),
              child: Row(
                children: [
                  // Animated typing hands
                  AnimatedBuilder(
                    animation: _floatingController,
                    builder: (context, child) {
                      return Container(
                        width: 20,
                        height: 16,
                        margin: EdgeInsets.only(
                          left: 8,
                          top: 8 + _floatingAnimation.value * 0.2,
                        ),
                        decoration: BoxDecoration(
                          color: const Color(0xFFE8B4A0),
                          borderRadius: BorderRadius.circular(8),
                        ),
                      );
                    },
                  ),
                  const Spacer(),
                  AnimatedBuilder(
                    animation: _floatingController,
                    builder: (context, child) {
                      return Container(
                        width: 20,
                        height: 16,
                        margin: EdgeInsets.only(
                          right: 8,
                          top: 8 - _floatingAnimation.value * 0.2,
                        ),
                        decoration: BoxDecoration(
                          color: const Color(0xFFE8B4A0),
                          borderRadius: BorderRadius.circular(8),
                        ),
                      );
                    },
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  // Pages 3 and 4 would follow similar enhancement patterns
  Widget _buildPage3() {
    return AnimatedBuilder(
      animation: _mainAnimationController,
      builder: (context, child) {
        return Stack(
          children: [
            _buildFloatingElements(),
            Padding(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Transform.scale(
                    scale: _illustrationScaleAnimation.value,
                    child: AnimatedBuilder(
                      animation: _pulseController,
                      builder: (context, child) {
                        return Transform.scale(
                          scale: _pulseAnimation.value,
                          child: _buildEnhancedCheckmark(),
                        );
                      },
                    ),
                  ),
                  const SizedBox(height: 40),
                  FadeTransition(
                    opacity: _titleFadeAnimation,
                    child: SlideTransition(
                      position: _titleSlideAnimation,
                      child: const Text(
                        '3. Check & correct',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 32,
                          fontWeight: FontWeight.bold,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
                  const SizedBox(height: 24),
                  FadeTransition(
                    opacity: _descriptionFadeAnimation,
                    child: SlideTransition(
                      position: _descriptionSlideAnimation,
                      child: const Text(
                        'Error correction is important for your listening accuracy and reading comprehension, it\'s best to learn from mistakes.',
                        style: TextStyle(
                          color: Color(0xFFBDC3C7),
                          fontSize: 18,
                          height: 1.5,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
                  const SizedBox(height: 60),
                  FadeTransition(
                    opacity: _buttonFadeAnimation,
                    child: SlideTransition(
                      position: _buttonSlideAnimation,
                      child: _buildAnimatedNextButton(),
                    ),
                  ),
                ],
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildEnhancedCheckmark() {
    return Container(
      width: 200,
      height: 200,
      margin: const EdgeInsets.only(bottom: 60),
      child: Stack(
        alignment: Alignment.center,
        children: [
          // Outer glow
          Container(
            width: 200,
            height: 200,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFF7CB342).withOpacity(0.3),
                  blurRadius: 30,
                  spreadRadius: 10,
                ),
              ],
            ),
          ),
          // Outer circle with gradient
          Container(
            width: 180,
            height: 180,
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                colors: [Color(0xFFE8E8E8), Color(0xFFBDBDBD)],
              ),
              shape: BoxShape.circle,
            ),
          ),
          // Inner circle with enhanced gradient
          Container(
            width: 140,
            height: 140,
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [Color(0xFF8BC34A), Color(0xFF7CB342)],
              ),
              shape: BoxShape.circle,
            ),
          ),
          // Enhanced checkmark with shadow
          const Icon(
            Icons.check,
            color: Colors.white,
            size: 60,
            shadows: [
              Shadow(
                color: Colors.black26,
                offset: Offset(2, 2),
                blurRadius: 4,
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildPage4() {
    return AnimatedBuilder(
      animation: _mainAnimationController,
      builder: (context, child) {
        return Stack(
          children: [
            _buildFloatingElements(),
            Padding(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Transform.scale(
                    scale: _illustrationScaleAnimation.value,
                    child: Transform.rotate(
                      angle: _illustrationRotateAnimation.value * 0.05,
                      child: _buildEnhancedSpeakingPerson(),
                    ),
                  ),
                  const SizedBox(height: 40),
                  FadeTransition(
                    opacity: _titleFadeAnimation,
                    child: SlideTransition(
                      position: _titleSlideAnimation,
                      child: const Text(
                        '4. Read it out loud',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 32,
                          fontWeight: FontWeight.bold,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
                  const SizedBox(height: 24),
                  FadeTransition(
                    opacity: _descriptionFadeAnimation,
                    child: SlideTransition(
                      position: _descriptionSlideAnimation,
                      child: const Text(
                        'After complete a sentence, try to read it out loud, it will greatly improve your pronunciation & speaking skills!',
                        style: TextStyle(
                          color: Color(0xFFBDC3C7),
                          fontSize: 18,
                          height: 1.5,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
                  const SizedBox(height: 60),
                  FadeTransition(
                    opacity: _buttonFadeAnimation,
                    child: SlideTransition(
                      position: _buttonSlideAnimation,
                      child: _buildAnimatedNextButton(),
                    ),
                  ),
                ],
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildEnhancedSpeakingPerson() {
    return Container(
      width: 200,
      height: 200,
      margin: const EdgeInsets.only(bottom: 60),
      child: Stack(
        alignment: Alignment.center,
        children: [
          // Person with enhanced styling
          Positioned(
            bottom: 0,
            child: Container(
              width: 120,
              height: 140,
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Color(0xFFE8B4A0), Color(0xFFD4A574)],
                ),
                borderRadius: BorderRadius.circular(20),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.2),
                    blurRadius: 10,
                    offset: const Offset(0, 5),
                  ),
                ],
              ),
              child: Stack(
                children: [
                  // Enhanced hair
                  Positioned(
                    top: 0,
                    left: 10,
                    right: 10,
                    child: Container(
                      height: 40,
                      decoration: const BoxDecoration(
                        gradient: LinearGradient(
                          colors: [Color(0xFF5D4037), Color(0xFF3E2723)],
                        ),
                        borderRadius: BorderRadius.only(
                          topLeft: Radius.circular(20),
                          topRight: Radius.circular(20),
                        ),
                      ),
                    ),
                  ),
                  // Animated eyes
                  Positioned(
                    top: 50,
                    left: 25,
                    child: Container(
                      width: 6,
                      height: 12,
                      decoration: const BoxDecoration(
                        color: Colors.black,
                        borderRadius: BorderRadius.all(Radius.circular(3)),
                      ),
                    ),
                  ),
                  Positioned(
                    top: 50,
                    right: 25,
                    child: Container(
                      width: 6,
                      height: 12,
                      decoration: const BoxDecoration(
                        color: Colors.black,
                        borderRadius: BorderRadius.all(Radius.circular(3)),
                      ),
                    ),
                  ),
                  // Animated mouth
                  AnimatedBuilder(
                    animation: _pulseController,
                    builder: (context, child) {
                      return Positioned(
                        top: 70,
                        left: 45,
                        child: Transform.scale(
                          scale: _pulseAnimation.value,
                          child: Container(
                            width: 30,
                            height: 15,
                            decoration: const BoxDecoration(
                              color: Colors.black,
                              borderRadius: BorderRadius.all(Radius.circular(15)),
                            ),
                          ),
                        ),
                      );
                    },
                  ),
                  // Animated cheeks
                  AnimatedBuilder(
                    animation: _pulseController,
                    builder: (context, child) {
                      return Positioned(
                        top: 60,
                        left: 10,
                        child: Transform.scale(
                          scale: _pulseAnimation.value,
                          child: Container(
                            width: 20,
                            height: 20,
                            decoration: const BoxDecoration(
                              color: Color(0xFFFF8A80),
                              shape: BoxShape.circle,
                            ),
                          ),
                        ),
                      );
                    },
                  ),
                  AnimatedBuilder(
                    animation: _pulseController,
                    builder: (context, child) {
                      return Positioned(
                        top: 60,
                        right: 10,
                        child: Transform.scale(
                          scale: _pulseAnimation.value,
                          child: Container(
                            width: 20,
                            height: 20,
                            decoration: const BoxDecoration(
                              color: Color(0xFFFF8A80),
                              shape: BoxShape.circle,
                            ),
                          ),
                        ),
                      );
                    },
                  ),
                  // Enhanced shirt
                  Positioned(
                    bottom: 0,
                    left: 0,
                    right: 0,
                    child: Container(
                      height: 40,
                      decoration: const BoxDecoration(
                        gradient: LinearGradient(
                          colors: [Color(0xFF5DADE2), Color(0xFF3498DB)],
                        ),
                        borderRadius: BorderRadius.only(
                          bottomLeft: Radius.circular(20),
                          bottomRight: Radius.circular(20),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          // Enhanced speech bubble with animation
          AnimatedBuilder(
            animation: _floatingController,
            builder: (context, child) {
              return Positioned(
                top: 20 + _floatingAnimation.value * 0.3,
                right: 10,
                child: Transform.scale(
                  scale: 1.0 + math.sin(_floatingController.value * 2 * math.pi) * 0.05,
                  child: Container(
                    width: 80,
                    height: 60,
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(
                        colors: [Color(0xFFF5F5DC), Color(0xFFE8E8DC)],
                      ),
                      borderRadius: BorderRadius.circular(20),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.1),
                          blurRadius: 5,
                          offset: const Offset(0, 2),
                        ),
                      ],
                    ),
                    child: const Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        SizedBox(
                          width: 40,
                          height: 4,
                          child: DecoratedBox(
                            decoration: BoxDecoration(
                              color: Color(0xFF7CB342),
                              borderRadius: BorderRadius.all(Radius.circular(2)),
                            ),
                          ),
                        ),
                        SizedBox(height: 6),
                        SizedBox(
                          width: 30,
                          height: 4,
                          child: DecoratedBox(
                            decoration: BoxDecoration(
                              color: Color(0xFF7CB342),
                              borderRadius: BorderRadius.all(Radius.circular(2)),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
        ],
      ),
    );
  }
}
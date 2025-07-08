import 'package:flutter/material.dart';

import '../nav/app_bottom_navigation.dart';

class OnboardingScreen extends StatefulWidget {
  const OnboardingScreen({super.key});

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen>
    with TickerProviderStateMixin {
  late PageController _pageController;
  late AnimationController _animationController;
  late Animation<double> _fadeAnimation;
  late Animation<Offset> _slideAnimation;
  int _currentNavIndex = 0;
  int _currentPage = 0;
  final int _totalPages = 5;

  @override
  void initState() {
    super.initState();
    _pageController = PageController();

    // Animation controller for smooth transitions
    _animationController = AnimationController(
      duration: const Duration(milliseconds: 600),
      vsync: this,
    );

    // Fade animation for content
    _fadeAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeInOut,
    ));

    // Slide animation for illustrations
    _slideAnimation = Tween<Offset>(
      begin: const Offset(0, 0.3),
      end: Offset.zero,
    ).animate(CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeOutBack,
    ));

    _animationController.forward();
  }

  @override
  void dispose() {
    _pageController.dispose();
    _animationController.dispose();
    super.dispose();
  }

  void _onPageChanged(int page) {
    setState(() {
      _currentPage = page;
    });

    // Restart animation for new page
    _animationController.reset();
    _animationController.forward();
  }

  void _nextPage() {
    if (_currentPage < _totalPages - 1) {
      _pageController.nextPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF2C3E50),
      body: SafeArea(
        child: Column(
          children: [
            Expanded(
              child: PageView(
                controller: _pageController,
                onPageChanged: _onPageChanged,
                children: [
                  _buildPage0(), // Practice English intro
                  _buildPage1(), // Listen to audio
                  _buildPage2(), // Type what you hear
                  _buildPage3(), // Check & correct
                  _buildPage4(), // Read it out loud
                ],
              ),
            ),
            Container(
              padding: const EdgeInsets.only(top: 20),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: List.generate(_totalPages, (index) {
                  return Container(
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
            AppBottomNavigation(
              currentIndex: _currentNavIndex,
              context: context,
            ),
          ],
        ),
      ),
    );
  }


  // Page 0: Practice English intro
  Widget _buildPage0() {
    return AnimatedBuilder(
      animation: _animationController,
      builder: (context, child) {
        return FadeTransition(
          opacity: _fadeAnimation,
          child: SlideTransition(
            position: _slideAnimation,
            child: Padding(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Container(
                    width: 150,
                    height: 150,
                    margin: const EdgeInsets.only(bottom: 60),
                    child: Stack(
                      alignment: Alignment.center,
                      children: [
                        // Outer D
                        Container(
                          width: 180,
                          height: 180,
                          decoration: BoxDecoration(
                            color: const Color(0xFF3498DB),
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: const Center(
                            child: Text(
                              'D',
                              style: TextStyle(
                                color: Colors.white,
                                fontSize: 120,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ),
                        // Inner D outline
                        Positioned(
                          child: Container(
                            width: 140,
                            height: 140,
                            decoration: BoxDecoration(
                              border: Border.all(color: const Color(0xFF2C3E50), width: 6),
                              borderRadius: BorderRadius.circular(15),
                            ),
                            child: const Center(
                              child: Text(
                                '',
                                style: TextStyle(
                                  color: Color(0xFF2C3E50),
                                  fontSize: 80,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),

                  // Title
                  const Text(
                    'Practice English with dictation exercises',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 28,
                      fontWeight: FontWeight.bold,
                    ),
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 24),

                  // Description
                  const Text(
                    'Dictation is a method to learn languages by listening and writing down what you hear. It is a highly effective method!',
                    style: TextStyle(
                      color: Color(0xFFBDC3C7),
                      fontSize: 18,
                      height: 1.5,
                    ),
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 40),

                  // How? button
                  Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: [
                      GestureDetector(
                        onTap: _nextPage,
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
                      ),
                    ],
                  ),

                  const SizedBox(height: 60),

                  // Action buttons
                  Column(
                    children: [
                      // Practice now button
                      Container(
                        width: double.infinity,
                        height: 56,
                        margin: const EdgeInsets.only(bottom: 16),
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

                      // Upgrade PRO button
                      Container(
                        width: double.infinity,
                        height: 56,
                        child: ElevatedButton(
                          onPressed: () {
                            // Handle upgrade
                          },
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFF7FB3D3),
                            foregroundColor: const Color(0xFF2C3E50),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(28),
                            ),
                            elevation: 0,
                          ),
                          child: const Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Text(
                                '🔥',
                                style: TextStyle(fontSize: 18),
                              ),
                              SizedBox(width: 8),
                              Text(
                                'Upgrade PRO',
                                style: TextStyle(
                                  fontSize: 18,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                              SizedBox(width: 8),
                              Text(
                                '🔥',
                                style: TextStyle(fontSize: 18),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
  // Page 1: Listen to the audio
  Widget _buildPage1() {
    return AnimatedBuilder(
      animation: _animationController,
      builder: (context, child) {
        return FadeTransition(
          opacity: _fadeAnimation,
          child: SlideTransition(
            position: _slideAnimation,
            child: Padding(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  // Illustration: Person with headphones
                  Container(
                    width: 200,
                    height: 200,
                    margin: const EdgeInsets.only(bottom: 60),
                    child: Stack(
                      alignment: Alignment.center,
                      children: [
                        // Person illustration
                        Container(
                          width: 120,
                          height: 140,
                          decoration: BoxDecoration(
                            color: const Color(0xFFE8B4A0), // Skin tone
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: Stack(
                            children: [
                              // Hair
                              Positioned(
                                top: 0,
                                left: 10,
                                right: 10,
                                child: Container(
                                  height: 40,
                                  decoration: const BoxDecoration(
                                    color: Color(0xFFB8860B),
                                    borderRadius: BorderRadius.only(
                                      topLeft: Radius.circular(20),
                                      topRight: Radius.circular(20),
                                    ),
                                  ),
                                ),
                              ),
                              // Face features
                              Positioned(
                                top: 50,
                                left: 30,
                                child: Container(
                                  width: 8,
                                  height: 8,
                                  decoration: const BoxDecoration(
                                    color: Colors.black,
                                    shape: BoxShape.circle,
                                  ),
                                ),
                              ),
                              Positioned(
                                top: 50,
                                right: 30,
                                child: Container(
                                  width: 8,
                                  height: 8,
                                  decoration: const BoxDecoration(
                                    color: Colors.black,
                                    shape: BoxShape.circle,
                                  ),
                                ),
                              ),
                              // Shirt
                              Positioned(
                                bottom: 0,
                                left: 0,
                                right: 0,
                                child: Container(
                                  height: 40,
                                  decoration: const BoxDecoration(
                                    color: Color(0xFF5DADE2),
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
                        // Headphones
                        Positioned(
                          top: 20,
                          child: Container(
                            width: 140,
                            height: 100,
                            decoration: BoxDecoration(
                              border: Border.all(color: const Color(0xFF7FB3D3), width: 8),
                              borderRadius: BorderRadius.circular(50),
                            ),
                          ),
                        ),
                        // Music notes
                        const Positioned(
                          top: 40,
                          right: 20,
                          child: Icon(
                            Icons.music_note,
                            color: Color(0xFF5DADE2),
                            size: 24,
                          ),
                        ),
                        const Positioned(
                          top: 20,
                          right: 40,
                          child: Icon(
                            Icons.music_note,
                            color: Color(0xFF5DADE2),
                            size: 20,
                          ),
                        ),
                      ],
                    ),
                  ),

                  // Title
                  const Text(
                    '1. Listen to the audio',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 32,
                      fontWeight: FontWeight.bold,
                    ),
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 24),

                  // Description
                  const Text(
                    'Through the exercises, you will have to listen a lot; that\'s the key to improving your listening skills in any learning method.',
                    style: TextStyle(
                      color: Color(0xFFBDC3C7),
                      fontSize: 18,
                      height: 1.5,
                    ),
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 60),

                  // Next arrow
                  GestureDetector(
                    onTap: _nextPage,
                    child: Container(
                      width: 60,
                      height: 60,
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.1),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.arrow_forward_ios,
                        color: Colors.white,
                        size: 24,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  // Page 2: Type what you hear
  Widget _buildPage2() {
    return AnimatedBuilder(
      animation: _animationController,
      builder: (context, child) {
        return FadeTransition(
          opacity: _fadeAnimation,
          child: SlideTransition(
            position: _slideAnimation,
            child: Padding(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  // Illustration: Computer with keyboard
                  Container(
                    width: 200,
                    height: 200,
                    margin: const EdgeInsets.only(bottom: 60),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        // Monitor
                        Container(
                          width: 140,
                          height: 100,
                          decoration: BoxDecoration(
                            color: const Color(0xFF85C1E9),
                            borderRadius: BorderRadius.circular(8),
                            border: Border.all(color: const Color(0xFF5DADE2), width: 3),
                          ),
                          child: Container(
                            margin: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: const Color(0xFF5DADE2),
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
                                          color: const Color(0xFF3498DB),
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
                                Container(
                                  height: 2,
                                  margin: const EdgeInsets.symmetric(horizontal: 8),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFF3498DB),
                                    borderRadius: BorderRadius.circular(1),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),

                        // Monitor stand
                        Container(
                          width: 20,
                          height: 20,
                          margin: const EdgeInsets.only(top: 4),
                          decoration: const BoxDecoration(
                            color: Color(0xFFBDC3C7),
                            borderRadius: BorderRadius.only(
                              bottomLeft: Radius.circular(10),
                              bottomRight: Radius.circular(10),
                            ),
                          ),
                        ),

                        const SizedBox(height: 8),

                        // Keyboard
                        Container(
                          width: 160,
                          height: 40,
                          decoration: BoxDecoration(
                            color: const Color(0xFFE8B4A0),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Container(
                            margin: const EdgeInsets.all(4),
                            decoration: BoxDecoration(
                              color: const Color(0xFFBDC3C7),
                              borderRadius: BorderRadius.circular(4),
                            ),
                            child: Row(
                              children: [
                                // Hands
                                Container(
                                  width: 20,
                                  height: 16,
                                  margin: const EdgeInsets.only(left: 8, top: 8),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFFE8B4A0),
                                    borderRadius: BorderRadius.circular(8),
                                  ),
                                ),
                                const Spacer(),
                                Container(
                                  width: 20,
                                  height: 16,
                                  margin: const EdgeInsets.only(right: 8, top: 8),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFFE8B4A0),
                                    borderRadius: BorderRadius.circular(8),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),

                  // Title
                  const Text(
                    '2. Type what you hear',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 32,
                      fontWeight: FontWeight.bold,
                    ),
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 24),

                  // Description
                  const Text(
                    'Typing what you hear forces you to focus on every detail which helps you become better at pronunciation, spelling and writing.',
                    style: TextStyle(
                      color: Color(0xFFBDC3C7),
                      fontSize: 18,
                      height: 1.5,
                    ),
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 60),

                  // Next arrow
                  GestureDetector(
                    onTap: _nextPage,
                    child: Container(
                      width: 60,
                      height: 60,
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.1),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.arrow_forward_ios,
                        color: Colors.white,
                        size: 24,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  // Page 3: Check & correct
  Widget _buildPage3() {
    return AnimatedBuilder(
      animation: _animationController,
      builder: (context, child) {
        return FadeTransition(
          opacity: _fadeAnimation,
          child: SlideTransition(
            position: _slideAnimation,
            child: Padding(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  // Illustration: Green checkmark circle
                  Container(
                    width: 200,
                    height: 200,
                    margin: const EdgeInsets.only(bottom: 60),
                    child: Stack(
                      alignment: Alignment.center,
                      children: [
                        // Outer circle (light gray)
                        Container(
                          width: 180,
                          height: 180,
                          decoration: const BoxDecoration(
                            color: Color(0xFFE8E8E8),
                            shape: BoxShape.circle,
                          ),
                        ),
                        // Inner circle (green)
                        Container(
                          width: 140,
                          height: 140,
                          decoration: const BoxDecoration(
                            color: Color(0xFF7CB342),
                            shape: BoxShape.circle,
                          ),
                        ),
                        // Checkmark
                        const Icon(
                          Icons.check,
                          color: Colors.white,
                          size: 60,
                        ),
                      ],
                    ),
                  ),

                  // Title
                  const Text(
                    '3. Check & correct',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 32,
                      fontWeight: FontWeight.bold,
                    ),
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 24),

                  // Description
                  const Text(
                    'Error correction is important for your listening accuracy and reading comprehension, it\'s best to learn from mistakes.',
                    style: TextStyle(
                      color: Color(0xFFBDC3C7),
                      fontSize: 18,
                      height: 1.5,
                    ),
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 60),

                  // Next arrow
                  GestureDetector(
                    onTap: _nextPage,
                    child: Container(
                      width: 60,
                      height: 60,
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.1),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.arrow_forward_ios,
                        color: Colors.white,
                        size: 24,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  // Page 4: Read it out loud
  Widget _buildPage4() {
    return AnimatedBuilder(
      animation: _animationController,
      builder: (context, child) {
        return FadeTransition(
          opacity: _fadeAnimation,
          child: SlideTransition(
            position: _slideAnimation,
            child: Padding(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  // Illustration: Person with speech bubble
                  Container(
                    width: 200,
                    height: 200,
                    margin: const EdgeInsets.only(bottom: 60),
                    child: Stack(
                      alignment: Alignment.center,
                      children: [
                        // Person
                        Positioned(
                          bottom: 0,
                          child: Container(
                            width: 120,
                            height: 140,
                            decoration: BoxDecoration(
                              color: const Color(0xFFE8B4A0),
                              borderRadius: BorderRadius.circular(20),
                            ),
                            child: Stack(
                              children: [
                                // Hair
                                Positioned(
                                  top: 0,
                                  left: 10,
                                  right: 10,
                                  child: Container(
                                    height: 40,
                                    decoration: const BoxDecoration(
                                      color: Color(0xFF5D4037),
                                      borderRadius: BorderRadius.only(
                                        topLeft: Radius.circular(20),
                                        topRight: Radius.circular(20),
                                      ),
                                    ),
                                  ),
                                ),
                                // Eyes
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
                                // Mouth
                                Positioned(
                                  top: 70,
                                  left: 45,
                                  child: Container(
                                    width: 30,
                                    height: 15,
                                    decoration: const BoxDecoration(
                                      color: Colors.black,
                                      borderRadius: BorderRadius.all(Radius.circular(15)),
                                    ),
                                  ),
                                ),
                                // Cheeks
                                Positioned(
                                  top: 60,
                                  left: 10,
                                  child: Container(
                                    width: 20,
                                    height: 20,
                                    decoration: const BoxDecoration(
                                      color: Color(0xFFFF8A80),
                                      shape: BoxShape.circle,
                                    ),
                                  ),
                                ),
                                Positioned(
                                  top: 60,
                                  right: 10,
                                  child: Container(
                                    width: 20,
                                    height: 20,
                                    decoration: const BoxDecoration(
                                      color: Color(0xFFFF8A80),
                                      shape: BoxShape.circle,
                                    ),
                                  ),
                                ),
                                // Shirt
                                Positioned(
                                  bottom: 0,
                                  left: 0,
                                  right: 0,
                                  child: Container(
                                    height: 40,
                                    decoration: const BoxDecoration(
                                      color: Color(0xFF5DADE2),
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
                        // Speech bubble
                        Positioned(
                          top: 20,
                          right: 10,
                          child: Container(
                            width: 80,
                            height: 60,
                            decoration: BoxDecoration(
                              color: const Color(0xFFF5F5DC),
                              borderRadius: BorderRadius.circular(20),
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
                      ],
                    ),
                  ),

                  // Title
                  const Text(
                    '4. Read it out loud',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 32,
                      fontWeight: FontWeight.bold,
                    ),
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 24),

                  // Description
                  const Text(
                    'After complete a sentence, try to read it out loud, it will greatly improve your pronunciation & speaking skills!',
                    style: TextStyle(
                      color: Color(0xFFBDC3C7),
                      fontSize: 18,
                      height: 1.5,
                    ),
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 60),

                  // Next arrow
                  GestureDetector(
                    onTap: _nextPage,
                    child: Container(
                      width: 60,
                      height: 60,
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.1),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.arrow_forward_ios,
                        color: Colors.white,
                        size: 24,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }


}
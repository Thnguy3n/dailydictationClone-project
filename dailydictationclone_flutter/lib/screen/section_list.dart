import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../models/section.dart';
import '../../models/section_filter.dart';
import '../../models/lesson.dart';
import '../locator/locator.dart';
import '../nav/app_bottom_navigation.dart';
import '../service/section_service.dart';
import '../service/lesson_service.dart';
import '../provider/auth_providers.dart';
import 'challenge_screen.dart';

class SectionListPage extends ConsumerStatefulWidget {
  final int topicId;
  final String topicTitle;

  const SectionListPage({
    super.key,
    required this.topicId,
    required this.topicTitle,
  });

  @override
  ConsumerState<SectionListPage> createState() => _SectionListPageState();
}

class _SectionListPageState extends ConsumerState<SectionListPage>
    with TickerProviderStateMixin {
  late final SectionService _sectionService = getIt<SectionService>();
  late final LessonService _lessonService = getIt<LessonService>();

  List<Section> _sections = [];
  bool _isLoading = true;
  String? _errorMessage;
  bool _isPremium = false;
  String? _premiumMessage;

  bool _showFilter = false;
  late AnimationController _filterAnimationController;
  late Animation<double> _filterAnimation;
  SectionFilter _currentFilter = SectionFilter();

  final TextEditingController _lessonTitleController = TextEditingController();
  String? _selectedLevel;
  String? _selectedProgress;

  final List<String> _levelOptions = ['A1', 'A2', 'B1', 'B2', 'C1', 'C2'];
  final List<String> _progressOptions = ['NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'];
  final Map<String, String> _progressLabels = {
    'NOT_STARTED': 'Not Started',
    'IN_PROGRESS': 'In Progress',
    'COMPLETED': 'Completed',
  };

  final Map<int, bool> _expandedSections = {};
  final Map<int, List<LessonResponse>> _sectionLessons = {};
  final Map<int, bool> _loadingLessons = {};
  final Map<int, String> _lessonErrors = {};
  final Map<int, AnimationController> _animationControllers = {};

  @override
  void initState() {
    super.initState();
    _filterAnimationController = AnimationController(
      duration: const Duration(milliseconds: 300),
      vsync: this,
    );
    _filterAnimation = CurvedAnimation(
      parent: _filterAnimationController,
      curve: Curves.easeInOut,
    );
    _loadSections();
  }

  @override
  void dispose() {
    _filterAnimationController.dispose();
    _lessonTitleController.dispose();
    // Dispose all animation controllers
    for (var controller in _animationControllers.values) {
      controller.dispose();
    }
    super.dispose();
  }

  Future<void> _loadSections() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      SectionResult result;

      // Lấy trạng thái xác thực từ Riverpod provider
      // Lưu ý: ref.read() chỉ lấy giá trị hiện tại một lần, không lắng nghe thay đổi
      // Với trường hợp này (kiểm tra khi load sections), ref.read() là phù hợp.
      final isAuthenticated = await ref.read(authStatusProvider.future);

      SectionFilter finalFilter = _currentFilter;
      if (!isAuthenticated) {
        finalFilter = finalFilter.copyWith(challengeProgress: null);
      }


      if (finalFilter.isEmpty) {
        result = await _sectionService.getSections(widget.topicId);
      } else {
        result = await _sectionService.getFilteredSections(widget.topicId, finalFilter); // Sử dụng finalFilter
      }

      setState(() {
        _sections = result.sections;
        _isPremium = result.isPremium;
        _premiumMessage = result.message;
        _isLoading = false;
      });

      // Initialize animation controllers for each section
      for (var section in result.sections) {
        _animationControllers[section.id] = AnimationController(
          duration: const Duration(milliseconds: 300),
          vsync: this,
        );
      }
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
        _isLoading = false;
      });
    }
  }

  void _toggleFilter() {
    setState(() {
      _showFilter = !_showFilter;
    });

    if (_showFilter) {
      _filterAnimationController.forward();
    } else {
      _filterAnimationController.reverse();
    }
  }

  void _applyFilter() async {
    final isAuthenticated = await ref.read(authStatusProvider.future);

    setState(() {
      _currentFilter = SectionFilter(
        level: _selectedLevel?.isNotEmpty == true ? _selectedLevel : null,
        lessonTitle: _lessonTitleController.text.isNotEmpty ? _lessonTitleController.text : null,
        challengeProgress: !isAuthenticated
            ? null
            : (_selectedProgress?.isNotEmpty == true ? _selectedProgress : null),
      );
    });

    _loadSections();
    _toggleFilter();
  }

  void _clearFilter() {
    setState(() {
      _selectedLevel = null;
      _selectedProgress = null;
      _lessonTitleController.clear();
      _currentFilter = SectionFilter();
    });

    _loadSections();
  }

  Future<void> _toggleSection(Section section) async {
    final sectionId = section.id;
    final isExpanded = _expandedSections[sectionId] ?? false;

    if (isExpanded) {
      // Collapse section
      _animationControllers[sectionId]?.reverse();
      setState(() {
        _expandedSections[sectionId] = false;
      });
    } else {
      // Expand section and load lessons if not already loaded
      setState(() {
        _expandedSections[sectionId] = true;
      });
      _animationControllers[sectionId]?.forward();

      if (!_sectionLessons.containsKey(sectionId)) {
        await _loadLessons(sectionId);
      }
    }
  }

  Future<void> _loadLessons(int sectionId) async {
    setState(() {
      _loadingLessons[sectionId] = true;
      _lessonErrors.remove(sectionId);
    });

    try {
      final lessons = await _lessonService.getLessons(sectionId);
      setState(() {
        _sectionLessons[sectionId] = lessons;
        _loadingLessons[sectionId] = false;
      });
    } catch (e) {
      setState(() {
        _lessonErrors[sectionId] = e.toString();
        _loadingLessons[sectionId] = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    // Lắng nghe trạng thái đăng nhập từ authStatusProvider
    // Dùng ref.watch() để widget tự động rebuild khi trạng thái này thay đổi (sau khi Future hoàn thành)
    final isAuthenticatedAsync = ref.watch(authStatusProvider);

    return Scaffold(
      backgroundColor: const Color(0xFF2C3E50),
      appBar: AppBar(
        backgroundColor: const Color(0xFF2C3E50),
        title: Text(
          widget.topicTitle,
          style: const TextStyle(
            fontSize: 24,
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
        leading: Padding(
          padding: const EdgeInsets.only(left: 12.0),
          child: GestureDetector(
            onTap: () => Navigator.pop(context),
            child: Container(
              width: 44,
              height: 40,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Icon(
                Icons.arrow_back_ios_new,
                color: Colors.white,
                size: 20,
              ),
            ),
          ),
        ),
        actions: [
          // Filter toggle button
          Padding(
            padding: const EdgeInsets.only(right: 12.0),
            child: GestureDetector(
              onTap: _toggleFilter,
              child: Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: _showFilter ? const Color(0xFF7FB3D3) : const Color(0xFF34495E),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(
                  Icons.tune,
                  color: _showFilter ? Colors.black : Colors.white,
                  size: 20,
                ),
              ),
            ),
          ),
        ],
      ),
      body: Column(
        children: [
          // Filter Panel
          AnimatedBuilder(
            animation: _filterAnimation,
            builder: (context, child) {
              return ClipRect(
                child: Align(
                  alignment: Alignment.topCenter,
                  heightFactor: _filterAnimation.value,
                  child: child,
                ),
              );
            },
            // Truyền isAuthenticatedAsync vào _buildFilterPanel
            child: _buildFilterPanel(isAuthenticatedAsync),
          ),

          // Main Content
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Container(
                decoration: BoxDecoration(
                  color: Colors.black45,
                  border: Border.all(color: Colors.black26, width: 1),
                  borderRadius: BorderRadius.circular(25),
                ),
                child: _buildBody(),
              ),
            ),
          ),
        ],
      ),
      bottomNavigationBar: AppBottomNavigation(
        currentIndex: 2,
        context: context,
      ),
    );
  }

  // Cập nhật hàm _buildFilterPanel để nhận isAuthenticatedAsync
  Widget _buildFilterPanel(AsyncValue<bool> isAuthenticatedAsync) {
    return Container(
      color: const Color(0xFF34495E),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.filter_list, color: Colors.white, size: 20),
              const SizedBox(width: 8),
              const Text(
                'Filter Sections',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const Spacer(),
              if (!_currentFilter.isEmpty)
                TextButton(
                  onPressed: _clearFilter,
                  child: const Text(
                    'Clear All',
                    style: TextStyle(color: Colors.white),
                  ),
                ),
            ],
          ),

          const SizedBox(height: 16),

          // Search by lesson title
          TextField(
            controller: _lessonTitleController,
            style: const TextStyle(color: Colors.white),
            decoration: InputDecoration(
              hintText: 'Search lesson title...',
              hintStyle: TextStyle(color: Colors.grey[400]),
              prefixIcon: const Icon(Icons.search, color: Colors.grey),
              filled: true,
              fillColor: const Color(0xFF2C3E50),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
                borderSide: BorderSide.none,
              ),
              contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            ),
          ),

          const SizedBox(height: 16),

          Row(
            children: [
              // Level filter
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Level',
                      style: TextStyle(color: Colors.white70, fontSize: 12),
                    ),
                    const SizedBox(height: 4),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 12),
                      decoration: BoxDecoration(
                        color: const Color(0xFF2C3E50),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: DropdownButtonHideUnderline(
                        child: DropdownButton<String>(
                          value: _selectedLevel,
                          hint: Text(
                            'Select level',
                            style: TextStyle(color: Colors.grey[400], fontSize: 14),
                          ),
                          dropdownColor: const Color(0xFF2C3E50),
                          style: const TextStyle(color: Colors.white),
                          isExpanded: true,
                          items: [
                            const DropdownMenuItem<String>(
                              value: null,
                              child: Text('All levels'),
                            ),
                            ..._levelOptions.map((String level) {
                              return DropdownMenuItem<String>(
                                value: level,
                                child: Text(level),
                              );
                            }),
                          ],
                          onChanged: (String? value) {
                            setState(() {
                              _selectedLevel = value;
                            });
                          },
                        ),
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(width: 12),

              isAuthenticatedAsync.when(
                data: (isAuthenticated) {
                  if (!isAuthenticated) { // <<< ĐIỀU KIỆN QUAN TRỌNG: Nếu CHƯA đăng nhập
                    return const SizedBox.shrink(); // Ẩn "progress filter"
                  } else {
                    return Expanded( // ĐÃ đăng nhập, hiển thị "progress filter"
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'Progress',
                            style: TextStyle(color: Colors.white70, fontSize: 12),
                          ),
                          const SizedBox(height: 4),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 12),
                            decoration: BoxDecoration(
                              color: const Color(0xFF2C3E50),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: DropdownButtonHideUnderline(
                              child: DropdownButton<String>(
                                value: _selectedProgress,
                                hint: Text(
                                  'Select progress',
                                  style: TextStyle(color: Colors.grey[400], fontSize: 14),
                                ),
                                dropdownColor: const Color(0xFF2C3E50),
                                style: const TextStyle(color: Colors.white),
                                isExpanded: true,
                                items: [
                                  const DropdownMenuItem<String>(
                                    value: null,
                                    child: Text('All progress'),
                                  ),
                                  ..._progressOptions.map((String progress) {
                                    return DropdownMenuItem<String>(
                                      value: progress,
                                      child: Text(_progressLabels[progress] ?? progress),
                                    );
                                  }),
                                ],
                                onChanged: (String? value) {
                                  setState(() {
                                    _selectedProgress = value;
                                  });
                                },
                              ),
                            ),
                          ),
                        ],
                      ),
                    );
                  }
                },
                loading: () => const Expanded( // Hiển thị loading cho phần này
                  child: Center(
                    child: SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(strokeWidth: 2, valueColor: AlwaysStoppedAnimation<Color>(Colors.white70)),
                    ),
                  ),
                ),
                error: (error, stack) => Expanded(
                  child: Text(
                    'Error: ${error.toString()}',
                    style: const TextStyle(color: Colors.red, fontSize: 12),
                  ),
                ),
              ),
            ],
          ),

          const SizedBox(height: 16),

          // Apply button
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: _applyFilter,
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF7FB3D3),
                foregroundColor: Colors.black,
                padding: const EdgeInsets.symmetric(vertical: 12),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
              child: const Text(
                'Apply Filter',
                style: TextStyle(fontWeight: FontWeight.w600),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBody() {
    // ... (Giữ nguyên các phần còn lại của _buildBody và các hàm phụ trợ)
    if (_isLoading) {
      return const Center(
        child: CircularProgressIndicator(
          valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF011D2E)),
        ),
      );
    }

    if (_errorMessage != null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              'Error: $_errorMessage',
              style: const TextStyle(color: Colors.red, fontSize: 16),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () {
                setState(() {
                  _isLoading = true;
                  _errorMessage = null;
                });
                _loadSections();
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF7FB3D3),
                foregroundColor: const Color(0xFF2C3E50),
              ),
              child: const Text('Retry'),
            ),
          ],
        ),
      );
    }

    // Handle premium content
    if (_isPremium) {
      return _buildPremiumContent();
    }

    if (_sections.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(
              Icons.search_off,
              size: 64,
              color: Colors.white54,
            ),
            const SizedBox(height: 16),
            Text(
              _currentFilter.isEmpty ? 'No sections found' : 'No sections match your filter',
              style: const TextStyle(color: Colors.white, fontSize: 16),
            ),
            if (!_currentFilter.isEmpty) ...[
              const SizedBox(height: 8),
              TextButton(
                onPressed: _clearFilter,
                child: const Text(
                  'Clear filter',
                  style: TextStyle(color: Color(0xFF7FB3D3)),
                ),
              ),
            ],
          ],
        ),
      );
    }

    return Column(
      children: [
        // Results header
        if (!_currentFilter.isEmpty)
          Container(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Icon(
                  Icons.filter_list,
                  size: 16,
                  color: Colors.grey[400],
                ),
                const SizedBox(width: 8),
                Text(
                  'Found ${_sections.length} section${_sections.length != 1 ? 's' : ''}',
                  style: TextStyle(
                    color: Colors.grey[400],
                    fontSize: 14,
                  ),
                ),
                const Spacer(),
                TextButton(
                  onPressed: _clearFilter,
                  child: const Text(
                    'Clear filter',
                    style: TextStyle(color: Color(0xFF7FB3D3), fontSize: 12),
                  ),
                ),
              ],
            ),
          ),

        // Sections list
        Expanded(
          child: ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: _sections.length,
            itemBuilder: (context, index) {
              final section = _sections[index];
              return _buildExpandableSectionItem(section);
            },
            separatorBuilder: (context, index) {
              return const SizedBox(height: 8);
            },
          ),
        ),
      ],
    );
  }

  Widget _buildPremiumContent() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Premium icon
            Container(
              width: 80,
              height: 80,
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Colors.amber, Colors.orange],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.circular(40),
              ),
              child: const Icon(
                Icons.star,
                color: Colors.white,
                size: 40,
              ),
            ),

            const SizedBox(height: 24),

            // Premium message
            Text(
              _premiumMessage ?? 'This topic is premium, please subscribe to access it.',
              style: const TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.w600,
              ),
              textAlign: TextAlign.center,
            ),

            const SizedBox(height: 16),

            Text(
              'Unlock all premium content with a subscription',
              style: TextStyle(
                color: Colors.grey[300],
                fontSize: 14,
              ),
              textAlign: TextAlign.center,
            ),

            const SizedBox(height: 32),

            // Action buttons
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                ElevatedButton.icon(
                  onPressed: () {
                    Navigator.pushNamed(context, '/upgradePremium');
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.amber,
                    foregroundColor: Colors.black,
                    padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                  ),
                  icon: const Icon(Icons.star, size: 20),
                  label: const Text('Subscribe Now'),
                ),

                const SizedBox(width: 16),

                OutlinedButton(
                  onPressed: () {
                    setState(() {
                      _isLoading = true;
                      _isPremium = false;
                      _premiumMessage = null;
                    });
                    _loadSections();
                  },
                  style: OutlinedButton.styleFrom(
                    side: const BorderSide(color: Colors.white54),
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                  ),
                  child: const Text('Retry'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildExpandableSectionItem(Section section) {
    final sectionId = section.id;
    final isExpanded = _expandedSections[sectionId] ?? false;
    final animationController = _animationControllers[sectionId];

    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: Colors.white.withOpacity(0.1),
          width: 1,
        ),
      ),
      child: Column(
        children: [
          // Section Header
          InkWell(
            onTap: () => _toggleSection(section),
            borderRadius: BorderRadius.circular(12),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Row(
                children: [
                  // Section Icon
                  Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: const Color(0xFF7FB3D3).withOpacity(0.2),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: const Icon(
                      Icons.book_outlined,
                      color: Color(0xFF7FB3D3),
                      size: 24,
                    ),
                  ),

                  const SizedBox(width: 16),

                  // Section Info
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          section.title,
                          style: const TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                            fontSize: 16,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          'Level: ${section.level}',
                          style: TextStyle(
                            color: Colors.grey[300],
                            fontSize: 14,
                          ),
                        ),
                      ],
                    ),
                  ),

                  // Expand/Collapse Icon
                  if (animationController != null)
                    AnimatedBuilder(
                      animation: animationController,
                      builder: (context, child) {
                        return Transform.rotate(
                          angle: animationController.value * 1.5708, // 90 degrees in radians
                          child: Icon(
                            Icons.arrow_forward_ios,
                            size: 16,
                            color: Colors.white70,
                          ),
                        );
                      },
                    ),
                ],
              ),
            ),
          ),

          // Expandable Lessons Section
          if (animationController != null)
            AnimatedBuilder(
              animation: animationController,
              builder: (context, child) {
                return ClipRect(
                  child: Align(
                    alignment: Alignment.topCenter,
                    heightFactor: animationController.value,
                    child: child,
                  ),
                );
              },
              child: _buildLessonsSection(sectionId),
            ),
        ],
      ),
    );
  }

  Widget _buildLessonsSection(int sectionId) {
    final isLoading = _loadingLessons[sectionId] ?? false;
    final error = _lessonErrors[sectionId];
    final lessons = _sectionLessons[sectionId];

    return Container(
      decoration: const BoxDecoration(
        border: Border(
          top: BorderSide(
            color: Colors.white12,
            width: 1,
          ),
        ),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: _buildLessonsContent(isLoading, error, lessons, sectionId),
      ),
    );
  }

  Widget _buildLessonsContent(
      bool isLoading,
      String? error,
      List<LessonResponse>? lessons,
      int sectionId,
      ) {
    if (isLoading) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.all(16.0),
          child: CircularProgressIndicator(
            valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF7FB3D3)),
            strokeWidth: 2,
          ),
        ),
      );
    }

    if (error != null) {
      return Center(
        child: Column(
          children: [
            Text(
              'Failed to load lessons',
              style: TextStyle(
                color: Colors.red[300],
                fontSize: 14,
              ),
            ),
            const SizedBox(height: 8),
            TextButton(
              onPressed: () => _loadLessons(sectionId),
              child: const Text(
                'Retry',
                style: TextStyle(color: Color(0xFF7FB3D3)),
              ),
            ),
          ],
        ),
      );
    }

    if (lessons == null || lessons.isEmpty) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.all(16.0),
          child: Text(
            'No lessons available',
            style: TextStyle(
              color: Colors.white70,
              fontSize: 14,
            ),
          ),
        ),
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Lessons (${lessons.length})',
          style: const TextStyle(
            color: Color(0xFF7FB3D3),
            fontSize: 14,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 12),
        ...lessons.map((lesson) => _buildLessonItem(lesson)).toList(),
      ],
    );
  }

  Widget _buildLessonItem(LessonResponse lesson) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFF2C3E50),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(
          color: Colors.white.withOpacity(0.05),
          width: 1,
        ),
      ),
      child: Row(
        children: [
          // Lesson Icon
          Container(
            width: 32,
            height: 32,
            decoration: BoxDecoration(
              color: const Color(0xFF7FB3D3).withOpacity(0.2),
              borderRadius: BorderRadius.circular(6),
            ),
            child: const Icon(
              Icons.play_circle_outline,
              color: Color(0xFF7FB3D3),
              size: 18,
            ),
          ),

          const SizedBox(width: 12),

          // Lesson Info
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  lesson.title,
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  '${lesson.countChallenge} challenges',
                  style: TextStyle(
                    color: Colors.grey[400],
                    fontSize: 12,
                  ),
                ),
              ],
            ),
          ),

          // Action Button
          IconButton(
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => ChallengeScreen(
                    lessonId: lesson.id,
                    lessonTitle: lesson.title,
                  ),
                ),
              );
            },
            icon: const Icon(
              Icons.arrow_forward_ios,
              color: Colors.white54,
              size: 14,
            ),
          ),
        ],
      ),
    );
  }
}
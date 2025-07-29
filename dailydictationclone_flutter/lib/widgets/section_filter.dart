import 'package:flutter/material.dart';
import '../models/section_filter.dart';

class SectionFilterWidget extends StatefulWidget {
  final SectionFilter initialFilter;
  final Function(SectionFilter) onFilterApplied;
  final VoidCallback? onFilterCleared;
  final bool isVisible;

  const SectionFilterWidget({
    super.key,
    required this.initialFilter,
    required this.onFilterApplied,
    this.onFilterCleared,
    this.isVisible = true,
  });

  @override
  State<SectionFilterWidget> createState() => _SectionFilterWidgetState();
}

class _SectionFilterWidgetState extends State<SectionFilterWidget> {
  late TextEditingController _lessonTitleController;
  String? _selectedLevel;
  String? _selectedProgress;

  final List<String> _levelOptions = ['Beginner', 'Intermediate', 'Advanced'];
  final List<String> _progressOptions = ['NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'];
  final Map<String, String> _progressLabels = {
    'NOT_STARTED': 'Not Started',
    'IN_PROGRESS': 'In Progress',
    'COMPLETED': 'Completed',
  };

  @override
  void initState() {
    super.initState();
    _lessonTitleController = TextEditingController(text: widget.initialFilter.lessonTitle ?? '');
    _selectedLevel = widget.initialFilter.level;
    _selectedProgress = widget.initialFilter.challengeProgress;
  }

  @override
  void dispose() {
    _lessonTitleController.dispose();
    super.dispose();
  }

  void _applyFilter() {
    final filter = SectionFilter(
      level: _selectedLevel?.isNotEmpty == true ? _selectedLevel : null,
      lessonTitle: _lessonTitleController.text.isNotEmpty ? _lessonTitleController.text : null,
      challengeProgress: _selectedProgress?.isNotEmpty == true ? _selectedProgress : null,
    );

    widget.onFilterApplied(filter);
  }

  void _clearFilter() {
    setState(() {
      _selectedLevel = null;
      _selectedProgress = null;
      _lessonTitleController.clear();
    });

    if (widget.onFilterCleared != null) {
      widget.onFilterCleared!();
    }
  }

  bool get _hasActiveFilter {
    return (_selectedLevel?.isNotEmpty == true) ||
        (_lessonTitleController.text.isNotEmpty) ||
        (_selectedProgress?.isNotEmpty == true);
  }

  @override
  Widget build(BuildContext context) {
    if (!widget.isVisible) {
      return const SizedBox.shrink();
    }

    return Container(
      color: const Color(0xFF34495E),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
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
              if (_hasActiveFilter)
                TextButton(
                  onPressed: _clearFilter,
                  child: const Text(
                    'Clear All',
                    style: TextStyle(color: Color(0xFF7FB3D3)),
                  ),
                ),
            ],
          ),

          const SizedBox(height: 16),

          // Search by lesson title
          _buildSearchField(),

          const SizedBox(height: 16),

          // Filter dropdowns
          Row(
            children: [
              Expanded(child: _buildLevelDropdown()),
              const SizedBox(width: 12),
              Expanded(child: _buildProgressDropdown()),
            ],
          ),

          const SizedBox(height: 16),

          // Apply button
          _buildApplyButton(),
        ],
      ),
    );
  }

  Widget _buildSearchField() {
    return TextField(
      controller: _lessonTitleController,
      style: const TextStyle(color: Colors.white),
      decoration: InputDecoration(
        hintText: 'Search lesson title...',
        hintStyle: TextStyle(color: Colors.grey[400]),
        prefixIcon: const Icon(Icons.search, color: Colors.grey),
        suffixIcon: _lessonTitleController.text.isNotEmpty
            ? IconButton(
          icon: const Icon(Icons.clear, color: Colors.grey),
          onPressed: () {
            setState(() {
              _lessonTitleController.clear();
            });
          },
        )
            : null,
        filled: true,
        fillColor: const Color(0xFF2C3E50),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide.none,
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      ),
      onChanged: (value) {
        setState(() {}); // Rebuild to show/hide clear button
      },
    );
  }

  Widget _buildLevelDropdown() {
    return Column(
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
            border: _selectedLevel != null
                ? Border.all(color: const Color(0xFF7FB3D3), width: 1)
                : null,
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
                    child: Row(
                      children: [
                        Icon(
                          _getLevelIcon(level),
                          size: 16,
                          color: _getLevelColor(level),
                        ),
                        const SizedBox(width: 8),
                        Text(level),
                      ],
                    ),
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
    );
  }

  Widget _buildProgressDropdown() {
    return Column(
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
            border: _selectedProgress != null
                ? Border.all(color: const Color(0xFF7FB3D3), width: 1)
                : null,
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
                    child: Row(
                      children: [
                        Icon(
                          _getProgressIcon(progress),
                          size: 16,
                          color: _getProgressColor(progress),
                        ),
                        const SizedBox(width: 8),
                        Text(_progressLabels[progress] ?? progress),
                      ],
                    ),
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
    );
  }

  Widget _buildApplyButton() {
    return SizedBox(
      width: double.infinity,
      child: ElevatedButton.icon(
        onPressed: _applyFilter,
        style: ElevatedButton.styleFrom(
          backgroundColor: const Color(0xFF7FB3D3),
          foregroundColor: Colors.black,
          padding: const EdgeInsets.symmetric(vertical: 12),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
        ),
        icon: const Icon(Icons.tune, size: 18),
        label: const Text(
          'Apply Filter',
          style: TextStyle(fontWeight: FontWeight.w600),
        ),
      ),
    );
  }

  IconData _getLevelIcon(String level) {
    switch (level.toLowerCase()) {
      case 'beginner':
        return Icons.play_circle_outline;
      case 'intermediate':
        return Icons.pause_circle_outline;
      case 'advanced':
        return Icons.stop_circle_outlined;
      default:
        return Icons.circle_outlined;
    }
  }

  Color _getLevelColor(String level) {
    switch (level.toLowerCase()) {
      case 'beginner':
        return Colors.green;
      case 'intermediate':
        return Colors.orange;
      case 'advanced':
        return Colors.red;
      default:
        return Colors.grey;
    }
  }

  IconData _getProgressIcon(String progress) {
    switch (progress) {
      case 'NOT_STARTED':
        return Icons.radio_button_unchecked;
      case 'IN_PROGRESS':
        return Icons.schedule;
      case 'COMPLETED':
        return Icons.check_circle;
      default:
        return Icons.help_outline;
    }
  }

  Color _getProgressColor(String progress) {
    switch (progress) {
      case 'NOT_STARTED':
        return Colors.grey;
      case 'IN_PROGRESS':
        return Colors.orange;
      case 'COMPLETED':
        return Colors.green;
      default:
        return Colors.grey;
    }
  }
}
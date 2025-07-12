import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:audioplayers/audioplayers.dart';
import 'package:flutter/material.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

import '../locator/locator.dart';
import '../models/challenge.dart';
import '../service/challenge_service.dart';

class ChallengeScreen extends StatefulWidget {
  final int lessonId;
  final String lessonTitle;

  const ChallengeScreen({
    super.key,
    required this.lessonId,
    required this.lessonTitle,
  });

  @override
  State<ChallengeScreen> createState() => _ChallengeScreenState();
}

class _ChallengeScreenState extends State<ChallengeScreen> {

  Challenge? _currentChallenge;
  bool _isLoading = true;
  bool _isLoadingNext = false;
  bool _isLoadingPrevious = false;
  String? _errorMessage;

  Map<String, dynamic>? _lastCheckResult;
  String? _hintSentence;
  bool _showResult = false;
  late final ChallengeService _challengeService = getIt<ChallengeService>();
  late final AudioPlayer _audioPlayer = AudioPlayer();
  double _playbackSpeed = 1.0;
  final List<double> _speedOptions = [0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 3.0];

  // Audio player state
  bool _isPlaying = false;
  Duration _currentPosition = Duration.zero;
  Duration _totalDuration = Duration.zero;
  StreamSubscription? _positionSubscription;
  StreamSubscription? _durationSubscription;
  StreamSubscription? _playerStateSubscription;

  // Input state
  final TextEditingController _answerController = TextEditingController();
  bool _isCheckingAnswer = false;
  String? _checkResult;

  // --- Translation state ---
  String? _translatedAnswer;
  bool _isTranslating = false;
  String _selectedLanguage = 'vi'; // Default language

  // Language options
  final Map<String, String> _languageOptions = {
    'vi': 'Vietnamese',
    'en': 'English',
    'zh': 'Chinese',
    'ja': 'Japanese',
    'ko': 'Korean',
    'th': 'Thai',
    'es': 'Spanish',
    'fr': 'French',
    'de': 'German',
    'it': 'Italian',
    'pt': 'Portuguese',
    'ru': 'Russian',
    'ar': 'Arabic',
    'hi': 'Hindi',
  };


  @override
  void initState() {
    super.initState();
    _loadCurrentChallenge();
    _setupAudioPlayer();
  }

  @override
  void dispose() {
    _audioPlayer.stop();
    _positionSubscription?.cancel();
    _durationSubscription?.cancel();
    _playerStateSubscription?.cancel();
    _audioPlayer.dispose();
    _answerController.dispose();
    super.dispose();
  }

  void _setupAudioPlayer() {
    _positionSubscription = _audioPlayer.onPositionChanged.listen(
          (position) {
        setState(() {
          _currentPosition = position;
        });
      },
      onError: (e) => print('Position stream error: $e'),
    );

    _durationSubscription = _audioPlayer.onDurationChanged.listen(
          (duration) {
        setState(() {
          _totalDuration = duration;
        });
      },
      onError: (e) => print('Duration stream error: $e'),
    );

    _playerStateSubscription = _audioPlayer.onPlayerStateChanged.listen(
          (state) {
        print('Player state changed: $state'); // Debug log
        setState(() {
          _isPlaying = state == PlayerState.playing;

          if (state == PlayerState.completed) {
            _isPlaying = false;
          }
        });
      },
      onError: (e) => print('Player state stream error: $e'),
    );
  }

  Future<void> _loadCurrentChallenge() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final challenge = await _challengeService.getCurrentChallenge(widget.lessonId);
      setState(() {
        _currentChallenge = challenge;
        _isLoading = false;
      });

      await _loadAudioForChallenge();
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
        _isLoading = false;
      });
    }
  }

  Future<void> _loadAudioForChallenge() async {
    if (_currentChallenge == null) return;

    setState(() {
      _showResult = false;
      _lastCheckResult = null;
      _hintSentence = null;
    });

    try {
      print('Loading audio from URL: ${_currentChallenge!.audioSegmentUrl}');

      await _audioPlayer.stop();
      await _audioPlayer.setSourceUrl(_currentChallenge!.audioSegmentUrl);
      await _audioPlayer.setPlaybackRate(_playbackSpeed);

      setState(() {
        _currentPosition = Duration.zero;
        _isPlaying = false;
      });

      _answerController.clear();
      _checkResult = null;
      _translatedAnswer = null;

    } catch (e) {
      print('Error loading audio: $e');
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Failed to load audio: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  Future<void> _playPause() async {
    try {
      final currentState = _audioPlayer.state;
      print('Current state in _playPause: $currentState');

      if (currentState == PlayerState.completed) {
        await _replayFromStart();
      } else if (_isPlaying) {
        await _audioPlayer.pause();
      } else {
        await _audioPlayer.resume();
      }
    } catch (e) {
      print('Error in _playPause: $e');
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Playback error: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }
  Future<void> _replayFromStart() async {
    try {
      await _audioPlayer.stop();
      await _audioPlayer.setSourceUrl(_currentChallenge!.audioSegmentUrl);

      await _audioPlayer.setPlaybackRate(_playbackSpeed);

      await _audioPlayer.resume();

      setState(() {
        _currentPosition = Duration.zero;
        _isPlaying = true;
      });

    } catch (e) {
      print('Error replaying from start: $e');
      throw e;
    }
  }

  Future<void> _replay() async {
    try {
      final currentState = _audioPlayer.state;

      if (currentState == PlayerState.completed) {
        await _replayFromStart();
      } else {
        await _audioPlayer.seek(Duration.zero);
        if (currentState != PlayerState.playing) {
          await _audioPlayer.resume();
        }
      }

    } catch (e) {
      print('Error replaying audio: $e');
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Failed to replay audio: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  Future<void> _changePlaybackSpeed() async {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: const Color(0xFF2C3E50),
        title: const Text(
          'Playback Speed',
          style: TextStyle(color: Colors.white),
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: _speedOptions.map((speed) {
            return ListTile(
              title: Text(
                '${speed}x',
                style: TextStyle(
                  color: _playbackSpeed == speed ? const Color(0xFF7FB3D3) : Colors.white,
                  fontWeight: _playbackSpeed == speed ? FontWeight.bold : FontWeight.normal,
                ),
              ),
              leading: Radio<double>(
                value: speed,
                groupValue: _playbackSpeed,
                onChanged: (value) async {
                  if (value != null) {
                    await _setPlaybackSpeed(value);
                    Navigator.pop(context);
                  }
                },
                activeColor: const Color(0xFF7FB3D3),
              ),
              onTap: () async {
                await _setPlaybackSpeed(speed);
                Navigator.pop(context);
              },
            );
          }).toList(),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text(
              'Cancel',
              style: TextStyle(color: Color(0xFF7FB3D3)),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _setPlaybackSpeed(double speed) async {
    try {
      await _audioPlayer.setPlaybackRate(speed);
      setState(() {
        _playbackSpeed = speed;
      });
    } catch (e) {
      print('Error setting playback speed: $e');
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Failed to set playback speed: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  List<String> _parseUserInput(String input) {
    // Remove extra spaces and split by spaces
    return input.trim()
        .replaceAll(RegExp(r'\s+'), ' ')
        .split(' ')
        .where((word) => word.isNotEmpty)
        .toList();
  }

  Future<void> _checkAnswer() async {
    if (_answerController.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Please type your answer first'),
          backgroundColor: Colors.orange,
        ),
      );
      return;
    }

    if (_currentChallenge == null) return;

    setState(() {
      _isCheckingAnswer = true;
      _showResult = false;
    });

    try {
      final userAnswer = _parseUserInput(_answerController.text);

      final result = await _challengeService.checkChallenge(
        _currentChallenge!.orderIndex,
        widget.lessonId,
        userAnswer,
      );

      final hintSentence = _challengeService.buildHintSentence(
          result['wordResults'] as List<dynamic>);

      setState(() {
        _isCheckingAnswer = false;
        _lastCheckResult = result;
        _hintSentence = hintSentence;
        _showResult = true;
      });

    } catch (e) {
      setState(() {
        _isCheckingAnswer = false;
      });

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Failed to check answer: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  Future<void> _previousChallenge() async {
    if (_currentChallenge == null) return;

    setState(() {
      _isLoadingPrevious = true;
    });

    try {
      final previousChallenge = await _challengeService.getPreviousChallenge(
        widget.lessonId,
        _currentChallenge!.orderIndex,
      );

      setState(() {
        _currentChallenge = previousChallenge;
        _isLoadingPrevious = false;
      });

      await _loadAudioForChallenge();
    } catch (e) {
      setState(() {
        _isLoadingPrevious = false;
      });

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('No previous challenge available'),
          backgroundColor: Colors.orange,
          duration: Duration(seconds: 2),
        ),
      );
    }
  }

  Future<void> _nextChallengeManual() async {
    if (_currentChallenge == null) return;

    setState(() {
      _isLoadingNext = true;
    });

    try {
      final nextChallenge = await _challengeService.getNextChallenge(
        widget.lessonId,
        _currentChallenge!.orderIndex,
      );

      setState(() {
        _currentChallenge = nextChallenge;
        _isLoadingNext = false;
      });

      await _loadAudioForChallenge();
    } catch (e) {
      setState(() {
        _isLoadingNext = false;
      });

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('No next challenge available'),
          backgroundColor: Colors.orange,
          duration: Duration(seconds: 2),
        ),
      );
    }
  }

  Future<void> _nextChallenge() async {
    if (_currentChallenge == null) return;

    setState(() {
      _isLoadingNext = true;
    });

    try {
      final nextChallenge = await _challengeService.getNextChallenge(
        widget.lessonId,
        _currentChallenge!.orderIndex,
      );

      setState(() {
        _currentChallenge = nextChallenge;
        _isLoadingNext = false;
      });

      await _loadAudioForChallenge();
    } catch (e) {
      setState(() {
        _isLoadingNext = false;
      });

      // Lesson completed
      _showCompletionDialog();
    }
  }

  void _handleResultAction() {
    if (_lastCheckResult?['isCorrect'] == true) {
      _nextChallenge();
    } else {
      setState(() {
        _showResult = false; // Ẩn kết quả khi nhấn OK
      });
    }
  }
  void _showCompletionDialog() {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        backgroundColor: const Color(0xFF2C3E50),
        title: const Text(
          'Lesson Completed!',
          style: TextStyle(
            color: Colors.green,
            fontWeight: FontWeight.bold,
          ),
        ),
        content: const Text(
          'Congratulations! You have completed all challenges in this lesson.',
          style: TextStyle(color: Colors.white),
        ),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.pop(context); // Close dialog
              Navigator.pop(context); // Go back to previous screen
            },
            child: const Text(
              'Back to Lessons',
              style: TextStyle(color: Color(0xFF7FB3D3)),
            ),
          ),
        ],
      ),
    );
  }

  void _skipChallenge() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: const Color(0xFF2C3E50),
        title: const Text(
          'Skip Challenge',
          style: TextStyle(color: Colors.white),
        ),
        content: const Text(
          'Are you sure you want to skip this challenge?',
          style: TextStyle(color: Color(0xFFBDC3C7)),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text(
              'Cancel',
              style: TextStyle(color: Color(0xFFBDC3C7)),
            ),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              _nextChallenge();
            },
            child: const Text(
              'Skip',
              style: TextStyle(color: Colors.orange),
            ),
          ),
        ],
      ),
    );
  }

  String _formatDuration(Duration duration) {
    String twoDigits(int n) => n.toString().padLeft(2, '0');
    final minutes = twoDigits(duration.inMinutes);
    final seconds = twoDigits(duration.inSeconds.remainder(60));
    return '$minutes:$seconds';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF2C3E50),
      body: SafeArea(
        child: _isLoading ? _buildLoadingState() : _buildMainContent(),
      ),
    );
  }

  Widget _buildLoadingState() {
    return const Center(
      child: CircularProgressIndicator(
        valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF7FB3D3)),
      ),
    );
  }

  Widget _buildMainContent() {
    if (_errorMessage != null) {
      return _buildErrorState();
    }

    if (_currentChallenge == null) {
      return _buildEmptyState();
    }

    return Column(
      children: [
        _buildHeader(),
        Expanded(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              children: [
                _buildChallengeInfo(),
                const SizedBox(height: 10),
                _buildAudioPlayer(),
                const SizedBox(height: 20),
                _buildAnswerInput(),
                const SizedBox(height: 20),
                _buildActionButtons(),
                if (_showResult) _buildResultDisplay(),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildResultDisplay() {
    final isCorrect = _lastCheckResult?['allCorrect'] ?? false;
    final hasMistake = !isCorrect;
    final correctAnswer = _lastCheckResult?['fullSentence'] as String?;

    return Padding(
      padding: const EdgeInsets.only(top: 20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            isCorrect ? 'Correct!' : 'Incorrect!',
            style: TextStyle(
              color: isCorrect ? Colors.green : Colors.orange,
              fontWeight: FontWeight.bold,
              fontSize: 20,
            ),
          ),

          if (_lastCheckResult?['message'] != null)
            Text(
              _lastCheckResult!['message'],
              style: const TextStyle(color: Colors.white),
            ),

          if (hasMistake) ...[
            const SizedBox(height: 10),
            const Text(
              'Try this:',
              style: TextStyle(
                color: Color(0xFF7FB3D3),
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 5),
            Text(
              _hintSentence!,
              style: const TextStyle(
                color: Colors.orange,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],

          if (isCorrect && correctAnswer != null) ...[
            const SizedBox(height: 10),
            Row(
              children: [
                const Text(
                  'Full Correct Answer:',
                  style: TextStyle(
                    color: Color(0xFF7FB3D3),
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(width: 8),
                // Language selector button
                GestureDetector(
                  onTap: _showLanguageSelectionDialog,
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: const Color(0xFF34495E),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(
                        color: const Color(0xFF7FB3D3).withOpacity(0.5),
                        width: 1,
                      ),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          Icons.language,
                          color: const Color(0xFF7FB3D3),
                          size: 16,
                        ),
                        const SizedBox(width: 4),
                        Text(
                          _languageOptions[_selectedLanguage] ?? 'Unknown',
                          style: const TextStyle(
                            color: Color(0xFF7FB3D3),
                            fontSize: 12,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                // Translate button
                IconButton(
                  icon: const Icon(Icons.g_translate, color: Color(0xFF7FB3D3)),
                  tooltip: 'Dịch sang ${_languageOptions[_selectedLanguage]}',
                  onPressed: _isTranslating ? null : () {
                    if (correctAnswer.isNotEmpty) {
                      _translateCorrectAnswer(correctAnswer);
                    }
                  },
                ),
              ],
            ),
            const SizedBox(height: 5),
            Text(
              correctAnswer,
              style: const TextStyle(color: Colors.white),
            ),
            if (_translatedAnswer != null || _isTranslating) ...[
              const SizedBox(height: 10),
              Row(
                children: [
                  Text(
                    'Bản dịch (${_languageOptions[_selectedLanguage]}):',
                    style: const TextStyle(
                      color: Color(0xFF7FB3D3),
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  if (_isTranslating) ...[
                    const SizedBox(width: 8),
                    const SizedBox(
                      width: 16,
                      height: 16,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF7FB3D3)),
                      ),
                    ),
                  ],
                ],
              ),
              const SizedBox(height: 5),
              if (_translatedAnswer != null)
                Text(
                  _translatedAnswer!,
                  style: const TextStyle(color: Colors.white),
                ),
            ],
          ],

          const SizedBox(height: 20),
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              if (hasMistake)
                TextButton(
                  onPressed: () {
                    setState(() {
                      _showResult = false;
                    });
                  },
                  child: const Text(
                    'Try Again',
                    style: TextStyle(color: Color(0xFF7FB3D3)),
                  ),
                ),
              const SizedBox(width: 10),
              TextButton(
                onPressed: _handleResultAction,
                child: Text(
                  isCorrect ? 'Next Challenge' : 'OK',
                  style: const TextStyle(color: Color(0xFF7FB3D3)),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
  Widget _buildErrorState() {
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
            onPressed: _loadCurrentChallenge,
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

  Widget _buildEmptyState() {
    return const Center(
      child: Text(
        'No challenge found for this lesson',
        style: TextStyle(color: Colors.white, fontSize: 16),
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.all(20),
      child: Row(
        children: [
          GestureDetector(
            onTap: () => Navigator.pop(context),
            child: Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: const Color(0xFF34495E),
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Icon(
                Icons.arrow_back_ios_new,
                color: Colors.white,
                size: 20,
              ),
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Text(
              'Lesson name: ${widget.lessonTitle}',
              style: const TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildChallengeInfo() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          'Challenge ${_currentChallenge?.orderIndex ?? 0}:',
          style: const TextStyle(
            color: Color(0xFFD1D1D1),
            fontSize: 20,
            fontWeight: FontWeight.w600,
          ),
        ),
        // Speed Control Button
        GestureDetector(
          onTap: _changePlaybackSpeed,
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
              color: const Color(0xFF34495E),
              borderRadius: BorderRadius.circular(20),
              border: Border.all(
                color: const Color(0xFF7FB3D3).withOpacity(0.3),
                width: 1,
              ),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(
                  Icons.speed,
                  color: const Color(0xFF7FB3D3),
                  size: 16,
                ),
                const SizedBox(width: 4),
                Text(
                  '${_playbackSpeed}x',
                  style: const TextStyle(
                    color: Color(0xFF7FB3D3),
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildAudioPlayer() {
    return Column(
      children: [
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: Column(
            children: [
              SliderTheme(
                data: SliderTheme.of(context).copyWith(
                  activeTrackColor: const Color(0xFF7FB3D3),
                  inactiveTrackColor: Colors.white.withOpacity(0.3),
                  thumbColor: const Color(0xFF7FB3D3),
                  overlayColor: const Color(0xFF7FB3D3).withOpacity(0.2),
                  thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 8),
                  trackHeight: 4,
                ),
                child: Slider(
                  value: _totalDuration.inMilliseconds > 0
                      ? _currentPosition.inMilliseconds / _totalDuration.inMilliseconds
                      : 0.0,
                  onChanged: (value) {
                    final position = Duration(
                      milliseconds: (value * _totalDuration.inMilliseconds).round(),
                    );
                    _audioPlayer.seek(position);
                  },
                ),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 5),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      _formatDuration(_currentPosition),
                      style: const TextStyle(color: Colors.white, fontSize: 14),
                    ),
                    Text(
                      _formatDuration(_totalDuration),
                      style: const TextStyle(color: Colors.white, fontSize: 14),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),

        // Audio Controls với loading state
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Previous Challenge Button
            _isLoadingPrevious
                ? const SizedBox(
              width: 48,
              height: 48,
              child: Center(
                child: SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                  ),
                ),
              ),
            )
                : IconButton(
              onPressed: _previousChallenge,
              icon: const Icon(
                Icons.skip_previous,
                color: Colors.white,
                size: 32,
              ),
            ),

            const SizedBox(width: 20),

            GestureDetector(
              onTap: _playPause,
              child: Container(
                width: 64,
                height: 64,
                decoration: const BoxDecoration(
                  color: Colors.white,
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  (_audioPlayer.state == PlayerState.completed || !_isPlaying)
                      ? Icons.play_arrow
                      : Icons.pause,
                  color: const Color(0xFF2C3E50),
                  size: 32,
                ),
              ),
            ),

            const SizedBox(width: 20),

            // Next Challenge Button
            _isLoadingNext
                ? const SizedBox(
              width: 48,
              height: 48,
              child: Center(
                child: SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                  ),
                ),
              ),
            )
                : IconButton(
              onPressed: _nextChallengeManual,
              icon: const Icon(
                Icons.skip_next,
                color: Colors.white,
                size: 32,
              ),
            ),

            const SizedBox(width: 20),
          ],
        ),

        const SizedBox(height: 10),

        Column(
          children: [
            Text(
              'Challenge ${_currentChallenge?.orderIndex ?? 0}',
              style: TextStyle(
                color: Colors.white.withOpacity(0.7),
                fontSize: 12,
              ),
            ),
            if (_audioPlayer.state == PlayerState.completed)
              Text(
                'Audio completed - Tap play to replay',
                style: TextStyle(
                  color: Colors.orange.withOpacity(0.8),
                  fontSize: 10,
                  fontStyle: FontStyle.italic,
                ),
              ),
          ],
        ),
      ],
    );
  }
  Widget _buildAnswerInput() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      child: TextField(
        controller: _answerController,
        style: const TextStyle(color: Colors.white, fontSize: 16),
        decoration: InputDecoration(
          hintText: 'Type what you hear ...',
          hintStyle: TextStyle(
            color: Colors.white.withOpacity(0.6),
            fontSize: 16,
          ),
          filled: true,
          fillColor: const Color(0xFF34495E),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: BorderSide.none,
          ),
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 20,
            vertical: 16,
          ),
        ),
        maxLines: 3,
        minLines: 1,
      ),
    );
  }

  Widget _buildActionButtons() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        // Skip Button
        _buildActionButton(
          'Skip',
          Colors.grey[600]!,
          _skipChallenge,
        ),

        // Replay Button
        _buildActionButton(
          'Replay',
          const Color(0xFF7FB3D3),
          _replay,
        ),

        // Check Button
        _buildActionButton(
          'Check',
          const Color(0xFF7FB3D3),
          _isCheckingAnswer ? null : _checkAnswer,
          isLoading: _isCheckingAnswer,
        ),
      ],
    );
  }

  Widget _buildActionButton(
      String text,
      Color color,
      VoidCallback? onPressed, {
        bool isLoading = false,
      }) {
    return SizedBox(
      width: 100,
      height: 40,
      child: ElevatedButton(
        onPressed: onPressed,
        style: ElevatedButton.styleFrom(
          backgroundColor: color,
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
          elevation: 0,
        ),
        child: isLoading
            ? const SizedBox(
          width: 16,
          height: 16,
          child: CircularProgressIndicator(
            strokeWidth: 2,
            valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
          ),
        )
            : Text(
          text,
          style: const TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }

  Future<void> _translateCorrectAnswer(String text) async {
    setState(() {
      _isTranslating = true;
      _translatedAnswer = null;
    });

    try {
      const String apiKey = String.fromEnvironment('TRANSLATE_API_KEY');
      print('api: $apiKey');

      // Sử dụng HTTP POST request với API key trong header
      final client = HttpClient();
      final request = await client.postUrl(
        Uri.parse('https://translation.googleapis.com/language/translate/v2'),
      );

      // Set headers
      request.headers.set('Content-Type', 'application/json');
      request.headers.set('X-Goog-Api-Key', apiKey);

      // Tạo request body với ngôn ngữ đã chọn
      final requestBody = jsonEncode({
        'q': text,
        'target': _selectedLanguage,
        'source': 'en',
      });

      // Gửi request body
      request.add(utf8.encode(requestBody));

      final response = await request.close();
      final resBody = await response.transform(const Utf8Decoder()).join();

      print('Response status: ${response.statusCode}');
      print('Response body: $resBody');

      if (response.statusCode == 200) {
        final data = jsonDecode(resBody);

        if (data['data'] != null && data['data']['translations'] != null) {
          final translated = data['data']['translations'][0]['translatedText'] as String?;
          setState(() {
            _translatedAnswer = translated;
          });
        } else {
          print('Error: Invalid response structure');
          setState(() {
            _translatedAnswer = 'Lỗi dịch: Invalid response structure';
          });
        }
      } else {
        setState(() {
          _translatedAnswer = 'Lỗi dịch: HTTP ${response.statusCode}';
        });
      }

    } catch (e) {
      print('Translation error: $e');
      setState(() {
        _translatedAnswer = 'Lỗi dịch: $e';
      });
    } finally {
      setState(() {
        _isTranslating = false;
      });
    }
  }
  void _showLanguageSelectionDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: const Color(0xFF2C3E50),
        title: const Text(
          'Chọn ngôn ngữ dịch',
          style: TextStyle(color: Colors.white),
        ),
        content: SizedBox(
          width: double.maxFinite,
          child: ListView.builder(
            shrinkWrap: true,
            itemCount: _languageOptions.length,
            itemBuilder: (context, index) {
              final languageCode = _languageOptions.keys.elementAt(index);
              final languageName = _languageOptions[languageCode]!;

              return ListTile(
                title: Text(
                  languageName,
                  style: TextStyle(
                    color: _selectedLanguage == languageCode
                        ? const Color(0xFF7FB3D3)
                        : Colors.white,
                    fontWeight: _selectedLanguage == languageCode
                        ? FontWeight.bold
                        : FontWeight.normal,
                  ),
                ),
                leading: Radio<String>(
                  value: languageCode,
                  groupValue: _selectedLanguage,
                  onChanged: (value) {
                    if (value != null) {
                      setState(() {
                        _selectedLanguage = value;
                        _translatedAnswer = null; // Clear previous translation
                      });
                      Navigator.pop(context);
                    }
                  },
                  activeColor: const Color(0xFF7FB3D3),
                ),
                onTap: () {
                  setState(() {
                    _selectedLanguage = languageCode;
                    _translatedAnswer = null; // Clear previous translation
                  });
                  Navigator.pop(context);
                },
              );
            },
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text(
              'Đóng',
              style: TextStyle(color: Color(0xFF7FB3D3)),
            ),
          ),
        ],
      ),
    );
  }
}

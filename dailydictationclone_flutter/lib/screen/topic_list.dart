import 'package:dailydictationclone_flutter/screen/section_list.dart';
import 'package:flutter/material.dart';
import '../models/topic.dart';
import '../nav/app_bottom_navigation.dart';
import '../service/topic_service.dart';

class TopicListPage extends StatefulWidget {
  const TopicListPage({super.key});

  @override
  State<TopicListPage> createState() => _TopicListPageState();
}

class _TopicListPageState extends State<TopicListPage> {
  int _currentNavIndex = 1;
  final TopicService _topicService = TopicService();
  List<Topic> _topics = [];
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadTopics();
  }

  Future<void> _loadTopics() async {
    try {
      final topics = await _topicService.getTopics();
      setState(() {
        _topics = topics;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF2C3E50),
      appBar: AppBar(
        backgroundColor: const Color(0xFF2C3E50),
        automaticallyImplyLeading: true,
        leading: Padding(
          padding: const EdgeInsets.only(left: 12.0),
          child: GestureDetector(
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
        ),
        title: const Text(
          'Topics',
          style: TextStyle(
            fontSize: 24,
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
      ),
      body:
      Padding(
        padding: const EdgeInsets.all(16.0),
        child: Container(
          decoration: BoxDecoration(
            color: Colors.black45,
            border: Border.all(color: Colors.black26, width:1 ),
            borderRadius: BorderRadius.circular(25),

          ),
          child: _buildBody(),
        ),
      ),
      bottomNavigationBar: AppBottomNavigation(
        currentIndex: _currentNavIndex,
        context: context,
      ),
    );
  }

  Widget _buildBody() {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_errorMessage != null) {
      return Center(child: Text('Error: $_errorMessage'));
    }

    if (_topics.isEmpty) {
      return const Center(child: Text('No topics found'));
    }

    return ListView.separated(
      padding: EdgeInsets.zero,
      itemCount: _topics.length,
      itemBuilder: (context, index) {
        final topic = _topics[index];
        return _buildTopicItem(topic);
      },
      separatorBuilder: (context, index) {
        return const Divider(
          height: 1,
          thickness: 0.5,
          color: Colors.white24,
          indent: 10,
          endIndent: 10,
        );
      },
    );
  }

  Widget _buildTopicItem(Topic topic) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: ListTile(
        leading: topic.image.isNotEmpty
            ? Image.network(topic.image, width: 50, height: 50)
            : const Icon(Icons.book, size: 40),
        title: Row(
          children: [
            Expanded(
              child: Text(
                topic.title,
                style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.white),
              ),
            ),
            // Hiển thị badge premium nếu premiumTopic = 1
            if (topic.premiumTopic == 1)
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [Colors.amber, Colors.orange],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(
                      Icons.star,
                      color: Colors.white,
                      size: 16,
                    ),
                    SizedBox(width: 4),
                    Text(
                      'Premium',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              ),
          ],
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Level: ${topic.levels}',
                style: TextStyle(color: Colors.grey[500])),
            Text('${topic.categoryTitle}',
                style: TextStyle(color: Colors.grey[500])),
          ],
        ),
        // Thêm trailing icon premium nếu cần
        trailing: topic.premiumTopic == 1
            ? const Icon(
          Icons.lock,
          color: Colors.amber,
          size: 20,
        )
            : const Icon(
          Icons.arrow_forward_ios,
          color: Colors.grey,
          size: 16,
        ),
        onTap: () {
          _navigateToSections(topic);
        },
      ),
    );
  }

  void _navigateToSections(Topic topic) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => SectionListPage(
          topicId: topic.id,
          topicTitle: topic.title,
        ),
      ),
    );
  }
}
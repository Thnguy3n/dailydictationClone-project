import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import '../models/bankApp.dart';
import '../service/payment_service.dart';

class BankSelectionWidget extends StatefulWidget {
  final Function(BankApp)? onBankSelected;

  const BankSelectionWidget({
    Key? key,
    this.onBankSelected,
  }) : super(key: key);

  @override
  State<BankSelectionWidget> createState() => _BankSelectionWidgetState();
}

class _BankSelectionWidgetState extends State<BankSelectionWidget> {
  List<BankApp> _bankApps = [];
  List<BankApp> _filteredBankApps = [];
  bool _isLoading = false;
  String _errorMessage = '';
  final TextEditingController _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadBankApps();
    _searchController.addListener(_filterBankApps);
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _loadBankApps() async {
    setState(() {
      _isLoading = true;
      _errorMessage = '';
    });

    try {
      final bankApps = await PaymentService.getPopularBankApps(limit: 20);
      setState(() {
        _bankApps = bankApps;
        _filteredBankApps = bankApps;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
        _isLoading = false;
      });
    }
  }

  void _filterBankApps() {
    final query = _searchController.text;
    setState(() {
      if (query.isEmpty) {
        _filteredBankApps = _bankApps;
      } else {
        _filteredBankApps = _bankApps.where((bank) {
          final searchQuery = query.toLowerCase();
          return bank.appName.toLowerCase().contains(searchQuery) ||
              bank.bankName.toLowerCase().contains(searchQuery);
        }).toList();
      }
    });
  }

  Future<void> _openBankApp(BankApp bankApp) async {
    try {
      String deeplink = bankApp.deeplink;
      final uri = Uri.parse(deeplink);

      print('Attempting to launch: $deeplink');

      try {
        if (await canLaunchUrl(uri)) {
          await launchUrl(
            uri,
            mode: LaunchMode.externalNonBrowserApplication,
          );

          if (widget.onBankSelected != null) {
            widget.onBankSelected!(bankApp);
          }
          return;
        }
      } catch (e) {
        print('External app launch failed: $e');
      }

      // Fallback: Mở trong browser
      try {
        await launchUrl(
          uri,
          mode: LaunchMode.externalApplication,
        );

        if (widget.onBankSelected != null) {
          widget.onBankSelected!(bankApp);
        }
      } catch (e) {
        print('Browser launch failed: $e');
        _showErrorSnackBar('Không thể mở ứng dụng ${bankApp.appName}');
      }

    } catch (e) {
      print('Error launching URL: $e');
      _showErrorSnackBar('Lỗi khi mở ứng dụng: $e');
    }
  }

  void _showErrorSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.red,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFF2D2D2D),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Row(
            children: [
              const Icon(
                Icons.account_balance,
                color: Color(0xFF4CAF50),
                size: 24,
              ),
              const SizedBox(width: 12),
              const Text(
                'Choose Your Bank',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),

          const SizedBox(height: 16),

          // Search bar
          TextField(
            controller: _searchController,
            style: const TextStyle(color: Colors.white),
            decoration: InputDecoration(
              hintText: 'Search for a bank...',
              hintStyle: const TextStyle(color: Colors.white60),
              prefixIcon: const Icon(Icons.search, color: Colors.white60),
              filled: true,
              fillColor: const Color(0xFF1E1E1E),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
                borderSide: BorderSide.none,
              ),
            ),
          ),

          const SizedBox(height: 16),

          // Content
          if (_isLoading)
            const Center(
              child: CircularProgressIndicator(
                color: Color(0xFF4CAF50),
              ),
            )
          else if (_errorMessage.isNotEmpty)
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.red.withOpacity(0.1),
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.red.withOpacity(0.3)),
              ),
              child: Column(
                children: [
                  Text(
                    _errorMessage,
                    style: const TextStyle(color: Colors.red),
                  ),
                  const SizedBox(height: 8),
                  ElevatedButton(
                    onPressed: _loadBankApps,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF4CAF50),
                    ),
                    child: const Text('Thử lại'),
                  ),
                ],
              ),
            )
          else
            SizedBox(
              height: 300,
              child: ListView.builder(
                itemCount: _filteredBankApps.length,
                itemBuilder: (context, index) {
                  final bank = _filteredBankApps[index];
                  return _buildBankItem(bank);
                },
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildBankItem(BankApp bank) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      child: Material(
        color: const Color(0xFF1E1E1E),
        borderRadius: BorderRadius.circular(8),
        child: InkWell(
          onTap: () => _openBankApp(bank),
          borderRadius: BorderRadius.circular(8),
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: Row(
              children: [
                // Logo
                ClipRRect(
                  borderRadius: BorderRadius.circular(8),
                  child: Image.network(
                    bank.appLogo,
                    width: 40,
                    height: 40,
                    fit: BoxFit.cover,
                    errorBuilder: (context, error, stackTrace) {
                      return Container(
                        width: 40,
                        height: 40,
                        decoration: BoxDecoration(
                          color: Colors.grey[300],
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: const Icon(
                          Icons.account_balance,
                          color: Colors.grey,
                          size: 20,
                        ),
                      );
                    },
                  ),
                ),

                const SizedBox(width: 12),

                // Info
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        bank.appName,
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 16,
                          fontWeight: FontWeight.w600,
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                      const SizedBox(height: 4),
                      Text(
                        bank.bankName,
                        style: const TextStyle(
                          color: Colors.white70,
                          fontSize: 12,
                        ),
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ),
                ),

                const SizedBox(width: 8),

                // Badges
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    if (bank.isAutofillSupported)
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 6,
                          vertical: 2,
                        ),
                        decoration: BoxDecoration(
                          color: const Color(0xFF4CAF50).withOpacity(0.2),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: const Text(
                          'AUTO',
                          style: TextStyle(
                            color: Color(0xFF4CAF50),
                            fontSize: 10,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),

                  ],
                ),

                const SizedBox(width: 8),

                const Icon(
                  Icons.arrow_forward_ios,
                  color: Colors.white60,
                  size: 16,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
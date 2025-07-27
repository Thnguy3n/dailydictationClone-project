import 'package:flutter/material.dart';
import '../models/premium_package.dart';
import '../service/payment_service.dart';
import '../widgets/package_card.dart';
import '../widgets/payment_dialog.dart';

class UpgradePremiumScreen extends StatefulWidget {
  const UpgradePremiumScreen({Key? key}) : super(key: key);

  @override
  State<UpgradePremiumScreen> createState() => _UpgradePremiumScreenState();
}

class _UpgradePremiumScreenState extends State<UpgradePremiumScreen>
    with TickerProviderStateMixin {

  List<PremiumPackage> _packages = [];
  bool _isLoading = true;
  String? _errorMessage;
  bool _isProcessingPayment = false;

  late AnimationController _headerController;
  late Animation<double> _headerFadeAnimation;
  late Animation<Offset> _headerSlideAnimation;

  @override
  void initState() {
    super.initState();
    _initializeAnimations();
    _fetchPackages();
  }

  void _initializeAnimations() {
    _headerController = AnimationController(
      duration: const Duration(milliseconds: 800),
      vsync: this,
    );

    _headerFadeAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _headerController,
      curve: Curves.easeOut,
    ));

    _headerSlideAnimation = Tween<Offset>(
      begin: const Offset(0, -0.5),
      end: Offset.zero,
    ).animate(CurvedAnimation(
      parent: _headerController,
      curve: Curves.easeOutCubic,
    ));
  }

  Future<void> _fetchPackages() async {
    try {
      setState(() {
        _isLoading = true;
        _errorMessage = null;
      });

      final packages = await PaymentService.fetchPremiumPackages();

      setState(() {
        _packages = packages;
        _isLoading = false;
      });

      _headerController.forward();

    } catch (e) {
      setState(() {
        _isLoading = false;
        _errorMessage = e.toString();
      });
    }
  }

  Future<void> _onPackageTap(PremiumPackage package) async {
    if (_isProcessingPayment) return;

    try {
      setState(() {
        _isProcessingPayment = true;
      });

      // Show loading dialog
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) => const Center(
          child: CircularProgressIndicator(
            valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF4CAF50)),
          ),
        ),
      );

      final purchaseResponse = await PaymentService.purchasePremium(
        package.id,
      );

      final qrResponse = await PaymentService.generateQrCode(
        purchaseResponse.purchaseId,
      );

      Navigator.of(context).pop();

      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) => PaymentDialog(
          qrDataURL: qrResponse.data.qrDataURL,
          packageName: package.name,
          price: package.price,
          expireAt: qrResponse.expireAt,
          qrTransactionId: qrResponse.id,
          addInfo: qrResponse.addInfo,
        ),
      );

    } catch (e) {
      Navigator.of(context).pop();

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Lỗi: ${e.toString()}'),
          backgroundColor: Colors.red,
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
        ),
      );
    } finally {
      setState(() {
        _isProcessingPayment = false;
      });
    }
  }

  @override
  void dispose() {
    _headerController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      // Dark background with subtle gradient
      backgroundColor: const Color(0xFF1C2937),
      appBar: AppBar(
        title: const Text(
          'Premium Packages',
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
        backgroundColor: const Color(0xFF1C2937),
        foregroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
        // Add subtle border at bottom
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(1),
          child: Container(
            height: 1,
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [
                  Colors.transparent,
                  Colors.white.withOpacity(0.1),
                  Colors.transparent,
                ],
              ),
            ),
          ),
        ),
      ),
      body: Container(
        // Add subtle gradient background
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              Color(0xFF1C2937),
              Color(0xFF2C3E50),
            ],
          ),
        ),
        child: _buildBody(),
      ),
    );
  }

  Widget _buildBody() {
    if (_isLoading) {
      return _buildLoadingState();
    }

    if (_errorMessage != null) {
      return _buildErrorState();
    }

    if (_packages.isEmpty) {
      return _buildEmptyState();
    }

    return _buildSuccessState();
  }

  Widget _buildLoadingState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Container(
            width: 80,
            height: 80,
            decoration: BoxDecoration(
              color: const Color(0xFF64B5F6).withOpacity(0.2),
              borderRadius: BorderRadius.circular(40),
              border: Border.all(
                color: const Color(0xFF64B5F6).withOpacity(0.3),
                width: 1,
              ),
            ),
            child: const Center(
              child: CircularProgressIndicator(
                strokeWidth: 3,
                valueColor: AlwaysStoppedAnimation<Color>(
                  Color(0xFF64B5F6),
                ),
              ),
            ),
          ),
          const SizedBox(height: 24),
          const Text(
            'Loading Premium Packages...',
            style: TextStyle(
              fontSize: 16,
              color: Colors.white70,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorState() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: Colors.red.withOpacity(0.1),
                borderRadius: BorderRadius.circular(40),
                border: Border.all(
                  color: Colors.red.withOpacity(0.3),
                  width: 1,
                ),
              ),
              child: const Icon(
                Icons.error_outline,
                size: 60,
                color: Colors.red,
              ),
            ),
            const SizedBox(height: 24),
            const Text(
              'Oops! Something went wrong',
              style: TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 12),
            Text(
              _errorMessage ?? 'Unknown error occurred',
              style: const TextStyle(
                fontSize: 14,
                color: Colors.white60,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 32),
            ElevatedButton.icon(
              onPressed: _fetchPackages,
              icon: const Icon(Icons.refresh, color: Colors.white),
              label: const Text(
                'Try Again',
                style: TextStyle(color: Colors.white),
              ),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF64B5F6),
                padding: const EdgeInsets.symmetric(
                  horizontal: 32,
                  vertical: 16,
                ),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(25),
                ),
                elevation: 4,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.05),
                borderRadius: BorderRadius.circular(40),
                border: Border.all(
                  color: Colors.white.withOpacity(0.1),
                  width: 1,
                ),
              ),
              child: Icon(
                Icons.inbox_outlined,
                size: 60,
                color: Colors.white.withOpacity(0.6),
              ),
            ),
            const SizedBox(height: 24),
            const Text(
              'No Premium Packages Available',
              style: TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 12),
            const Text(
              'Check back later for exciting premium offers!',
              style: TextStyle(
                fontSize: 14,
                color: Colors.white60,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSuccessState() {
    return CustomScrollView(
      physics: const BouncingScrollPhysics(),
      slivers: [
        // Animated header
        SliverToBoxAdapter(
          child: FadeTransition(
            opacity: _headerFadeAnimation,
            child: SlideTransition(
              position: _headerSlideAnimation,
              child: Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 24,
                  vertical: 10,
                ),
                child: const Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Choose Your Plan',
                      style: TextStyle(
                        fontSize: 28,
                        fontWeight: FontWeight.bold,
                        color:  Colors.white,
                      ),
                    ),
                    SizedBox(height: 8),
                    Text(
                      'Unlock premium features and enhance your experience',
                      style: TextStyle(
                        fontSize: 16,
                        color: Colors.white70,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),

        // Package list with staggered animations
        SliverList(
          delegate: SliverChildBuilderDelegate(
                (context, index) {
              return PackageCard(
                package: _packages[index],
                index: index,
                onTap: () => _onPackageTap(_packages[index]),
              );
            },
            childCount: _packages.length,
          ),
        ),

        // Bottom spacing
        const SliverToBoxAdapter(
          child: SizedBox(height: 32),
        ),
      ],
    );
  }
}

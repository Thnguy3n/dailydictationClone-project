import 'package:flutter/material.dart';
import '../models/premium_package.dart';
import 'animated_price_counter.dart';
import 'sale_badge.dart';

/// Individual package card with animations and pricing display
class PackageCard extends StatefulWidget {
  final PremiumPackage package;
  final int index;
  final VoidCallback? onTap;

  const PackageCard({
    Key? key,
    required this.package,
    required this.index,
    this.onTap,
  }) : super(key: key);

  @override
  State<PackageCard> createState() => _PackageCardState();
}

class _PackageCardState extends State<PackageCard>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _fadeAnimation;
  late Animation<Offset> _slideAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 600),
      vsync: this,
    );

    _fadeAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _controller,
      curve: Curves.easeOut,
    ));

    _slideAnimation = Tween<Offset>(
      begin: const Offset(0, 0.3),
      end: Offset.zero,
    ).animate(CurvedAnimation(
      parent: _controller,
      curve: Curves.easeOutCubic,
    ));

    // Staggered animation based on index
    Future.delayed(Duration(milliseconds: widget.index * 150), () {
      if (mounted) {
        _controller.forward();
      }
    });
  }
  String _formatPrice(double price) {
    if (price == price.roundToDouble()) {
      return '${_addThousandSeparator(price.round().toString())} đ';
    } else {
      String formatted = price.toStringAsFixed(2);
      if (formatted.endsWith('.00')) {
        formatted = formatted.substring(0, formatted.length - 3);
        return '${_addThousandSeparator(formatted)} đ';
      } else {
        List<String> parts = formatted.split('.');
        String integerPart = _addThousandSeparator(parts[0]);
        return '$integerPart,${parts[1]} đ';
      }
    }
  }
  String _addThousandSeparator(String number) {
    String result = '';
    int counter = 0;

    for (int i = number.length - 1; i >= 0; i--) {
      if (counter == 3) {
        result = '.$result';
        counter = 0;
      }
      result = number[i] + result;
      counter++;
    }

    return result;
  }
  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return FadeTransition(
      opacity: _fadeAnimation,
      child: SlideTransition(
        position: _slideAnimation,
        child: Container(
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          child: Material(
            elevation: 12,
            borderRadius: BorderRadius.circular(16),
            shadowColor: Colors.black.withOpacity(0.3),
            color: Colors.transparent,
            child: InkWell(
              onTap: widget.onTap,
              borderRadius: BorderRadius.circular(16),
              child: Container(
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(16),
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [
                      const Color(0xFF2C3E50),
                      const Color(0xFF34495E),
                      const Color(0xFF1A252F),
                    ],
                  ),
                  border: Border.all(
                    color: widget.package.isOnSale
                        ? const Color(0xFF0061F3).withOpacity(0.5)
                        : Colors.white.withOpacity(0.1),
                    width: 1,
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.2),
                      blurRadius: 20,
                      offset: const Offset(0, 8),
                    ),
                  ],
                ),
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Header with title and sale badge
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  widget.package.name,
                                  style: const TextStyle(
                                    fontSize: 24,
                                    fontWeight: FontWeight.bold,
                                    color: Colors.white,
                                  ),
                                ),
                                const SizedBox(height: 8),
                                Text(
                                  widget.package.description,
                                  style: const TextStyle(
                                    fontSize: 14,
                                    color: Colors.white70,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          if (widget.package.isOnSale)
                            SaleBadge(
                              discountPercentage: widget.package.discountPercentage,
                              delay: Duration(milliseconds: widget.index * 150 + 400),
                            ),
                        ],
                      ),

                      const SizedBox(height: 20),

                      // Pricing section
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.end,
                        children: [
                          if (widget.package.isOnSale) ...[
                            Text(
                              _formatPrice(widget.package.originalPrice),
                              style: const TextStyle(
                                fontSize: 16,
                                decoration: TextDecoration.lineThrough,
                                color: Colors.white54,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                            const SizedBox(width: 12),
                          ],

                          // Animated current price
                          AnimatedPriceCounter(
                            originalPrice: widget.package.originalPrice,
                            finalPrice: widget.package.price,
                            textStyle: TextStyle(
                              fontSize: 28,
                              fontWeight: FontWeight.bold,
                              color: widget.package.isOnSale
                                  ? const Color(0xFF4CAF50)
                                  : Colors.white,
                            ),
                          ),

                          const Spacer(),

                          // Purchase button
                          ElevatedButton(
                            onPressed: widget.onTap,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: widget.package.isOnSale
                                  ? const Color(0xFF0061F3)
                                  : const Color(0xFF64B5F6),
                              foregroundColor: Colors.white,
                              padding: const EdgeInsets.symmetric(
                                horizontal: 24,
                                vertical: 12,
                              ),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(25),
                              ),
                              elevation: 6,
                              shadowColor: Colors.black.withOpacity(0.3),
                            ),
                            child: const Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Icon(Icons.shopping_cart, size: 18),
                                SizedBox(width: 8),
                                Text(
                                  'Purchase',
                                  style: TextStyle(
                                    fontWeight: FontWeight.bold,
                                    color: Colors.white,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

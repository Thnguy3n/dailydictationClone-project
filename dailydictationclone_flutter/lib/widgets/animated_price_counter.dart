import 'package:flutter/material.dart';

class AnimatedPriceCounter extends StatefulWidget {
  final double originalPrice;
  final double finalPrice;
  final Duration duration;
  final TextStyle? textStyle;

  const AnimatedPriceCounter({
    Key? key,
    required this.originalPrice,
    required this.finalPrice,
    this.duration = const Duration(milliseconds: 1500),
    this.textStyle,
  }) : super(key: key);

  @override
  State<AnimatedPriceCounter> createState() => _AnimatedPriceCounterState();
}

class _AnimatedPriceCounterState extends State<AnimatedPriceCounter>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: widget.duration,
      vsync: this,
    );

    _animation = Tween<double>(
      begin: widget.originalPrice,
      end: widget.finalPrice,
    ).animate(CurvedAnimation(
      parent: _controller,
      curve: Curves.easeOutCubic,
    ));

    Future.delayed(const Duration(milliseconds: 300), () {
      if (mounted) {
        _controller.forward();
      }
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
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
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _animation,
      builder: (context, child) {
        return Text(
          _formatPrice(_animation.value),
          style: widget.textStyle ?? const TextStyle(
            fontSize: 24,
            fontWeight: FontWeight.bold,
            color: Colors.green,
          ),
        );
      },
    );
  }
}

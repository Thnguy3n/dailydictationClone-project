import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';
import 'package:flutter/material.dart';

import '../models/bankApp.dart';
import '../models/payment_status_response.dart';
import '../service/payment_service.dart';
import 'bank_selection.dart';

class PaymentDialog extends StatefulWidget {
  final String qrDataURL;
  final String packageName;
  final double price;
  final DateTime expireAt;
  final int qrTransactionId; // Thêm qrTransactionId từ QrResponse

  const PaymentDialog({
    Key? key,
    required this.qrDataURL,
    required this.packageName,
    required this.price,
    required this.expireAt,
    required this.qrTransactionId,
  }) : super(key: key);

  @override
  State<PaymentDialog> createState() => _PaymentDialogState();
}

class _PaymentDialogState extends State<PaymentDialog>
    with SingleTickerProviderStateMixin {
  late Uint8List _qrImageData;
  late AnimationController _animationController;
  late Animation<double> _scaleAnimation;
  late Animation<double> _fadeAnimation;

  // Countdown timer variables
  late Timer _countdownTimer;
  Duration _remainingTime = Duration.zero;
  bool _isExpired = false;

  bool _showBankSelection = false;
  BankApp? _selectedBank;

  // Payment status variables
  PaymentStatusResponse? _paymentStatus;
  bool _isCheckingPayment = false;
  Timer? _autoCheckTimer;

  @override
  void initState() {
    super.initState();
    _qrImageData = _getImageFromBase64(widget.qrDataURL);
    _initializeAnimations();
    _initializeCountdown();
    _startAutoPaymentCheck();
  }

  void _initializeAnimations() {
    _animationController = AnimationController(
      duration: const Duration(milliseconds: 300),
      vsync: this,
    );

    _scaleAnimation = Tween<double>(
      begin: 0.8,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeOutBack,
    ));

    _fadeAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeOut,
    ));

    _animationController.forward();
  }

  void _initializeCountdown() {
    _updateRemainingTime();
    _countdownTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      _updateRemainingTime();
    });
  }

  void _updateRemainingTime() {
    final now = DateTime.now();
    final difference = widget.expireAt.difference(now);

    if (difference.isNegative) {
      setState(() {
        _remainingTime = Duration.zero;
        _isExpired = true;
      });
      _countdownTimer.cancel();
      _autoCheckTimer?.cancel();
    } else {
      setState(() {
        _remainingTime = difference;
        _isExpired = false;
      });
    }
  }

  void _startAutoPaymentCheck() {
    // Tự động kiểm tra thanh toán mỗi 5 giây
    _autoCheckTimer = Timer.periodic(const Duration(seconds: 5), (timer) {
      if (!_isExpired && _paymentStatus?.isCompleted != true) {
        _checkPaymentStatus(showLoading: false);
      }
    });
  }

  Future<void> _checkPaymentStatus({bool showLoading = true}) async {
    if (_isCheckingPayment) return;

    setState(() {
      _isCheckingPayment = showLoading;
    });

    try {
      final status = await PaymentService.checkPaymentStatus(widget.qrTransactionId);

      setState(() {
        _paymentStatus = status;
        _isCheckingPayment = false;
      });

      // Nếu thanh toán thành công, dừng timer tự động kiểm tra
      if (status.isPaid) {
        _autoCheckTimer?.cancel();
        _showPaymentSuccessDialog();
      } else if (status.isExpired || status.isCancelled) {
        _autoCheckTimer?.cancel();
      }

    } catch (e) {
      setState(() {
        _isCheckingPayment = false;
      });

      if (showLoading) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Lỗi kiểm tra thanh toán: ${e.toString()}'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  void _showPaymentSuccessDialog() {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        backgroundColor: const Color(0xFF2D2D2D),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
        ),
        title: const Row(
          children: [
            Icon(Icons.check_circle, color: Colors.green, size: 28),
            SizedBox(width: 12),
            Text(
              'Thanh toán thành công!',
              style: TextStyle(color: Colors.white, fontSize: 18),
            ),
          ],
        ),
        content: Text(
          'Bạn đã thanh toán thành công gói ${widget.packageName}. Tính năng premium đã được kích hoạt.',
          style: const TextStyle(color: Colors.white70),
        ),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.of(context).pop(); // Đóng dialog thành công
              Navigator.of(context).pop(); // Đóng payment dialog
            },
            child: const Text(
              'OK',
              style: TextStyle(color: Color(0xFF4CAF50)),
            ),
          ),
        ],
      ),
    );
  }

  String _formatDuration(Duration duration) {
    if (duration.isNegative || duration == Duration.zero) {
      return "00:00";
    }

    int minutes = duration.inMinutes % 60;
    int seconds = duration.inSeconds % 60;
    return "${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}";
  }

  Color _getCountdownColor() {
    if (_isExpired) return Colors.red;
    if (_remainingTime.inMinutes < 1) return Colors.orange;
    return const Color(0xFF4CAF50);
  }

  Color _getStatusColor() {
    if (_paymentStatus == null) return Colors.grey;
    if (_paymentStatus!.isPaid) return Colors.green;
    if (_paymentStatus!.isExpired) return Colors.red;
    if (_paymentStatus!.isCancelled) return Colors.orange;
    return Colors.blue; // PENDING
  }

  String _getStatusText() {
    if (_paymentStatus == null) return 'Chưa kiểm tra';
    if (_paymentStatus!.isPaid) return 'Đã thanh toán';
    if (_paymentStatus!.isExpired) return 'Hết hạn';
    if (_paymentStatus!.isCancelled) return 'Đã hủy';
    return 'Đang chờ thanh toán...';
  }

  IconData _getStatusIcon() {
    if (_paymentStatus == null) return Icons.help_outline;
    if (_paymentStatus!.isPaid) return Icons.check_circle;
    if (_paymentStatus!.isExpired) return Icons.error;
    if (_paymentStatus!.isCancelled) return Icons.cancel;
    return Icons.schedule;
  }

  @override
  void dispose() {
    _animationController.dispose();
    _countdownTimer.cancel();
    _autoCheckTimer?.cancel();
    super.dispose();
  }

  Uint8List _getImageFromBase64(String base64String) {
    // Remove data:image/png;base64, prefix if exists
    String cleanBase64 = base64String;
    if (base64String.contains(',')) {
      cleanBase64 = base64String.split(',')[1];
    }
    return base64Decode(cleanBase64);
  }

  void _toggleBankSelection() {
    setState(() {
      _showBankSelection = !_showBankSelection;
    });
  }

  void _onBankSelected(BankApp bank) {
    setState(() {
      _selectedBank = bank;
      _showBankSelection = false;
    });

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Đã mở ${bank.appName}'),
        backgroundColor: const Color(0xFF4CAF50),
        duration: const Duration(seconds: 2),
      ),
    );
  }

  Widget _buildCountdownWidget() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: _getCountdownColor().withOpacity(0.1),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(
          color: _getCountdownColor(),
          width: 1,
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            _isExpired ? Icons.access_time_filled : Icons.timer,
            color: _getCountdownColor(),
            size: 20,
          ),
          const SizedBox(width: 8),
          Text(
            _isExpired ? 'Hết hạn' : _formatDuration(_remainingTime),
            style: TextStyle(
              color: _getCountdownColor(),
              fontSize: 16,
              fontWeight: FontWeight.bold,
              fontFamily: 'monospace',
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPaymentStatusWidget() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: _getStatusColor().withOpacity(0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: _getStatusColor(),
          width: 1,
        ),
      ),
      child: Row(
        children: [
          Icon(
            _getStatusIcon(),
            color: _getStatusColor(),
            size: 24,
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Trạng thái thanh toán',
                  style: TextStyle(
                    color: _getStatusColor(),
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  _getStatusText(),
                  style: TextStyle(
                    color: _getStatusColor(),
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                if (_paymentStatus != null) ...[
                  const SizedBox(height: 4),
                  Text(
                    _paymentStatus!.message,
                    style: TextStyle(
                      color: _getStatusColor().withOpacity(0.7),
                      fontSize: 12,
                    ),
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      backgroundColor: Colors.transparent,
      child: AnimatedBuilder(
        animation: _animationController,
        builder: (context, child) {
          return Transform.scale(
            scale: _scaleAnimation.value,
            child: Opacity(
              opacity: _fadeAnimation.value,
              child: Container(
                width: MediaQuery.of(context).size.width * 0.9,
                constraints: BoxConstraints(
                  maxWidth: 400,
                  maxHeight: MediaQuery.of(context).size.height * 0.9,
                ),
                decoration: BoxDecoration(
                  color: const Color(0xFF1E1E1E),
                  borderRadius: BorderRadius.circular(20),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.3),
                      blurRadius: 20,
                      offset: const Offset(0, 10),
                    ),
                  ],
                ),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    // Header
                    Container(
                      padding: const EdgeInsets.all(20),
                      decoration: const BoxDecoration(
                        color: Color(0xFF2D2D2D),
                        borderRadius: BorderRadius.only(
                          topLeft: Radius.circular(20),
                          topRight: Radius.circular(20),
                        ),
                      ),
                      child: Row(
                        children: [
                          const Icon(
                            Icons.qr_code_2,
                            color: Color(0xFF4CAF50),
                            size: 24,
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                const Text(
                                  'Thanh toán gói dịch vụ',
                                  style: TextStyle(
                                    color: Colors.white,
                                    fontSize: 18,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                                Text(
                                  widget.packageName,
                                  style: const TextStyle(
                                    color: Colors.white70,
                                    fontSize: 14,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          IconButton(
                            onPressed: () => Navigator.of(context).pop(),
                            icon: const Icon(
                              Icons.close,
                              color: Colors.white70,
                            ),
                          ),
                        ],
                      ),
                    ),

                    // Countdown Timer
                    Container(
                      padding: const EdgeInsets.all(16),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          _buildCountdownWidget(),
                        ],
                      ),
                    ),

                    // Content
                    Expanded(
                      child: SingleChildScrollView(
                        child: Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 20),
                          child: Column(
                            children: [
                              Container(
                                width: double.infinity,
                                child: ElevatedButton.icon(
                                  onPressed: (_isExpired || _paymentStatus?.isPaid == true)
                                      ? null
                                      : _toggleBankSelection,
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: (_isExpired || _paymentStatus?.isPaid == true)
                                        ? Colors.grey
                                        : const Color(0xFF4CAF50),
                                    foregroundColor: Colors.white,
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 16,
                                      vertical: 12,
                                    ),
                                    shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(12),
                                    ),
                                  ),
                                  icon: const Icon(Icons.account_balance),
                                  label: Text(
                                    _paymentStatus?.isPaid == true
                                        ? 'Đã thanh toán thành công'
                                        : _isExpired
                                        ? 'Phiên thanh toán đã hết hạn'
                                        : _selectedBank != null
                                        ? 'Mở ${_selectedBank!.appName}'
                                        : 'Chọn ngân hàng để thanh toán',
                                    style: const TextStyle(
                                      fontSize: 16,
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                ),
                              ),

                              const SizedBox(height: 20),

                              // Expired Message
                              if (_isExpired && _paymentStatus?.isPaid != true) ...[
                                Container(
                                  padding: const EdgeInsets.all(16),
                                  decoration: BoxDecoration(
                                    color: Colors.red.withOpacity(0.1),
                                    borderRadius: BorderRadius.circular(12),
                                    border: Border.all(
                                      color: Colors.red,
                                      width: 1,
                                    ),
                                  ),
                                  child: const Row(
                                    children: [
                                      Icon(
                                        Icons.error_outline,
                                        color: Colors.red,
                                        size: 24,
                                      ),
                                      SizedBox(width: 12),
                                      Expanded(
                                        child: Column(
                                          crossAxisAlignment: CrossAxisAlignment.start,
                                          children: [
                                            Text(
                                              'Phiên thanh toán đã hết hạn',
                                              style: TextStyle(
                                                color: Colors.red,
                                                fontSize: 16,
                                                fontWeight: FontWeight.bold,
                                              ),
                                            ),
                                            Text(
                                              'Vui lòng tạo lại giao dịch mới',
                                              style: TextStyle(
                                                color: Colors.red,
                                                fontSize: 14,
                                              ),
                                            ),
                                          ],
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                                const SizedBox(height: 20),
                              ],

                              // Bank Selection Widget
                              if (_showBankSelection && !_isExpired && _paymentStatus?.isPaid != true)
                                ConstrainedBox(
                                  constraints: const BoxConstraints(
                                    maxHeight: 450,
                                  ),
                                  child: BankSelectionWidget(
                                    onBankSelected: _onBankSelected,
                                  ),
                                ),

                              if (_showBankSelection && !_isExpired && _paymentStatus?.isPaid != true)
                                const SizedBox(height: 20),

                              if (!_showBankSelection && !_isExpired && _paymentStatus?.isPaid != true) ...[
                                Container(
                                  padding: const EdgeInsets.all(16),
                                  decoration: BoxDecoration(
                                    color: Colors.white,
                                    borderRadius: BorderRadius.circular(12),
                                    boxShadow: [
                                      BoxShadow(
                                        color: Colors.black.withOpacity(0.1),
                                        blurRadius: 10,
                                        offset: const Offset(0, 5),
                                      ),
                                    ],
                                  ),
                                  child: Image.memory(
                                    _qrImageData,
                                    width: 300,
                                    height: 300,
                                    fit: BoxFit.contain,
                                    errorBuilder: (context, error, stackTrace) {
                                      return Container(
                                        width: 200,
                                        height: 200,
                                        decoration: BoxDecoration(
                                          color: Colors.grey[300],
                                          borderRadius: BorderRadius.circular(8),
                                        ),
                                        child: const Icon(
                                          Icons.error_outline,
                                          size: 48,
                                          color: Colors.grey,
                                        ),
                                      );
                                    },
                                  ),
                                ),

                                const SizedBox(height: 20),

                                // Instructions
                                Container(
                                  padding: const EdgeInsets.all(16),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFF2D2D2D),
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  child: const Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        'Hướng dẫn thanh toán:',
                                        style: TextStyle(
                                          color: Colors.white,
                                          fontSize: 16,
                                          fontWeight: FontWeight.bold,
                                        ),
                                      ),
                                      SizedBox(height: 8),
                                      Text(
                                        '1. Mở ứng dụng ngân hàng\n2. Quét mã QR trên\n3. Xác nhận thanh toán\n4. Đợi xử lý giao dịch',
                                        style: TextStyle(
                                          color: Colors.white70,
                                          fontSize: 14,
                                          height: 1.5,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),

                                const SizedBox(height: 20),

                                Row(
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  children: [
                                    SizedBox(
                                      width: 16,
                                      height: 16,
                                      child: CircularProgressIndicator(
                                        strokeWidth: 2,
                                        valueColor: AlwaysStoppedAnimation<Color>(
                                          const Color(0xFF4CAF50),
                                        ),
                                      ),
                                    ),
                                    const SizedBox(width: 8),
                                    Text(
                                      _getStatusText(),
                                      style: TextStyle(
                                        color: _getStatusColor(),
                                        fontSize: 16,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                  ],
                                ),
                              ],
                            ],
                          ),
                        ),
                      ),
                    ),

                    const SizedBox(height: 20),
                  ],
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}
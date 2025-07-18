// models/payment_status_response.dart
class PaymentStatusResponse {
  final String status; // "PENDING", "PAID", "EXPIRED", "CANCELLED"
  final String message;
  final DateTime checkedAt;

  PaymentStatusResponse({
    required this.status,
    required this.message,
    required this.checkedAt,
  });

  factory PaymentStatusResponse.fromJson(Map<String, dynamic> json) {
    return PaymentStatusResponse(
      status: json['status'] as String,
      message: json['message'] as String,
      checkedAt: DateTime.parse(json['checkedAt'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'status': status,
      'message': message,
      'checkedAt': checkedAt.toIso8601String(),
    };
  }

  bool get isPending => status == 'PENDING';
  bool get isPaid => status == 'PAID';
  bool get isExpired => status == 'EXPIRED';
  bool get isCancelled => status == 'CANCELLED';

  bool get isCompleted => isPaid || isExpired || isCancelled;
}
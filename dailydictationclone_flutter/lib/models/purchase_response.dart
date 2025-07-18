class PurchaseResponse {
  final String purchaseId;

  PurchaseResponse({required this.purchaseId});

  factory PurchaseResponse.fromJson(Map<String, dynamic> json) {
    return PurchaseResponse(
      purchaseId: json['purchaseId'] ?? '',
    );
  }
}
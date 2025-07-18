class QrRequest {
  final String purchaseId;

  QrRequest({required this.purchaseId});

  Map<String, dynamic> toJson() {
    return {
      'purchaseId': purchaseId,
    };
  }
}
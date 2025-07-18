class PremiumPurchaseRequest {
  final int premiumPackageId;

  PremiumPurchaseRequest({required this.premiumPackageId});

  Map<String, dynamic> toJson() {
    return {
      'premiumPackageId': premiumPackageId,
    };
  }
}
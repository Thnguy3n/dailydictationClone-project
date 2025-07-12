class PremiumPackage {
  final int id;
  final String name;
  final double originalPrice;
  final double price;
  final double discount;
  final DateTime? discountStart;
  final DateTime? discountEnd;
  final String discountStatus;
  final String description;

  PremiumPackage({
    required this.id,
    required this.name,
    required this.originalPrice,
    required this.price,
    required this.discount,
    this.discountStart,
    this.discountEnd,
    required this.discountStatus,
    required this.description,
  });

  factory PremiumPackage.fromJson(Map<String, dynamic> json) {
    return PremiumPackage(
      id: json['id'] ?? '',
      name: json['name'] ?? '',
      originalPrice: (json['originalPrice'] ?? 0).toDouble(),
      price: (json['price'] ?? 0).toDouble(),
      discount: (json['discount'] ?? 0).toDouble(),
      discountStart: json['discountStart'] != null
          ? DateTime.tryParse(json['discountStart'])
          : null,
      discountEnd: json['discountEnd'] != null
          ? DateTime.tryParse(json['discountEnd'])
          : null,
      discountStatus: json['discountStatus'] ?? '',
      description: json['description'] ?? '',
    );
  }

  bool get isOnSale => discountStatus.toUpperCase() == 'ON';

  double get discountPercentage =>
      originalPrice > 0 ? ((originalPrice - price) / originalPrice * 100) : 0;
}

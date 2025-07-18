class QrData {
  final String qrCode;
  final String qrDataURL;
  QrData({required this.qrCode, required this.qrDataURL});

  factory QrData.fromJson(Map<String, dynamic> json) {
    return QrData(
      qrCode: json['qrCode'] ?? '',
      qrDataURL: json['qrDataURL'] ?? '',
    );
  }
}

class QrResponse {
  final int id;
  final String code;
  final String description;
  final QrData data;
  final DateTime expireAt;

  QrResponse({
    required this.id,
    required this.code,
    required this.description,
    required this.data,
    required this.expireAt
  });

  factory QrResponse.fromJson(Map<String, dynamic> json) {
    return QrResponse(
      id: json['id'] ?? 0,
      code: json['code'] ?? '',
      description: json['description'] ?? '',
      data: QrData.fromJson(json['data'] ?? {}),
      expireAt: DateTime.parse(json['expireAt'] ?? DateTime.now().toIso8601String()),
    );
  }
}
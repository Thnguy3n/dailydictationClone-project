class BankApp {
  final String appId;
  final String appLogo;
  final String appName;
  final String bankName;
  final int monthlyInstall;
  final String deeplink;
  final int autofill;

  BankApp({
    required this.appId,
    required this.appLogo,
    required this.appName,
    required this.bankName,
    required this.monthlyInstall,
    required this.deeplink,
    required this.autofill,
  });

  factory BankApp.fromJson(Map<String, dynamic> json) {
    return BankApp(
      appId: json['appId'] ?? '',
      appLogo: json['appLogo'] ?? '',
      appName: json['appName'] ?? '',
      bankName: json['bankName'] ?? '',
      monthlyInstall: json['monthlyInstall'] ?? 0,
      deeplink: json['deeplink'] ?? '',
      autofill: json['autofill'] ?? 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'appId': appId,
      'appLogo': appLogo,
      'appName': appName,
      'bankName': bankName,
      'monthlyInstall': monthlyInstall,
      'deeplink': deeplink,
      'autofill': autofill,
    };
  }

  bool get isAutofillSupported => autofill == 1;

  String get formattedMonthlyInstall {
    if (monthlyInstall >= 1000000) {
      return '${(monthlyInstall / 1000000).toStringAsFixed(1)}M';
    } else if (monthlyInstall >= 1000) {
      return '${(monthlyInstall / 1000).toStringAsFixed(0)}K';
    } else {
      return monthlyInstall.toString();
    }
  }

  @override
  String toString() {
    return 'BankApp{appId: $appId, appName: $appName, bankName: $bankName, monthlyInstall: $monthlyInstall}';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is BankApp && other.appId == appId;
  }

  @override
  int get hashCode => appId.hashCode;
}
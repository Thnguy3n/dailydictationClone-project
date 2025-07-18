import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/bankApp.dart';
import '../models/premium_package.dart';
import '../models/purchase_request.dart';
import '../models/purchase_response.dart';
import '../models/qr_request.dart';
import '../models/qr_response.dart';
import '../models/payment_status_response.dart';
import 'auth_service.dart';

class PaymentService {
  static const String paymentServiceUrl = 'http://10.0.2.2:8181/api';
  static const String vietQrApiUrl = 'https://api.vietqr.io/v2/android-app-deeplinks';

  static Future<List<PremiumPackage>> fetchPremiumPackages() async {
    try {
      final response = await http.get(
        Uri.parse('$paymentServiceUrl/payment/premium-package/all'),
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        final List<dynamic> jsonData = json.decode(response.body);
        return jsonData
            .map((json) => PremiumPackage.fromJson(json))
            .toList();
      } else {
        throw Exception('Failed to load premium packages: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Network error: $e');
    }
  }

  static Future<PurchaseResponse> purchasePremium(int premiumPackageId) async {
    try {
      final token = await AuthService.getToken();
      if (token == null) {
        throw Exception('No authentication token found');
      }

      final request = PremiumPurchaseRequest(premiumPackageId: premiumPackageId);

      final response = await http.post(
        Uri.parse('$paymentServiceUrl/payment/premium-purchase'),
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'Authorization': 'Bearer $token',
        },
        body: json.encode(request.toJson()),
      );

      if (response.statusCode == 200) {
        final jsonData = json.decode(response.body);
        return PurchaseResponse.fromJson(jsonData);
      } else if (response.statusCode == 401) {
        await AuthService.removeToken();
        throw Exception('Session expired. Please sign in again.');
      } else {
        throw Exception('Failed to purchase premium: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Purchase error: $e');
    }
  }

  static Future<QrResponse> generateQrCode(String purchaseId) async {
    try {
      final request = QrRequest(purchaseId: purchaseId);
      print('Generating QR code for purchase ID: $purchaseId');
      final response = await http.post(
        Uri.parse('$paymentServiceUrl/payment/qr/generate'),
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: json.encode(request.toJson()),
      );

      if (response.statusCode == 200) {
        final jsonData = json.decode(response.body);
        return QrResponse.fromJson(jsonData);
      } else {
        throw Exception('Failed to generate QR code: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('QR generation error: $e');
    }
  }

  static Future<PaymentStatusResponse> checkPaymentStatus(int qrTransactionId) async {
    try {
      print('Checking payment status for transaction ID: $qrTransactionId');

      final response = await http.get(
        Uri.parse('$paymentServiceUrl/payment/qr/status/$qrTransactionId'),
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        final jsonData = json.decode(response.body);
        return PaymentStatusResponse.fromJson(jsonData);
      } else {
        throw Exception('Failed to check payment status: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Payment status check error: $e');
    }
  }

  static Future<List<BankApp>> fetchBankApps() async {
    try {
      final response = await http.get(
        Uri.parse(vietQrApiUrl),
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> jsonData = json.decode(response.body);
        final List<dynamic> apps = jsonData['apps'];

        return apps
            .map((json) => BankApp.fromJson(json))
            .toList();
      } else {
        throw Exception('Failed to load bank apps: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Network error while fetching bank apps: $e');
    }
  }

  static Future<List<BankApp>> searchBankApps(String query) async {
    try {
      final allBanks = await fetchBankApps();

      if (query.isEmpty) {
        return allBanks;
      }

      final filteredBanks = allBanks.where((bank) {
        final searchQuery = query.toLowerCase();
        return bank.appName.toLowerCase().contains(searchQuery) ||
            bank.bankName.toLowerCase().contains(searchQuery) ||
            bank.appId.toLowerCase().contains(searchQuery);
      }).toList();

      return filteredBanks;
    } catch (e) {
      throw Exception('Error searching bank apps: $e');
    }
  }

  static Future<List<BankApp>> getPopularBankApps({int limit = 10}) async {
    try {
      final allBanks = await fetchBankApps();

      allBanks.sort((a, b) => b.monthlyInstall.compareTo(a.monthlyInstall));

      return allBanks.take(limit).toList();
    } catch (e) {
      throw Exception('Error fetching popular bank apps: $e');
    }
  }

  static Future<List<BankApp>> getAutofillSupportedBanks() async {
    try {
      final allBanks = await fetchBankApps();

      return allBanks.where((bank) => bank.autofill == 1).toList();
    } catch (e) {
      throw Exception('Error fetching autofill supported banks: $e');
    }
  }
}
import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/premium_package.dart';

class PaymentService {
  static const String paymentServiceUrl = 'http://10.0.2.2:8181/api';

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
}

import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../config/constants.dart';
import '../models/user.dart';
import '../models/ride.dart';
import '../models/booking.dart';

class ApiService {
  final _storage = const FlutterSecureStorage();
  
  // Headers cu JWT token
  Future<Map<String, String>> _getHeaders() async {
    final token = await _storage.read(key: AppConstants.tokenKey);
    return {
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  // ========== AUTHENTICATION ==========
  
  Future<Map<String, dynamic>> register({
    required String email,
    required String password,
    required String fullName,
    String? phoneNumber,
  }) async {
    final response = await http.post(
      Uri.parse('${AppConstants.baseUrl}${AppConstants.registerEndpoint}'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'email': email,
        'password': password,
        'fullName': fullName,
        'phoneNumber': phoneNumber,
      }),
    );

    if (response.statusCode == 201) {
      final data = jsonDecode(response.body);
      // Salvează token
      await _storage.write(key: AppConstants.tokenKey, value: data['token']);
      await _storage.write(key: AppConstants.userIdKey, value: data['id'].toString());
      await _storage.write(key: AppConstants.userEmailKey, value: data['email']);
      await _storage.write(key: AppConstants.userRoleKey, value: data['role']);
      return data;
    } else {
      final error = jsonDecode(response.body);
      throw Exception(error['message'] ?? 'Registration failed');
    }
  }

  Future<Map<String, dynamic>> login({
    required String email,
    required String password,
  }) async {
    final response = await http.post(
      Uri.parse('${AppConstants.baseUrl}${AppConstants.loginEndpoint}'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'email': email,
        'password': password,
      }),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      // Salvează token
      await _storage.write(key: AppConstants.tokenKey, value: data['token']);
      await _storage.write(key: AppConstants.userIdKey, value: data['id'].toString());
      await _storage.write(key: AppConstants.userEmailKey, value: data['email']);
      await _storage.write(key: AppConstants.userRoleKey, value: data['role']);
      return data;
    } else {
      final error = jsonDecode(response.body);
      throw Exception(error['message'] ?? 'Login failed');
    }
  }

  Future<void> logout() async {
    await _storage.deleteAll();
  }

  Future<bool> isLoggedIn() async {
    final token = await _storage.read(key: AppConstants.tokenKey);
    return token != null;
  }

  // ========== RIDES ==========
  
  Future<List<Ride>> getRides() async {
    final response = await http.get(
      Uri.parse('${AppConstants.baseUrl}${AppConstants.ridesEndpoint}'),
      headers: await _getHeaders(),
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => Ride.fromJson(json)).toList();
    } else {
      throw Exception('Failed to load rides');
    }
  }

  Future<List<Ride>> searchRides(String from, String to) async {
    final response = await http.get(
      Uri.parse('${AppConstants.baseUrl}${AppConstants.ridesEndpoint}/search?from=$from&to=$to'),
      headers: await _getHeaders(),
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => Ride.fromJson(json)).toList();
    } else {
      throw Exception('Failed to search rides');
    }
  }

  // ========== BOOKINGS ==========
  
  Future<Booking> createBooking({
    required int rideId,
    required int seatsBooked,
  }) async {
    final response = await http.post(
      Uri.parse('${AppConstants.baseUrl}${AppConstants.bookingsEndpoint}'),
      headers: await _getHeaders(),
      body: jsonEncode({
        'rideId': rideId,
        'seatsBooked': seatsBooked,
      }),
    );

    if (response.statusCode == 201) {
      return Booking.fromJson(jsonDecode(response.body));
    } else {
      final error = jsonDecode(response.body);
      throw Exception(error['message'] ?? 'Failed to create booking');
    }
  }

  Future<List<Booking>> getMyBookings() async {
    final userId = await _storage.read(key: AppConstants.userIdKey);
    final response = await http.get(
      Uri.parse('${AppConstants.baseUrl}${AppConstants.bookingsEndpoint}/passenger/$userId'),
      headers: await _getHeaders(),
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => Booking.fromJson(json)).toList();
    } else {
      throw Exception('Failed to load bookings');
    }
  }
}
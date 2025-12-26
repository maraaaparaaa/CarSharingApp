class AppConstants {
  // Backend URL - SCHIMBĂ când deploy pe Azure!
  static const String baseUrl = 'http://localhost:8080/api';
  
  // Endpoints
  static const String loginEndpoint = '/auth/login';
  static const String registerEndpoint = '/auth/register';
  static const String ridesEndpoint = '/rides';
  static const String bookingsEndpoint = '/bookings';
  static const String usersEndpoint = '/users';
  
  // Storage keys
  static const String tokenKey = 'jwt_token';
  static const String userIdKey = 'user_id';
  static const String userEmailKey = 'user_email';
  static const String userRoleKey = 'user_role';
}
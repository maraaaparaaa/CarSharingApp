class Booking {
  final int id;
  final int passengerId;
  final String passengerName;
  final int rideId;
  final String startLocation;
  final String endLocation;
  final int seatsBooked;
  final double totalPrice;
  final String status;

  Booking({
    required this.id,
    required this.passengerId,
    required this.passengerName,
    required this.rideId,
    required this.startLocation,
    required this.endLocation,
    required this.seatsBooked,
    required this.totalPrice,
    required this.status,
  });

  factory Booking.fromJson(Map<String, dynamic> json) {
    return Booking(
      id: json['id'],
      passengerId: json['passengerId'],
      passengerName: json['passengerName'],
      rideId: json['rideId'],
      startLocation: json['startLocation'],
      endLocation: json['endLocation'],
      seatsBooked: json['seatsBooked'],
      totalPrice: (json['totalPrice'] as num).toDouble(),
      status: json['status'],
    );
  }
}
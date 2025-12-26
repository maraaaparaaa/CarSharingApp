class Ride {
  final int id;
  final String startLocation;
  final String endLocation;
  final DateTime departureTime;
  final double pricePerSeat;
  final int availableSeats;
  final int driverId;
  final String driverName;

  Ride({
    required this.id,
    required this.startLocation,
    required this.endLocation,
    required this.departureTime,
    required this.pricePerSeat,
    required this.availableSeats,
    required this.driverId,
    required this.driverName,
  });

  factory Ride.fromJson(Map<String, dynamic> json) {
    return Ride(
      id: json['id'],
      startLocation: json['startLocation'],
      endLocation: json['endLocation'],
      departureTime: DateTime.parse(json['departureTime']),
      pricePerSeat: (json['pricePerSeat'] as num).toDouble(),
      availableSeats: json['availableSeats'],
      driverId: json['driverId'],
      driverName: json['driverName'],
    );
  }
}
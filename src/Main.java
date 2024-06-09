import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class Main {
    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String username = "root";
    private static final String password = "Kaaviya@1";

    public static void main(String[] args) {

        try{
            Connection connection = DriverManager.getConnection(url, username, password);
            while(true){
                System.out.println();
                System.out.println("HOTEL MANAGEMENT SYSTEM");
                Scanner scanner = new Scanner(System.in);
                System.out.println("1. Reserve a room");
                System.out.println("2. View Reservations");
                System.out.println("3. Get Room Number");
                System.out.println("4. Vacating Room");
                System.out.println("5. Delete Reservations");
                System.out.println("6. View Rooms");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        reserveRoom(connection, scanner);
                        break;
                    case 2:
                        viewReservations(connection);
                        break;
                    case 3:
                        getRoomNumber(connection, scanner);
                        break;
                    case 4:
                        vacatingRoom(connection, scanner);
                        break;
                    case 5:
                        deleteReservation(connection, scanner);
                        break;
                    case 0:
                        exit();
                        scanner.close();
                        return;
                    case 6:
                        viewRooms(connection);
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }

        }catch (SQLException e){
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private static void viewRooms(Connection connection) throws SQLException {
        String sql = "SELECT room_number,reservation_id FROM reserve_rooms";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Current Reservations:");
            System.out.println("+----------------+-----------------+");
            System.out.println("| Room Number | Reservation ID     |");
            System.out.println("+----------------+-----------------+");

            while (resultSet.next()) {
                int reservationId = resultSet.getInt("reservation_id");
                int roomNumber = resultSet.getInt("room_number");

                System.out.printf("| %-14d | %-15s |\n",
                        roomNumber,reservationId );
            }
            System.out.println("+----------------+-----------------+");
        }
    }


    private static void reserveRoom(Connection connection, Scanner scanner) throws  SQLException{

        try{
            int gotRoomNumber = reserve_room_number(connection);
            int roomNumber;
            if(gotRoomNumber != 0){
                roomNumber = gotRoomNumber;
            }else {
                System.out.println("No Rooms Available!");
                return;
            }
            System.out.print("Enter guest name: ");
            String guestName = scanner.next();
            scanner.nextLine();

            System.out.print("Enter contact number: ");
            String contactNumber = scanner.next();

            int reserve_new_id = get_last_id(connection);
            int customer_id = reserve_new_id + 1;
            String str_id = Integer.toString(customer_id);

            String sql = "INSERT INTO reservations (guest_name, room_number, contact_number) " +
                    "VALUES ('" + guestName + "', " + roomNumber + ", '" + contactNumber + "')";

            String reserve_table = "UPDATE reserve_rooms SET reservation_id = '" + str_id +
                    "' WHERE room_number = " + roomNumber;

            try (Statement statement = connection.createStatement()) {
                int affectedRows = statement.executeUpdate(sql);

                if (affectedRows > 0 ) {
                    try (Statement stmt = connection.createStatement()){
                        int reserve_row = stmt.executeUpdate(reserve_table);

                        if(reserve_row > 0){
                            System.out.println("Reservation successful!");
                            System.out.println("And your Reservation is " + customer_id);
                        }
                    }
                } else {
                    System.out.println("Reservation failed.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static int reserve_room_number(Connection connection)throws SQLException {
        String sql = "SELECT max(room_number) from reserve_rooms WHERE reservation_id IS NULL";
        int id = 0;
        try(Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)){

            while (resultSet.next()) {
                id = resultSet.getInt(1);
            }

        }
        return id;
    }

    private static int get_last_id(Connection connection) throws SQLException{
        String sql = "SELECT max(reservation_id) from reservations";
        int id = 0;
        try(Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)){

            while (resultSet.next()) {
                id = resultSet.getInt(1);
            }

        }
        return id;
    }

    private static void viewReservations(Connection connection) throws SQLException {
        String sql = "SELECT reservation_id, guest_name, room_number, contact_number, reservation_date_time, vacating_time FROM reservations";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Current Reservations:");
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+------------------------------------");
            System.out.println("| Reservation ID | Guest           | Room Number   | Contact Number      | Reservation Date And Time        | Vacating Time              |");
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+------------------------------------");

            while (resultSet.next()) {
                int reservationId = resultSet.getInt("reservation_id");
                String guestName = resultSet.getString("guest_name");
                int roomNumber = resultSet.getInt("room_number");
                String contactNumber = resultSet.getString("contact_number");
                String reservationDate = resultSet.getTimestamp("reservation_date_time").toString();
                String vacatingTime = resultSet.getString("vacating_time");

                // Format and display the reservation data in a table-like format
                System.out.printf("| %-14d | %-15s | %-13d | %-20s | %-29s   | %-24s   |\n",
                        reservationId, guestName, roomNumber, contactNumber, reservationDate,vacatingTime);
            }
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+");
        }
    }

    private static void getRoomNumber(Connection connection, Scanner scanner) {
        try {
            System.out.print("Enter reservation ID: ");
            int reservationId = scanner.nextInt();
            System.out.print("Enter guest name: ");
            String guestName = scanner.next();

            String sql = "SELECT room_number FROM reservations " +
                    "WHERE reservation_id = " + reservationId +
                    " AND guest_name = '" + guestName + "'";

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {

                if (resultSet.next()) {
                    int roomNumber = resultSet.getInt("room_number");
                    System.out.println("Room number for Reservation ID " + reservationId +
                            " and Guest " + guestName + " is: " + roomNumber);
                } else {
                    System.out.println("Reservation not found for the given ID and guest name.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void vacatingRoom(Connection connection, Scanner scanner) {
        System.out.println("Enter Reservation id: ");
        int reserve_id = scanner.nextInt();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        System.out.println(formatter.format(date));

        try {
            String gettingRoom ="SELECT room_number FROM reservations " +
                    "WHERE reservation_id = " + reserve_id;

            String sql = "UPDATE reservations SET vacating_time = '" + formatter.format(date) +
                    "' WHERE reservation_id = " + reserve_id;

            try (Statement statement = connection.createStatement()) {

                ResultSet resultSet = statement.executeQuery(gettingRoom);
                int executingRoom;
                String cancel_room = "";
                if (resultSet.next()) {
                    int roomNumber = resultSet.getInt("room_number");
                    cancel_room = "UPDATE reserve_rooms SET reservation_id = NULL WHERE room_number = " + roomNumber;

                } else {
                    System.out.println("Reservation not found for the given ID and guest name.");
                }
                executingRoom = statement.executeUpdate(cancel_room);
                int affectedRows = statement.executeUpdate(sql);


                if (affectedRows > 0 && executingRoom > 0) {
                    System.out.println("Vacating time updated successfully!");
                } else {
                    System.out.println("Time updation failed.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static void deleteReservation(Connection connection, Scanner scanner) {
        try {
            System.out.print("Enter reservation ID to delete: ");
            int reservationId = scanner.nextInt();

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            getVacateAutomatically(connection , reservationId);
            String sql = "DELETE FROM reservations WHERE reservation_id = " + reservationId;

            try (Statement statement = connection.createStatement()) {
                int affectedRows = statement.executeUpdate(sql);

                if (affectedRows > 0) {
                    System.out.println("Reservation deleted successfully!");
                } else {
                    System.out.println("Reservation deletion failed.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void getVacateAutomatically(Connection connection, int reservationId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        System.out.println(formatter.format(date));

        try {
            String gettingRoom ="SELECT room_number FROM reservations " +
                    "WHERE reservation_id = " + reservationId;

            try (Statement statement = connection.createStatement()) {

                ResultSet resultSet = statement.executeQuery(gettingRoom);

                String cancel_room = "";
                if (resultSet.next()) {
                    int roomNumber = resultSet.getInt("room_number");
                    cancel_room = "UPDATE reserve_rooms SET reservation_id = NULL WHERE room_number = " + roomNumber;

                } else {
                    System.out.println("Reservation not found for the given ID and guest name.");
                }
                statement.executeUpdate(cancel_room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static boolean reservationExists(Connection connection, int reservationId) {
        try {
            String sql = "SELECT reservation_id FROM reservations WHERE reservation_id = " + reservationId;

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {

                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void exit() throws InterruptedException {
        System.out.print("Exiting System");
        int i = 5;
        while(i!=0){
            System.out.print(".");
            Thread.sleep(1000);
            i--;
        }
        System.out.println();
        System.out.println("ThankYou For Using Hotel Reservation System!!!");
    }
}


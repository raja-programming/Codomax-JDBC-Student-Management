package com.raja;

import java.sql.*;
import java.util.Scanner;

public class StudentManagementApp {
    private static final String URL = "jdbc:mysql://localhost:3306/student_management";
    private static final String USER = "root";
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            createTable();
            System.out.println("==================================");
            System.out.println(" STUDENT MANAGEMENT SYSTEM - JDBC");
            System.out.println("==================================");

            while (true) {
                System.out.println("\n1. Add Student");
                System.out.println("2. View All Students");
                System.out.println("3. Search Student");
                System.out.println("4. Update Student");
                System.out.println("5. Delete Student");
                System.out.println("0. Exit");
                System.out.print("Choose option: ");

                String choice = sc.nextLine().trim();

                try {
                    switch (choice) {
                        case "1" -> addStudent(sc);
                        case "2" -> viewStudents();
                        case "3" -> searchStudent(sc);
                        case "4" -> updateStudent(sc);
                        case "5" -> deleteStudent(sc);
                        case "0" -> {
                            System.out.println("Thank you!");
                            return;
                        }
                        default -> System.out.println("Invalid option.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Cannot connect to MySQL: " + e.getMessage());
            System.out.println("Check MySQL service, database and DB_PASSWORD.");
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS students (
                id INT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(150) NOT NULL,
                course VARCHAR(100) NOT NULL,
                age INT NOT NULL
            )
            """;
        try (Connection con = getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private static void addStudent(Scanner sc) throws SQLException {
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Course: ");
        String course = sc.nextLine().trim();
        System.out.print("Age: ");
        int age = Integer.parseInt(sc.nextLine().trim());

        String sql = "INSERT INTO students(name,email,course,age) VALUES(?,?,?,?)";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, course);
                ps.setInt(4, age);
                ps.executeUpdate();
                con.commit();
                System.out.println("Student added successfully!");
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private static void viewStudents() throws SQLException {
        String sql = "SELECT id,name,email,course,age FROM students ORDER BY id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nID | NAME | EMAIL | COURSE | AGE");
            System.out.println("----------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%d | %s | %s | %s | %d%n",
                    rs.getInt("id"), rs.getString("name"),
                    rs.getString("email"), rs.getString("course"),
                    rs.getInt("age"));
            }
            if (!found) System.out.println("No students found.");
        }
    }

    private static void searchStudent(Scanner sc) throws SQLException {
        System.out.print("Enter student ID: ");
        int id = Integer.parseInt(sc.nextLine().trim());
        String sql = "SELECT * FROM students WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("ID: %d%nName: %s%nEmail: %s%nCourse: %s%nAge: %d%n",
                        rs.getInt("id"), rs.getString("name"),
                        rs.getString("email"), rs.getString("course"),
                        rs.getInt("age"));
                } else {
                    System.out.println("Student not found.");
                }
            }
        }
    }

    private static void updateStudent(Scanner sc) throws SQLException {
        System.out.print("Student ID to update: ");
        int id = Integer.parseInt(sc.nextLine().trim());
        System.out.print("New name: ");
        String name = sc.nextLine().trim();
        System.out.print("New email: ");
        String email = sc.nextLine().trim();
        System.out.print("New course: ");
        String course = sc.nextLine().trim();
        System.out.print("New age: ");
        int age = Integer.parseInt(sc.nextLine().trim());

        String sql = "UPDATE students SET name=?, email=?, course=?, age=? WHERE id=?";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, course);
                ps.setInt(4, age);
                ps.setInt(5, id);
                int rows = ps.executeUpdate();
                con.commit();
                System.out.println(rows > 0 ? "Student updated!" : "Student ID not found.");
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private static void deleteStudent(Scanner sc) throws SQLException {
        System.out.print("Student ID to delete: ");
        int id = Integer.parseInt(sc.nextLine().trim());
        String sql = "DELETE FROM students WHERE id=?";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                int rows = ps.executeUpdate();
                con.commit();
                System.out.println(rows > 0 ? "Student deleted!" : "Student ID not found.");
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }
}

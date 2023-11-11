package RPNcalculator;

import java.sql.*;
import javax.swing.*;

public class Login {
    private JPanel LoginPanel;
    private JTextField Username;
    private JTextField Password;
    private JButton LOGINButton;
    private JButton REGISTERButton;

    static Connection conn1 = null;

    static JFrame frame = new JFrame("Login");

    public Login() {
        LOGINButton.addActionListener(actionEvent -> {
            String username = Username.getText();
            String password = Password.getText();

            try {
                String query = "SELECT * FROM users WHERE username = ? AND password = ?";
                PreparedStatement preparedStatement = conn1.prepareStatement(query);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    frame.setVisible(false);
                    showCalculator(username);
                } else {
                    JOptionPane.showMessageDialog(null, "Credenziali non valide. Riprova.");
                }
                resultSet.close();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Errore durante l'accesso al database.");
            }
        });

        REGISTERButton.addActionListener(actionEvent -> {
            String username = Username.getText();
            String password = Password.getText();

            try {
                String checkUserQuery = "SELECT * FROM users WHERE Username = ?";
                PreparedStatement checkUserStatement = conn1.prepareStatement(checkUserQuery);
                checkUserStatement.setString(1, username);
                ResultSet checkUserResult = checkUserStatement.executeQuery();

                if (checkUserResult.next()) {
                    JOptionPane.showMessageDialog(null, "Utente già esistente.");
                } else {
                    String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
                    PreparedStatement insertStatement = conn1.prepareStatement(insertQuery);
                    insertStatement.setString(1, username);
                    insertStatement.setString(2, password);
                    int rowsAffected = insertStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "Utente registrato con successo!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Impossibile registrare l'utente. Riprova.");
                    }

                    insertStatement.close();
                }

                checkUserResult.close();
                checkUserStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Errore durante la registrazione.");
            }
        });

    }

    public void showCalculator(String username) {
        JFrame calculatorFrame = new JFrame("Calculator");
        calculatorFrame.setContentPane(new Calculator(username).Panel);
        calculatorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        calculatorFrame.pack();
        calculatorFrame.setVisible(true);
    }

    public static void main(String[] args) throws SQLException {
        conn1 = DriverManager.getConnection("jdbc:mysql://127.0.0.1/Login", "root", "");
        frame.setContentPane(new Login().LoginPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}

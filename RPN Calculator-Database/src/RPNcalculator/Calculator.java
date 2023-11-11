package RPNcalculator;

import java.sql.*;
import javax.swing.*;
import java.util.Stack;

public class Calculator {
    public JPanel Panel;
    private JButton Zero;
    private JButton bracket1;
    private JButton Two;
    private JButton One;
    private JButton Three;
    private JButton bracket2;
    private JButton plus;
    private JButton Four;
    private JButton Five;
    private JButton Six;
    private JButton minus;
    private JButton Seven;
    private JButton Eight;
    private JButton Nine;
    private JButton multiplied;
    private JButton divided;
    private JButton delete;
    private JTextField TextField;
    private JButton equal;
    private JButton convert;
    private JRadioButton RPNRadioButton;
    private JRadioButton infixRadioButton;
    private JButton History;
    private JLabel Name;

    private String username;

    static Connection conn1 = null;

    public Calculator(String username) {

        this.username = username;
        Name.setText("User: " + username);

        Zero.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "0");
        });
        One.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "1");
        });
        Two.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "2");
        });
        Three.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "3");
        });
        Four.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "4");
        });
        Five.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "5");
        });
        Six.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "6");
        });
        Seven.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "7");
        });
        Eight.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "8");
        });
        Nine.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "9");
        });
        plus.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "+");
        });
        minus.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "-");
        });
        multiplied.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "*");
        });
        divided.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "/");
        });
        bracket1.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + "(");
        });
        bracket2.addActionListener(actionEvent -> {
            TextField.setText(TextField.getText() + ")");
        });
        delete.addActionListener(actionEvent -> {
            TextField.setText("");
        });
        convert.addActionListener(actionEvent -> {
            String rpnExpression = infixToRPN(TextField.getText());
            TextField.setText(rpnExpression);
        });
        equal.addActionListener(actionEvent -> {
            if (RPNRadioButton.isSelected()) {
                String rpnExpression = TextField.getText();
                String result = calculateRPN(rpnExpression);
                TextField.setText(result);
                try {
                    saveCalculationToHistory(username, rpnExpression, result);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if (infixRadioButton.isSelected()) {
                String infixExpression = TextField.getText();
                String rpnExpression = infixToRPN(infixExpression);
                String result = calculateRPN(rpnExpression);
                TextField.setText(result);
                try {
                    saveCalculationToHistory(username, infixExpression, result);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        RPNRadioButton.addActionListener(actionEvent -> {
            if(RPNRadioButton.isSelected()){
                infixRadioButton.setSelected(false);
            }
        });
        infixRadioButton.addActionListener(actionEvent -> {
            if(infixRadioButton.isSelected()){
                RPNRadioButton.setSelected(false);
            }
        });
        History.addActionListener(actionEvent -> {
            String history = null;
            try {
                history = retrieveUserHistory(username);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            JOptionPane.showMessageDialog(null, history, "History", JOptionPane.INFORMATION_MESSAGE);
        });

    }

    public static String infixToRPN(String infixExpression) {
        StringBuilder output = new StringBuilder();
        Stack<Character> operatorStack = new Stack<>();

        for (int i = 0; i < infixExpression.length(); i++) {
            char token = infixExpression.charAt(i);

            if (Character.isLetterOrDigit(token)) {
                output.append(token);
            } else if (token == '(') {
                operatorStack.push(token);
            } else if (token == ')') {
                output.append(" ");
                while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                    output.append(operatorStack.pop());
                }
                if (!operatorStack.isEmpty() && operatorStack.peek() == '(') {
                    operatorStack.pop();
                }
            } else {
                output.append(" ");
                while (!operatorStack.isEmpty() && priority(token) <= priority(operatorStack.peek())) {
                    output.append(operatorStack.pop());
                    output.append(" ");
                }
                operatorStack.push(token);
            }
        }

        output.append(" ");

        while (!operatorStack.isEmpty()) {
            output.append(operatorStack.pop());
            output.append(" ");
        }

        return output.toString();
    }

    private static int priority(char operator) {
        switch (operator) {
            case '+': return 1;
            case '-': return 1;
            case '*': return 2;
            case '/': return 2;
            default: return 0;
        }
    }

    public static String calculateRPN(String expression) {

        StringBuilder output = new StringBuilder();
        Stack<Double> stack = new Stack<>();
        String[] token = expression.split(" ");

        for (String tokens : token) {
            if (isNumber(tokens)) {
                double value = Double.parseDouble(tokens);
                stack.push(value);
            } else {
                double operand2 = stack.pop();
                double operand1 = stack.pop();

                double result = Operation(tokens, operand1, operand2);
                stack.push(result);
            }
        }

        output.append(stack.pop().toString());

        return output.toString();
    }

    public static boolean isNumber(String s){

        for (int i = 0; i < s.length(); i++){
            char token = s.charAt(i);
            if (!Character.isLetterOrDigit(token)) {
                return false;
            }
        }
        return true;
    }

    private static double Operation(String operator, double operand1, double operand2) {
        double result = 0;
        if(operator.equals("+"))
            result = operand1 + operand2;
        else if(operator.equals("-"))
            result = operand1 - operand2;
        else if(operator.equals("*"))
            result = operand1 * operand2;
        else if(operator.equals("/"))
            result = operand1 / operand2;

        return result;
    }

    public void saveCalculationToHistory(String username, String expression, String result) throws SQLException {
        conn1 = DriverManager.getConnection("jdbc:mysql://127.0.0.1/Login", "root", "");
        try {
            String query = "INSERT INTO Myhistory (username, expression, result) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conn1.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, expression);
            preparedStatement.setString(3, result);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String retrieveUserHistory(String username) throws SQLException {
        conn1 = DriverManager.getConnection("jdbc:mysql://127.0.0.1/Login", "root", "");
        StringBuilder history = new StringBuilder();
        try {
            String query = "SELECT expression, result FROM Myhistory WHERE username = ?";
            PreparedStatement preparedStatement = conn1.prepareStatement(query);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String expression = resultSet.getString("expression");
                String result = resultSet.getString("result");
                history.append("Expression: ").append(expression).append("  Result: ").append(result).append("\n");
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history.toString();
    }
}
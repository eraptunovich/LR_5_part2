import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PsychologySurveyApp extends JFrame {
    private List<Question> questions;
    private int currentQuestionIndex;
    private JTextArea questionTextArea;
    private JCheckBox[] answerCheckboxes;
    private JButton nextButton;
    private int totalPoints;

    public PsychologySurveyApp(String title) {
        super(title);
        questions = loadQuestionsFromFile("psychology_questions.txt");
        currentQuestionIndex = 0;
        totalPoints = 0;

        setLayout(new BorderLayout());

        // Question Panel
        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        questionTextArea = new JTextArea(5, 30);
        questionTextArea.setEditable(false);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionPanel.add(questionTextArea);
        add(questionPanel, BorderLayout.NORTH);

        // Answer Panel
        JPanel answerPanel = new JPanel();
        answerPanel.setLayout(new FlowLayout());
        answerCheckboxes = new JCheckBox[5];
        for (int i = 0; i < 5; i++) {
            answerCheckboxes[i] = new JCheckBox();
            answerPanel.add(answerCheckboxes[i]);
            final int index = i;
            answerCheckboxes[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleAnswerCheckboxClick(index);
                }
            });
        }
        add(answerPanel, BorderLayout.CENTER);

        // Next Button
        nextButton = new JButton("Далее");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleNextButtonClick();
            }
        });
        add(nextButton, BorderLayout.SOUTH);
        nextButton.setEnabled(false); // Initially disable the button

        // Set default close operation
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set frame properties
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Initialize UI
        displayCurrentQuestion();
    }

    private List<Question> loadQuestionsFromFile(String fileName) {
        List<Question> loadedQuestions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    String questionText = parts[0];
                    String[] answers = parts[1].split(",");
                    loadedQuestions.add(new Question(questionText, answers));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loadedQuestions;
    }

    private void displayCurrentQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question currentQuestion = questions.get(currentQuestionIndex);
            questionTextArea.setText(currentQuestion.getText());
            for (int i = 0; i < answerCheckboxes.length; i++) {
                if (i < currentQuestion.getAnswers().length) {
                    answerCheckboxes[i].setText(currentQuestion.getAnswers()[i]);
                    answerCheckboxes[i].setVisible(true);
                } else {
                    answerCheckboxes[i].setVisible(false);
                }
                answerCheckboxes[i].setSelected(false);
            }
        } else {
            // No more questions, survey completed
            displayResults();
            dispose(); // Close the window
        }
    }

    private void handleAnswerCheckboxClick(int index) {
        nextButton.setEnabled(true);
        for (int i = 0; i < answerCheckboxes.length; i++) {
            if (i != index) {
                answerCheckboxes[i].setSelected(false);
            }
        }
    }

    private void handleNextButtonClick() {
        Question currentQuestion = questions.get(currentQuestionIndex);
        for (int i = 0; i < answerCheckboxes.length; i++) {
            if (answerCheckboxes[i].isSelected()) {
                currentQuestion.setUserAnswerIndex(i);
                totalPoints += currentQuestion.getPointsForAnswer(i);
                break;
            }
        }

        currentQuestionIndex++;
        nextButton.setEnabled(false); // Disable the button for the next question until an answer is chosen
        displayCurrentQuestion();
    }

    private void displayResults() {
        StringBuilder results = new StringBuilder("Результаты опроса:\n\n");
        for (Question question : questions) {
            results.append(question.getText()).append("\n");
            int userAnswerIndex = question.getUserAnswerIndex();
            if (userAnswerIndex != -1) {
                results.append("Ваш ответ: ").append(question.getAnswers()[userAnswerIndex]).append("\n");
                results.append("Баллы за ответ: ").append(question.getPointsForAnswer(userAnswerIndex)).append("\n\n");
            } else {
                results.append("Вы не ответили на этот вопрос\n\n");
            }
        }
        results.append("Итоговые баллы: ").append(totalPoints);

        JTextArea resultsTextArea = new JTextArea(results.toString(), 10, 30);
        resultsTextArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(resultsTextArea), "Результаты опроса", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PsychologySurveyApp app = new PsychologySurveyApp("Опрос по психологии");
                app.setVisible(true);
            }
        });
    }

    private static class Question {
        private String text;
        private String[] answers;
        private int[] points;

        private int userAnswerIndex = -1; // Default: No answer

        public Question(String text, String[] answers) {
            this.text = text;
            this.answers = answers;
            this.points = new int[answers.length];
            for (int i = 0; i < answers.length; i++) {
                this.points[i] = i + 1; // Assigning points based on answer index
            }
        }

        public String getText() {
            return text;
        }

        public String[] getAnswers() {
            return answers;
        }

        public int getPointsForAnswer(int index) {
            return points[index];
        }

        public int getUserAnswerIndex() {
            return userAnswerIndex;
        }

        public void setUserAnswerIndex(int userAnswerIndex) {
            this.userAnswerIndex = userAnswerIndex;
        }
    }
}


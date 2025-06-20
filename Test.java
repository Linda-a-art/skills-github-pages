import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 浮动文字类
class FloatingText {
    String text;
    int x, y;         // 位置
    int dx, dy;       // 速度
    Color color;      // 颜色
    Font font;        // 字体

    public FloatingText(String text, int x, int y, int dx, int dy, Color color, Font font) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.color = color;
        this.font = font;
    }

    // 更新位置
    public void update(int width, int height) {
        x += dx;
        y += dy;

        // 边界检测 - 碰到边界反弹
        if (x <= 0 || x >= width - 50) {
            dx = -dx;
        }
        if (y <= 20 || y >= height - 20) {
            dy = -dy;
        }
    }
}

// 浮动文字面板
class FloatingTextPanel extends JPanel {
    private List<FloatingText> texts = new ArrayList<>();
    private Timer timer;
    private Random random = new Random();

    public FloatingTextPanel() {
        // 初始化一些浮动文字
        initTexts();

        // 设置背景色
        setBackground(Color.BLACK);

        // 创建定时器，每30毫秒更新一次
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    updatePositions();
                    repaint();
                });
            }
        }, 0, 30); // 初始延迟0ms，周期30ms
    }

    // 初始化文字
    private void initTexts() {
        String[] words = {};
        Font[] fonts = {
                new Font("Arial", Font.BOLD, 16),
                new Font("Times New Roman", Font.ITALIC, 20),
                new Font("Courier New", Font.PLAIN, 18),
                new Font("Verdana", Font.BOLD, 22)
        };

        for (String word : words) {
            addText(word, fonts[random.nextInt(fonts.length)]);
        }
    }

    // 添加新的浮动文字
    public void addText(String text, Font font) {
        int widthBound = getWidth() - 200; // 增大间距
        if (widthBound <= 0) {
            widthBound = 200; // 给一个默认正数，避免 nextInt 参数为非正数
        }
        int x = random.nextInt(widthBound);

        int heightBound = getHeight() - 100; // 增大间距
        if (heightBound <= 0) {
            heightBound = 100;
        }
        int y = random.nextInt(heightBound) + 20;

        int dx = random.nextInt(3) - 1;
        int dy = random.nextInt(3) - 1;

        if (dx == 0 && dy == 0) {
            dx = 1;
        }

        Color color = new Color(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
        );

        texts.add(new FloatingText(text, x, y, dx, dy, color, font));
    }

    // 更新所有文字的位置
    private void updatePositions() {
        for (FloatingText text : texts) {
            text.update(getWidth(), getHeight());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 绘制所有浮动文字
        for (FloatingText text : texts) {
            g.setColor(text.color);
            g.setFont(text.font);
            g.drawString(text.text, text.x, text.y);
        }
    }
}

// 主类：单词考试系统
class WordExamSystem extends JFrame {
    // 模拟专业单词库（key:单词，value:释义 ），实际可从文件/Db加载
    private static final Map<String, String> WORD_LIBRARY = new HashMap<>();
    // 考试时长（分钟）
    private static final int EXAM_DURATION = 15;
    // 考试题量
    private static final int QUESTION_COUNT = 10;
    // 用户信息文件路径
    private static final String USER_INFO_FILE = "user_info.txt";

    // 静态初始化单词库
    static {
        // 尝试从文件加载单词库
        try {
            loadWordLibraryFromFile("word_library.txt");
        } catch (IOException e) {
            System.err.println("加载单词库文件失败，使用默认单词库: " + e.getMessage());
            // 使用默认单词库
            WORD_LIBRARY.put("abandon", "放弃；抛弃");
            WORD_LIBRARY.put("accelerate", "加速；促进");
            WORD_LIBRARY.put("benefit", "利益；好处");
            WORD_LIBRARY.put("capacity", "能力；容量");
            WORD_LIBRARY.put("diverse", "不同的；多种多样的");
            WORD_LIBRARY.put("efficient", "高效的；有能力的");
            WORD_LIBRARY.put("generate", "产生；生成");
            WORD_LIBRARY.put("highlight", "强调；突出");
            WORD_LIBRARY.put("illustrate", "说明；阐明");
            WORD_LIBRARY.put("justify", "证明...正确；为...辩护");
            WORD_LIBRARY.put("maintain", "维持；保持");
            WORD_LIBRARY.put("neglect", "忽视；疏忽");
            WORD_LIBRARY.put("optimize", "优化；使完善");
            WORD_LIBRARY.put("persist", "坚持；持续");
            WORD_LIBRARY.put("qualify", "使具备资格；限定");
            WORD_LIBRARY.put("relevant", "相关的；切题的");
            WORD_LIBRARY.put("stimulate", "刺激；激励");
            WORD_LIBRARY.put("temporary", "临时的；暂时的");
            WORD_LIBRARY.put("ultimate", "最终的；根本的");
            WORD_LIBRARY.put("validate", "验证；确认");
        }
    }

    // 从文件加载单词库
    private static void loadWordLibraryFromFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 格式：单词,释义
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    WORD_LIBRARY.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
    }

    // 从文件加载用户信息
    private static Map<String, String> loadUserInfo() {
        Map<String, String> userInfo = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_INFO_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    userInfo.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("加载用户信息文件失败: " + e.getMessage());
        }
        return userInfo;
    }

    public WordExamSystem() {
        // 1. 初始化窗口
        setTitle("英文单词考试系统 - 登录");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示

        // 2. 布局：使用 BorderLayout + 面板嵌套
        Container container = getContentPane();
        container.setLayout(new BorderLayout());

        // 2.1 顶部标题
        JLabel titleLabel = new JLabel("英文单词考试系统", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        container.add(titleLabel, BorderLayout.NORTH);

        // 2.2 中间登录表单（用户名、密码、按钮 ）
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel userLabel = new JLabel("用户名：");
        JTextField userField = new JTextField();

        JLabel pwdLabel = new JLabel("密码：");
        JPasswordField pwdField = new JPasswordField();

        JButton loginBtn = new JButton("登录");
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userField.getText();
                String password = new String(pwdField.getPassword());
                Map<String, String> userInfo = loadUserInfo();
                if (userInfo.containsKey(username) && userInfo.get(username).equals(password)) {
                    JOptionPane.showMessageDialog(WordExamSystem.this,
                            "登录成功！进入单词考试~",
                            "提示",
                            JOptionPane.INFORMATION_MESSAGE);
                    // 隐藏登录窗口，打开考试窗口
                    setVisible(false);
                    new ExamWindow(WORD_LIBRARY, EXAM_DURATION, QUESTION_COUNT, username).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(WordExamSystem.this,
                            "用户名或密码错误！",
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        formPanel.add(userLabel);
        formPanel.add(userField);
        formPanel.add(pwdLabel);
        formPanel.add(pwdField);
        formPanel.add(new JLabel()); // 占位，让按钮居中
        formPanel.add(loginBtn);

        container.add(formPanel, BorderLayout.CENTER);

        // 2.3 底部版本信息
        JLabel versionLabel = new JLabel("v1.0.0", SwingConstants.RIGHT);
        versionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
        container.add(versionLabel, BorderLayout.SOUTH);
    }

    // 考试窗口类
    static class ExamWindow extends JFrame {
        private final Map<String, String> wordLibrary;
        private final int examDuration;
        private final int questionCount;
        private final List<String> selectedWords;
        private final Map<String, Integer> userAnswers = new HashMap<>();
        private int currentQuestionIndex = 0;
        private Timer timer;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private JLabel timeLabel;
        private JLabel questionLabel;
        private JRadioButton[] optionButtons = new JRadioButton[4];
        private ButtonGroup optionGroup;
        private String username;

        public ExamWindow(Map<String, String> wordLibrary, int examDuration, int questionCount, String username) {
            this.wordLibrary = wordLibrary;
            this.examDuration = examDuration;
            this.questionCount = questionCount;
            this.username = username;

            // 随机选择指定数量的单词作为考试题目
            List<String> allWords = new ArrayList<>(wordLibrary.keySet());
            Collections.shuffle(allWords);
            selectedWords = allWords.subList(0, Math.min(questionCount, allWords.size()));

            initUI();
            startExamTimer();
        }

        private void initUI() {
            setTitle("英文单词考试系统 - 考试中");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setLocationRelativeTo(null);

            // 顶部：标题和倒计时
            JPanel topPanel = new JPanel(new BorderLayout());
            JLabel titleLabel = new JLabel("专业英语单词测试", SwingConstants.CENTER);
            titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
            topPanel.add(titleLabel, BorderLayout.NORTH);

            timeLabel = new JLabel("剩余时间: " + examDuration + ":00");
            timeLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            timeLabel.setForeground(Color.RED);
            timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            topPanel.add(timeLabel, BorderLayout.CENTER);
            add(topPanel, BorderLayout.NORTH);

            // 中部：题目和选项
            JPanel questionPanel = new JPanel(new BorderLayout(10, 10));
            questionPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

            questionLabel = new JLabel();
            questionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            questionPanel.add(questionLabel, BorderLayout.NORTH);

            JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
            optionGroup = new ButtonGroup();
            for (int i = 0; i < 4; i++) {
                optionButtons[i] = new JRadioButton();
                optionButtons[i].setFont(new Font("微软雅黑", Font.PLAIN, 14));
                optionButtons[i].addActionListener(e -> {
                    String currentWord = selectedWords.get(currentQuestionIndex);
                    userAnswers.put(currentWord, getSelectedOptionIndex());
                });
                optionGroup.add(optionButtons[i]);
                optionsPanel.add(optionButtons[i]);
            }
            questionPanel.add(optionsPanel, BorderLayout.CENTER);
            add(questionPanel, BorderLayout.CENTER);

            // 底部：导航按钮
            JPanel buttonPanel = new JPanel();
            JButton prevBtn = new JButton("上一题");
            JButton nextBtn = new JButton("下一题");
            JButton submitBtn = new JButton("提交试卷");

            prevBtn.addActionListener(e -> showPreviousQuestion());
            nextBtn.addActionListener(e -> showNextQuestion());
            submitBtn.addActionListener(e -> confirmSubmit());

            buttonPanel.add(prevBtn);
            buttonPanel.add(nextBtn);
            buttonPanel.add(submitBtn);
            add(buttonPanel, BorderLayout.SOUTH);

            // 显示第一题
            showCurrentQuestion();
        }

        private void startExamTimer() {
            startTime = LocalDateTime.now();
            endTime = startTime.plusMinutes(examDuration);

            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    LocalDateTime now = LocalDateTime.now();
                    if (now.isAfter(endTime)) {
                        timer.cancel();
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(ExamWindow.this,
                                    "考试时间已结束，系统将自动提交！",
                                    "提示",
                                    JOptionPane.INFORMATION_MESSAGE);
                            submitExam();
                        });
                    } else {
                        long remainingSeconds = java.time.Duration.between(now, endTime).getSeconds();
                        long minutes = remainingSeconds / 60;
                        long seconds = remainingSeconds % 60;
                        SwingUtilities.invokeLater(() ->
                                timeLabel.setText("剩余时间: " + String.format("%02d:%02d", minutes, seconds))
                        );
                    }
                }
            }, 0, 1000);
        }

        private void showCurrentQuestion() {
            if (currentQuestionIndex < 0 || currentQuestionIndex >= selectedWords.size()) {
                return;
            }

            String currentWord = selectedWords.get(currentQuestionIndex);
            questionLabel.setText("问题 " + (currentQuestionIndex + 1) + ": " + currentWord);

            // 清除之前的选择
            optionGroup.clearSelection();

            // 生成当前单词的选项（1个正确，3个干扰项）
            List<String> options = generateOptions(currentWord);
            for (int i = 0; i < 4; i++) {
                optionButtons[i].setText(options.get(i));
            }

            // 如果用户之前已经选择过答案，恢复选择状态
            if (userAnswers.containsKey(currentWord)) {
                int selectedIndex = userAnswers.get(currentWord);
                optionButtons[selectedIndex].setSelected(true);
            }
        }

        private List<String> generateOptions(String correctWord) {
            List<String> options = new ArrayList<>();
            options.add(wordLibrary.get(correctWord)); // 正确答案

            // 从单词库中随机选择3个不同的释义作为干扰项
            List<String> allMeanings = new ArrayList<>(wordLibrary.values());
            allMeanings.remove(wordLibrary.get(correctWord)); // 移除正确答案
            Collections.shuffle(allMeanings);

            // 添加3个干扰项
            for (int i = 0; i < 3 && i < allMeanings.size(); i++) {
                options.add(allMeanings.get(i));
            }

            // 打乱选项顺序
            Collections.shuffle(options);
            return options;
        }

        private int getSelectedOptionIndex() {
            for (int i = 0; i < 4; i++) {
                if (optionButtons[i].isSelected()) {
                    return i;
                }
            }
            return -1; // 没有选择任何选项
        }

        private void showPreviousQuestion() {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                showCurrentQuestion();
            }
        }

        private void showNextQuestion() {
            if (currentQuestionIndex < selectedWords.size() - 1) {
                currentQuestionIndex++;
                showCurrentQuestion();
            }
        }

        private void confirmSubmit() {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "确定要提交试卷吗？提交后将无法修改答案。",
                    "确认提交",
                    JOptionPane.YES_NO_OPTION
            );

            if (result == JOptionPane.YES_OPTION) {
                submitExam();
            }
        }

        private void submitExam() {
            timer.cancel();

            // 计算得分
            int score = 0;
            StringBuilder wrongAnswers = new StringBuilder();
            List<String> wrongWords = new ArrayList<>();

            for (String word : selectedWords) {
                int userAnswer = userAnswers.getOrDefault(word, -1);
                List<String> options = generateOptions(word);
                String correctMeaning = wordLibrary.get(word);

                if (userAnswer != -1 && options.get(userAnswer).equals(correctMeaning)) {
                    score++;
                } else {
                    wrongAnswers.append("\n单词: ").append(word)
                            .append("\n正确释义: ").append(correctMeaning)
                            .append("\n你的答案: ").append(userAnswer != -1 ? options.get(userAnswer) : "未作答")
                            .append("\n");
                    wrongWords.add(word + ": " + correctMeaning);
                }
            }

            // 显示结果
            StringBuilder message = new StringBuilder();
            message.append("考试完成！\n\n")
                    .append("总题数: ").append(selectedWords.size()).append("\n")
                    .append("答对题数: ").append(score).append("\n")
                    .append("得分: ").append(score * 10).append("/").append(selectedWords.size() * 10).append("\n\n");

            if (wrongAnswers.length() > 0) {
                message.append("错题分析:").append(wrongAnswers);
            }

            JOptionPane.showMessageDialog(
                    this,
                    message.toString(),
                    "考试结果",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // 显示浮动文字窗口
            if (!wrongWords.isEmpty()) {
                JFrame floatingTextFrame = new JFrame("错题正确答案浮动显示");
                floatingTextFrame.setSize(800, 600);
                floatingTextFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                floatingTextFrame.setLocationRelativeTo(null);

                FloatingTextPanel floatingTextPanel = new FloatingTextPanel();
                for (String wrongWord : wrongWords) {
                    Font font = new Font("SimHei", Font.BOLD, 16);
                    floatingTextPanel.addText(wrongWord, font);
                }
                floatingTextFrame.add(floatingTextPanel);
                floatingTextFrame.setVisible(true);
            }

            // 关闭当前窗口
            dispose();

            // 将成绩信息添加到排名列表
            RankingManager.addScore(username, score * 10);
        }
    }

    // 排名管理类
    static class RankingManager {
        private static List<Map.Entry<String, Integer>> rankingList = new ArrayList<>();

        public static void addScore(String username, int score) {
            rankingList.add(new AbstractMap.SimpleEntry<>(username, score));
            if (rankingList.size() == 3) {
                showRanking();
            }
        }

        private static void showRanking() {
            // 按分数降序排序
            rankingList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

            StringBuilder rankingMessage = new StringBuilder("考试排名：\n");
            for (int i = 0; i < rankingList.size(); i++) {
                Map.Entry<String, Integer> entry = rankingList.get(i);
                rankingMessage.append(i + 1).append(". ").append(entry.getKey()).append(": ").append(entry.getValue()).append("分\n");
            }

            JOptionPane.showMessageDialog(null, rankingMessage.toString(), "考试排名", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            executorService.submit(() -> SwingUtilities.invokeLater(() -> new WordExamSystem().setVisible(true)));
        }
        executorService.shutdown();
    }
}
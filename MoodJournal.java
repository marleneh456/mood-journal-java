import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;
import java.awt.geom.Path2D;

public class MoodJournal extends JFrame {
    private String mood = "Happy";
    private JTextArea noteArea;
    private DrawPanel drawPanel;
    private String formattedDate;
    private JLabel penLabel, eraserLabel, shapeLabel;
    private int penSize = 12;
    private int eraserSize = 42;
    private int shapeSize = 36;

    public MoodJournal() {
        setTitle("Daily Mood Journal");
        setSize(1400, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Date as MM/DD/YYYY
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        formattedDate = LocalDate.now().format(formatter);

        // --- Mood Section ---
        JPanel moodSection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel dateLabel = new JLabel("Date: " + formattedDate);
        JButton happyBtn = new JButton("😊 Happy");
        JButton okBtn = new JButton("😐 Okay");
        JButton sadBtn = new JButton("😢 Sad");
        happyBtn.addActionListener(e -> mood = "Happy");
        okBtn.addActionListener(e -> mood = "Okay");
        sadBtn.addActionListener(e -> mood = "Sad");
        moodSection.add(dateLabel);
        moodSection.add(Box.createHorizontalStrut(15));
        moodSection.add(happyBtn);
        moodSection.add(okBtn);
        moodSection.add(sadBtn);

        // --- Note Section ---
        JPanel noteSection = new JPanel(new BorderLayout());
        noteSection.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0)); // Padding left
        JLabel noteLabel = new JLabel("Your Note");
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.BOLD));
        noteSection.add(noteLabel, BorderLayout.NORTH);
        noteArea = new JTextArea(75, 55);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteSection.add(noteScroll, BorderLayout.CENTER);

        // --- Drawing Section ---
        JPanel drawSection = new JPanel(new BorderLayout());
        JLabel drawLabel = new JLabel("Draw your mood!");
        drawLabel.setFont(drawLabel.getFont().deriveFont(Font.BOLD));
        drawSection.add(drawLabel, BorderLayout.NORTH);
        drawPanel = new DrawPanel();
        drawPanel.setPreferredSize(new Dimension(700, 500));
        drawSection.add(drawPanel, BorderLayout.CENTER);

        // --- Right panel: Save button + New canvas ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(16, 10, 10, 10));
        JButton saveBtn = new JButton("Save Today's Mood");
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.addActionListener(e -> saveEntry());
        JButton newCanvasBtn = new JButton("New Canvas");
        newCanvasBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        newCanvasBtn.addActionListener(e -> drawPanel.clearCanvas());
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(saveBtn);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(newCanvasBtn);
        rightPanel.add(Box.createVerticalGlue());

        // --- Split Note and Drawing ---
        JPanel centerSection = new JPanel();
        centerSection.setLayout(new BorderLayout(10, 0));
        centerSection.add(noteSection, BorderLayout.WEST);
        centerSection.add(drawSection, BorderLayout.CENTER);
        centerSection.add(rightPanel, BorderLayout.EAST);

        // --- Toolbar with groups ---
        JPanel toolsSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));

        // Pen group
        JPanel penGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        addColorButton(penGroup, "Black", Color.BLACK);
        addColorButton(penGroup, "Red", Color.RED);
        addColorButton(penGroup, "Green", Color.GREEN);
        addColorButton(penGroup, "Blue", Color.BLUE);
        addColorButton(penGroup, "Orange", Color.ORANGE);
        addColorButton(penGroup, "Purple", new Color(128,0,128));
        addColorButton(penGroup, "Pink", Color.PINK);
        penLabel = new JLabel("Pen Size: " + penSize);
        penLabel.setFont(penLabel.getFont().deriveFont(Font.BOLD));
        JButton penPlus = new JButton("+"); penPlus.setMargin(new Insets(1,7,1,7));
        JButton penMinus = new JButton("-"); penMinus.setMargin(new Insets(1,7,1,7));
        penPlus.addActionListener(e -> { penSize=Math.min(30, penSize+2); penLabel.setText("Pen Size: "+penSize); drawPanel.setPenSize(penSize);});
        penMinus.addActionListener(e -> { penSize=Math.max(2, penSize-2); penLabel.setText("Pen Size: "+penSize); drawPanel.setPenSize(penSize);});
        penGroup.add(penLabel); penGroup.add(penPlus); penGroup.add(penMinus);

        // Eraser group
        JPanel eraserGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        addEraserButton(eraserGroup);
        eraserLabel = new JLabel("Eraser Size: " + eraserSize);
        eraserLabel.setFont(eraserLabel.getFont().deriveFont(Font.BOLD));
        JButton eraserPlus = new JButton("+"); eraserPlus.setMargin(new Insets(1,7,1,7));
        JButton eraserMinus = new JButton("-"); eraserMinus.setMargin(new Insets(1,7,1,7));
        eraserPlus.addActionListener(e -> { eraserSize=Math.min(50, eraserSize+4); eraserLabel.setText("Eraser Size: "+eraserSize); drawPanel.setEraserSize(eraserSize);});
        eraserMinus.addActionListener(e -> { eraserSize=Math.max(6, eraserSize-4); eraserLabel.setText("Eraser Size: "+eraserSize); drawPanel.setEraserSize(eraserSize);});
        eraserGroup.add(eraserLabel); eraserGroup.add(eraserPlus); eraserGroup.add(eraserMinus);

        // Shape group
        JPanel shapeGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        shapeLabel = new JLabel("Shape Size: " + shapeSize);
        shapeLabel.setFont(shapeLabel.getFont().deriveFont(Font.BOLD));
        JButton shapePlus = new JButton("+"); shapePlus.setMargin(new Insets(1,7,1,7));
        JButton shapeMinus = new JButton("-"); shapeMinus.setMargin(new Insets(1,7,1,7));
        shapePlus.addActionListener(e -> { shapeSize=Math.min(120, shapeSize+8); shapeLabel.setText("Shape Size: "+shapeSize); drawPanel.setShapeSize(shapeSize); });
        shapeMinus.addActionListener(e -> { shapeSize=Math.max(16, shapeSize-8); shapeLabel.setText("Shape Size: "+shapeSize); drawPanel.setShapeSize(shapeSize); });
        JButton heartBtn = new JButton("♥ Heart");
        JButton starBtn = new JButton("★ Star");
        JButton ovalBtn = new JButton("◯ Oval");
        heartBtn.addActionListener(e -> drawPanel.setTool("heart"));
        starBtn.addActionListener(e -> drawPanel.setTool("star"));
        ovalBtn.addActionListener(e -> drawPanel.setTool("oval"));
        shapeGroup.add(shapeLabel); shapeGroup.add(shapePlus); shapeGroup.add(shapeMinus);
        shapeGroup.add(heartBtn); shapeGroup.add(starBtn); shapeGroup.add(ovalBtn);

        // Paint bucket group (single button)
        JPanel bucketGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        JButton fillBtn = new JButton("Paint Bucket");
        fillBtn.addActionListener(e -> drawPanel.setTool("fill"));
        bucketGroup.add(fillBtn);

        // Add all groups to the toolbar
        toolsSection.add(penGroup);
        toolsSection.add(eraserGroup);
        toolsSection.add(shapeGroup);
        toolsSection.add(bucketGroup);

        // --- Add everything to the main frame ---
        setLayout(new BorderLayout(10,10));
        add(moodSection, BorderLayout.NORTH);
        add(centerSection, BorderLayout.CENTER);
        add(toolsSection, BorderLayout.SOUTH);

        setVisible(true);
    }

    // Helper to add color pen buttons
    private void addColorButton(JPanel panel, String name, Color color) {
        JButton btn = new JButton();
        btn.setBackground(color);
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setToolTipText(name + " Pen");
        btn.addActionListener(e -> { drawPanel.setPenColor(color); drawPanel.setTool("pen"); });
        panel.add(btn);
    }

    // Helper to add eraser
    private void addEraserButton(JPanel panel) {
        JButton eraserBtn = new JButton("Eraser");
        eraserBtn.setPreferredSize(new Dimension(90, 28));
        eraserBtn.setToolTipText("Eraser");
        eraserBtn.addActionListener(e -> drawPanel.setTool("eraser"));
        panel.add(eraserBtn);
    }

    private void saveEntry() {
        try {
            String folderName = "PersonalJournal";
            File folder = new File(folderName);
            if (!folder.exists()) {
                folder.mkdir();
            }
            String docFileName = folderName + File.separator + "MoodJournal_" + formattedDate.replace("/", "-") + ".doc";
            FileWriter writer = new FileWriter(docFileName);
            writer.write("Date: " + formattedDate + "\n");
            writer.write("Mood: " + mood + "\n\n");
            writer.write("Note:\n" + noteArea.getText() + "\n");
            writer.close();
            String imageName = folderName + File.separator + "MoodDrawing_" + formattedDate.replace("/", "-") + ".png";
            ImageIO.write(drawPanel.getImage(), "PNG", new File(imageName));
            JOptionPane.showMessageDialog(this, "Saved!\nText: " + docFileName + "\nDrawing: " + imageName);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage());
        }
    }

    // Drawing panel with pen/eraser/shape/fill
    private class DrawPanel extends JPanel {
        private BufferedImage image;
        private Graphics2D g2;
        private int prevX, prevY;
        private Color penColor = Color.BLACK;
        private int penSize = 12;
        private int eraserSize = 42;
        private int shapeSize = 36;
        private String tool = "pen";

        public DrawPanel() {
            image = new BufferedImage(700, 500, BufferedImage.TYPE_INT_ARGB);
            g2 = image.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, 700, 500);
            g2.setColor(penColor);
            g2.setStroke(new BasicStroke(penSize));
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    prevX = e.getX();
                    prevY = e.getY();
                    if (tool.equals("heart")) drawHeart(prevX, prevY, shapeSize, penColor);
                    if (tool.equals("star")) drawStar(prevX, prevY, shapeSize, penColor);
                    if (tool.equals("oval")) drawOval(prevX, prevY, shapeSize+20, shapeSize, penColor);
                    if (tool.equals("fill")) floodFill(image, prevX, prevY, penColor);
                    repaint();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    if (tool.equals("pen")) {
                        g2.setColor(penColor);
                        g2.setStroke(new BasicStroke(penSize));
                        g2.drawLine(prevX, prevY, x, y);
                    }
                    if (tool.equals("eraser")) {
                        g2.setColor(Color.WHITE);
                        g2.setStroke(new BasicStroke(eraserSize));
                        g2.drawLine(prevX, prevY, x, y);
                    }
                    prevX = x;
                    prevY = y;
                    repaint();
                }
            });
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null);
        }
        public BufferedImage getImage() {
            return image;
        }
        public void setPenColor(Color color) {
            penColor = color;
            g2.setColor(penColor);
        }
        public void setPenSize(int size) {
            penSize = size;
        }
        public void setEraserSize(int size) {
            eraserSize = size;
        }
        public void setShapeSize(int size) {
            shapeSize = size;
        }
        public void setTool(String t) {
            tool = t;
        }
        public void clearCanvas() {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2.setColor(penColor);
            repaint();
        }
        private void drawHeart(int x, int y, int size, Color color) {
            g2.setColor(color);
            double scale = size / 32.0;
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x, y + 8*scale);
            path.curveTo(x + 16*scale, y - 16*scale, x + 32*scale, y + 8*scale, x, y + 28*scale);
            path.curveTo(x - 32*scale, y + 8*scale, x - 16*scale, y - 16*scale, x, y + 8*scale);
            g2.fill(path);
        }
        private void drawStar(int x, int y, int size, Color color) {
            g2.setColor(color);
            int r = size/2;
            int points = 5;
            int[] xPoints = new int[10];
            int[] yPoints = new int[10];
            for (int i = 0; i < 10; i++) {
                double angle = Math.PI/2 + i * Math.PI / points;
                int len = (i%2==0) ? r : r/2;
                xPoints[i] = x + (int)(Math.cos(angle)*len);
                yPoints[i] = y - (int)(Math.sin(angle)*len);
            }
            g2.fillPolygon(xPoints, yPoints, 10);
        }
        private void drawOval(int x, int y, int w, int h, Color color) {
            g2.setColor(color);
            g2.fillOval(x - w/2, y - h/2, w, h);
        }
        private void floodFill(BufferedImage img, int x, int y, Color fillColor) {
            int targetColor = img.getRGB(x, y);
            int replaceColor = fillColor.getRGB();
            if (targetColor == replaceColor) return;
            int width = img.getWidth();
            int height = img.getHeight();
            boolean[][] visited = new boolean[width][height];
            Queue<Point> queue = new LinkedList<>();
            queue.add(new Point(x, y));
            while (!queue.isEmpty()) {
                Point p = queue.remove();
                int px = p.x, py = p.y;
                if (px < 0 || px >= width || py < 0 || py >= height) continue;
                if (visited[px][py]) continue;
                if (img.getRGB(px, py) != targetColor) continue;
                img.setRGB(px, py, replaceColor);
                visited[px][py] = true;
                queue.add(new Point(px+1, py));
                queue.add(new Point(px-1, py));
                queue.add(new Point(px, py+1));
                queue.add(new Point(px, py-1));
            }
        }
    }
    public static void main(String[] args) {
        new MoodJournal();
    }
}

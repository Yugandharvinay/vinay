
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Paint1 extends JFrame {
    private List<Point> points;
    private List<Shape> shapes;
    private Color currentColor = Color.BLACK;
    private ShapeType currentShape = ShapeType.DRAW;
    private boolean eraseMode = false;
    private BufferedImage image;
    private Graphics2D imageGraphics;
    private Stack<BufferedImage> undoStack;
    private Stack<BufferedImage> redoStack;
    private Path2D.Double currentPath;

    public Paint1() {
        setTitle("Durga Paint");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);

        points = new ArrayList<>();
        shapes = new ArrayList<>();
//        undoStack = new Stack<>();
        redoStack = new Stack<>();
        setupUI();
    }
    private void setupUI() {
        JPanel drawingPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null) {
                    g.drawImage(image, 0, 0, null);
                }
                drawPoints(g);
            }
        };
        drawingPanel.setBackground(Color.WHITE);

        drawingPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                points.add(e.getPoint());
                drawingPanel.repaint();
            }

            public void mouseReleased(MouseEvent e) {
                points.clear();
                if (currentShape == ShapeType.PEN) {
                    addCurrentPathToShapes();
                } else if (currentShape == ShapeType.PENCIL) {
                    addCurrentPointsToShapes();
                } else if (image != null) {
                    undoStack.push(copyImage(image));
                    redoStack.clear();
                }
            }
        });

        drawingPanel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                points.add(e.getPoint());
                if (currentShape == ShapeType.DRAW || currentShape == ShapeType.PEN || currentShape == ShapeType.PENCIL) {
                   if (currentPath == null && (currentShape == ShapeType.PEN || currentShape == ShapeType.PENCIL)) {
                        currentPath = new Path2D.Double();
                        currentPath.moveTo(e.getX(), e.getY());
                    } else if (currentPath != null) {
                        currentPath.lineTo(e.getX(), e.getY());
                    }
                }
                drawingPanel.repaint();
            }
        });

        add(drawingPanel);
        JPopupMenu shapesMenu = new JPopupMenu();
        JMenuItem circleMenuItem = new JMenuItem("Circle");
        JMenuItem rectangleMenuItem = new JMenuItem("Rectangle");
        JMenuItem lineMenuItem = new JMenuItem("Line");
        
        circleMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape = ShapeType.CIRCLE;
                eraseMode = false;
            }
        });

        rectangleMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape = ShapeType.RECTANGLE;
                eraseMode = false;
            }
        });

        lineMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape = ShapeType.LINE;
                eraseMode = false;
            }
        });

        shapesMenu.add(circleMenuItem);
        shapesMenu.add(rectangleMenuItem);
        shapesMenu.add(lineMenuItem);

        JButton shapesButton = new JButton("Shapes");
        shapesButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                shapesMenu.show(shapesButton, 0, shapesButton.getHeight());
            }
        });

        JButton colorButton = new JButton("Color");
        colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(Paint1.this, "Choose Color", currentColor);
                if (newColor != null) {
                    currentColor = newColor;
                }
            }
        });

        JButton eraseButton = new JButton("Erase");
        eraseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eraseMode = true;
                currentShape = ShapeType.DRAW;
                image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                imageGraphics = (Graphics2D) image.getGraphics();
                imageGraphics.setColor(Color.WHITE);
                imageGraphics.fillRect(0, 0, getWidth(), getHeight());
                drawingPanel.repaint();
            }
        });

        JButton pencilButton = new JButton("Pencil");
        pencilButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eraseMode = false;
                currentShape = ShapeType.PENCIL; 
            }
        });

        JButton penButton = new JButton("Pen");
        penButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eraseMode = false;
                currentShape = ShapeType.PEN; 
            }
        });

//        JButton undoButton = new JButton("Undo");
//        undoButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                if (!undoStack.isEmpty()) {
//                    redoStack.push(copyImage(image));
//                    image = undoStack.pop();
//                    imageGraphics = (Graphics2D) image.getGraphics();
//                    drawingPanel.repaint();
//                }
//            }
//        });

//        JButton redoButton = new JButton("Redo");
//        redoButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                if (!redoStack.isEmpty()) {
//                    undoStack.push(copyImage(image));
//                    image = redoStack.pop();
//                    imageGraphics = (Graphics2D) image.getGraphics();
//                    drawingPanel.repaint();
//                }
//            }
//        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(shapesButton);
        buttonsPanel.add(colorButton);
        buttonsPanel.add(eraseButton);
        buttonsPanel.add(pencilButton);
        buttonsPanel.add(penButton);
//        buttonsPanel.add(undoButton);
        //buttonsPanel.add(redoButton);
        add(buttonsPanel, BorderLayout.NORTH);
    }

    private BufferedImage copyImage(BufferedImage originalImage) {
        BufferedImage copiedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
        Graphics2D g = copiedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();
        return copiedImage;
    }
    private void drawPoints(Graphics g) {
        if (image == null) {
            image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            imageGraphics = (Graphics2D) image.getGraphics();
            imageGraphics.setColor(currentColor);
            imageGraphics.setStroke(new BasicStroke(2));
            imageGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(image, 0, 0, null);
        
        if (!eraseMode) {
            g2d.setColor(currentColor);
            switch (currentShape) {
                case CIRCLE:
                    g2d.drawOval(100, 100, 100, 100);
                    break;
                case RECTANGLE:
                    g2d.drawRect(200, 200, 100, 150);
                    break;
                case LINE:
                    g2d.drawLine(300, 300, 400, 400);
                    break;
                case PENCIL:
                    if (!points.isEmpty()) {
                        for (int i = 1; i < points.size(); i++) {
                            Point p1 = points.get(i - 1);
                            Point p2 = points.get(i);
                            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                            imageGraphics.drawLine(p1.x, p1.y, p2.x, p2.y);
                        }
                    }
                    break;
                case PEN:
                    if (currentPath != null) {
                        g2d.draw(currentPath);
                    }
                    break;
            }
        } else {
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(10));
            for (int i = 1; i < points.size(); i++) {
                Point p1 = points.get(i - 1);
                Point p2 = points.get(i);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                imageGraphics.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    private void addCurrentPathToShapes() {
        if (currentPath != null) {
            shapes.add(currentPath);
            currentPath = null; 
        }
    }

    private void addCurrentPointsToShapes() {
        if (!points.isEmpty()) {
            List<Point> copiedPoints = new ArrayList<>(points);
        }
    }

    private enum ShapeType {
        DRAW, CIRCLE, RECTANGLE, LINE, PENCIL, PEN
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Paint1();
        });
    }
}


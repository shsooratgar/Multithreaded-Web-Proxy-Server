import javax.swing.*;
import java.awt.*;

public class AdminFrame extends JFrame {
    private JScrollPane scrollPane;
    private JList filter;
    private JPanel panel;
    public AdminFrame()  {
        super("Java Proxy");
        setSize(700,600);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
        setLayout(new BorderLayout());
        addComponents();
        setVisible(true);
    }

    private void addComponents() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top,BoxLayout.X_AXIS));
        top.add(new JLabel("Filtered addresses"));
        JButton addFilter = new JButton("Add Filter");
        JButton removeFilter = new JButton("Remove Filter");
        top.add(addFilter);
        top.add(removeFilter);

        filter = new JList(Proxy.getInstance().getFilters().toArray());
        scrollPane = new JScrollPane(filter);
        panel.add(scrollPane,BorderLayout.CENTER);
        addFilter.addActionListener(I->{
            String newFilter = JOptionPane.showInputDialog("Enter url of blocking website (Without http(s)://www.):");
            if(newFilter != null && !newFilter.isEmpty() && !newFilter.isBlank())
            Proxy.getInstance().getFilters().add(newFilter);
            updateList();
        });

        removeFilter.addActionListener(I->{
            String s = (String)filter.getSelectedValue();
            Proxy.getInstance().getFilters().remove(s);
            updateList();
        });
        panel.add(top,BorderLayout.NORTH);
        add(panel,BorderLayout.CENTER);
    }

    private void updateList() {
        panel.remove(scrollPane);
        filter = new JList(Proxy.getInstance().getFilters().toArray());
        scrollPane = new JScrollPane(filter);
        panel.add(scrollPane,BorderLayout.CENTER);
        repaint();
        revalidate();
    }

}

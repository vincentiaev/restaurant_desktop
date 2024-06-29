import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class Menu extends JFrame {
    private JButton main_bt;
    private JButton order_bt;
    private JButton menu_bt;
    private JPanel menu_panel;
    private JList<String> menuList;
    private JTextField menu_tf;
    private JTextField deskripsi_tf;
    private JTextField harga_tf;
    private JButton updateButton;
    private JButton deleteButton;
    private JTextField gambar_tf;
    private JButton addButton;
    private JButton browseButton;
    private JScrollPane menuScroll;
    private JLabel gambarView;
    private JTextField fileName;
    public static DefaultListModel<String> menuListModel;

    private String lastSelectedMenu = null;

    public Menu() {
        setTitle("Menu");
        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        setContentPane(menu_panel);

        gambarView.setPreferredSize(new Dimension(200, 100)); // Set preferred size as needed

        menuScroll.setViewportView(menuList);
        menuScroll.setPreferredSize(new Dimension(600, 1));

        menuListModel = new DefaultListModel<>();
        menuList.setModel(menuListModel);

        order_bt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Order orderFrame = new Order();
                orderFrame.setVisible(true);
                setVisible(false);
            }
        });
        main_bt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main mainFrame = new Main();
                mainFrame.setVisible(true);
                setVisible(false);
            }
        });

        try {
            getAllMenu();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        menuList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = menuList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String selectedMenu = menuListModel.getElementAt(index);
                        if (selectedMenu.equals(lastSelectedMenu)) {
                            menuList.clearSelection();
                            lastSelectedMenu = null;
                            menu_tf.setText("");
                            deskripsi_tf.setText("");
                            harga_tf.setText("");
                            fileName.setText("");
                            gambarView.setIcon(null);
                        } else {
                            lastSelectedMenu = selectedMenu;

                            String nama_menu = extractData(selectedMenu, "Nama Menu: ");
                            String deskripsi = extractData(selectedMenu, "Deskripsi: ");
                            String harga = extractData(selectedMenu, "Harga: ");
                            String gambar = extractData(selectedMenu, "Gambar: ");

                            menu_tf.setText(nama_menu);
                            deskripsi_tf.setText(deskripsi);
                            harga_tf.setText(harga);

                            if (!gambar.isEmpty()) {
                                String imageUrl = "http://localhost:8000/" + gambar;
                                System.out.println(imageUrl);
                                try {
                                    URL url = new URL(imageUrl);
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                    conn.setRequestMethod("GET");
                                    conn.setRequestProperty("User-Agent", "Chrome/51.0.2704.103");

                                    int responseCode = conn.getResponseCode();
                                    if (responseCode == 200) {
                                        BufferedImage originalImage = ImageIO.read(conn.getInputStream());
                                        Image scaledImage = originalImage.getScaledInstance(gambarView.getPreferredSize().width, gambarView.getPreferredSize().height, Image.SCALE_SMOOTH);
                                        ImageIcon imageIcon = new ImageIcon(scaledImage);
                                        gambarView.setIcon(imageIcon);
                                    } else {
                                        gambarView.setIcon(null);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    gambarView.setIcon(null);
                                }
                            } else {
                                gambarView.setIcon(null);
                            }
                        }
                    }
                }
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (lastSelectedMenu == null) {
                    JOptionPane.showMessageDialog(Menu.this, "Please select a menu");
                } else {
                    String nama_menu = menu_tf.getText();
                    String deskripsi = deskripsi_tf.getText();
                    int harga = Integer.parseInt(harga_tf.getText());
                    String nama_file = fileName.getText();
                    File gambarFile = null;
                    boolean isGambarUpdated = !nama_file.isEmpty();

                    if (isGambarUpdated) {
                        gambarFile = new File(nama_file);
                    }

                    try {
                        int responseCode = updateMenu(nama_menu, deskripsi, harga, gambarFile, isGambarUpdated);
                        System.out.println(responseCode);
                        if (responseCode == 200) {
                            JOptionPane.showMessageDialog(Menu.this, "Menu updated");
                            menuListModel.clear();
                            getAllMenu();
                            menu_tf.setText("");
                            deskripsi_tf.setText("");
                            harga_tf.setText("");
                            fileName.setText("");
                            gambarView.setIcon(null);
                        } else {
                            JOptionPane.showMessageDialog(Menu.this, "Update failed");
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (lastSelectedMenu == null) {
                    JOptionPane.showMessageDialog(Menu.this, "Please select a menu");
                } else {
                    JPanel panel = new JPanel();

                    panel.setSize(new Dimension(250, 100));
                    panel.setLayout(null);
                    JLabel label1 = new JLabel("Delete this menu?");
                    label1.setVerticalAlignment(SwingConstants.BOTTOM);
                    label1.setBounds(20, 20, 150, 15);
                    label1.setHorizontalAlignment(SwingConstants.CENTER);
                    panel.add(label1);
                    UIManager.put("OptionPane.minimumSize", new Dimension(300, 100));

                    int res = JOptionPane.showConfirmDialog(null, panel, "Delete menu",
                            JOptionPane.YES_NO_OPTION);
                    if (res == 0) {
                        System.out.println("Pressed YES");
                        try {
                            JOptionPane.showMessageDialog(Menu.this, "Menu deleted");
                            deleteMenu();
                            menuListModel.clear();
                            getAllMenu();
                            menu_tf.setText("");
                            deskripsi_tf.setText("");
                            harga_tf.setText("");
                            fileName.setText("");
                            gambarView.setIcon(null);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    } else if (res == 1) {
                        //System.out.println("Pressed NO");
                    } else {
                        //System.out.println("Pressed CANCEL");
                    }
                }
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nama_menu = menu_tf.getText();
                String deskripsi = deskripsi_tf.getText();
                String hargaText = harga_tf.getText();
                String nama_file = fileName.getText();

                if (nama_menu.isEmpty()) {
                    JOptionPane.showMessageDialog(Menu.this, "Nama menu harus diisi");
                    return;
                }
                if (deskripsi.isEmpty()) {
                    JOptionPane.showMessageDialog(Menu.this, "Deskripsi harus diisi");
                    return;
                }
                if (hargaText.isEmpty()) {
                    JOptionPane.showMessageDialog(Menu.this, "Harga harus diisi");
                    return;
                }
                if (nama_file.isEmpty()) {
                    JOptionPane.showMessageDialog(Menu.this, "Gambar harus diisi");
                    return;
                }
                int harga;
                try {
                    harga = Integer.parseInt(hargaText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(Menu.this, "Harga harus berupa angka");
                    return;
                }
                try {
                    int responseCode = addMenu(nama_menu, deskripsi, harga, new File(nama_file));
                    if (responseCode == 200) {
                        JOptionPane.showMessageDialog(Menu.this, "Menu added");
                        menuListModel.clear();
                        getAllMenu();
                        menu_tf.setText("");
                        deskripsi_tf.setText("");
                        harga_tf.setText("");
                        fileName.setText("");
                    } else {
                        JOptionPane.showMessageDialog(Menu.this, "Add menu failed");
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(Menu.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    fileName.setText(selectedFile.getAbsolutePath());
                }
            }
        });
    }

    public void getAllMenu() throws Exception {

        String url = "http://localhost:8000/menu";
        //System.out.println(url);
        URL obj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Chrome/51.0.2704.103");
        int responseCode = conn.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );
        String r;
        StringBuffer response = new StringBuffer();
        while ((r = in.readLine()) != null) {
            response.append(r);
        }
        in.close();
        System.out.println("Received data: \n" + response.toString());

        JSONObject jsonObject = new JSONObject(response.toString());
        JSONArray responseArray = jsonObject.getJSONArray("response");

        StringBuilder menuDetails = new StringBuilder();

        for (int i = 0; i < responseArray.length(); i++) {
            JSONObject menu = responseArray.getJSONObject(i);
            int menuId = menu.getInt("menu_id");
            String nama_menu = menu.getString("nama_menu");
            String deskripsi = menu.getString("deskripsi");
            int harga = menu.getInt("harga");
            String gambar = menu.getString("gambar");

            String menuInfo = "Menu ID: " + menuId + " \nNama Menu: " + nama_menu + " \nDeskripsi: " + deskripsi + " \nHarga: " + harga + " \nGambar: " + gambar;
            menuListModel.addElement(menuInfo);
        }
    }

    private static String extractData(String data, String key) {
        int startIndex = data.indexOf(key) + key.length();
        int endIndex = data.indexOf('\n', startIndex);

        if (endIndex == -1) {
            endIndex = data.length();
        }

        return data.substring(startIndex, endIndex).trim();
    }

    public int updateMenu(String nama_menu, String deskripsi, int harga, File gambar, boolean isGambarUpdated) throws Exception {
        String selectedMenuInfo = menuList.getSelectedValue();
        int menuId = getMenuIDFromInfo(selectedMenuInfo);
        String url = "http://localhost:8000/menu/" + menuId;

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("User-Agent", "Chrome/51.0.2704.103");
        conn.setDoOutput(true);

        // Boundary for multipart/form-data request
        String boundary = Long.toHexString(System.currentTimeMillis());
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream output = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {

            // Send text fields
            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"nama_menu\"").append("\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8").append("\r\n\r\n");
            writer.append(nama_menu).append("\r\n").flush();

            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"deskripsi\"").append("\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8").append("\r\n\r\n");
            writer.append(deskripsi).append("\r\n").flush();

            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"harga\"").append("\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8").append("\r\n\r\n");
            writer.append(Integer.toString(harga)).append("\r\n").flush();

            // Send binary file if a new image is provided
            if (isGambarUpdated && gambar != null) {
                writer.append("--" + boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"gambar\"; filename=\"" + gambar.getName() + "\"").append("\r\n");
                writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(gambar.getName())).append("\r\n");
                writer.append("Content-Transfer-Encoding: binary").append("\r\n\r\n").flush();

                try (InputStream input = new FileInputStream(gambar)) {
                    byte[] buffer = new byte[1024];
                    for (int length; (length = input.read(buffer)) > 0; ) {
                        output.write(buffer, 0, length);
                    }
                    output.flush();
                }

                writer.append("\r\n").flush();
            }

            writer.append("--" + boundary + "--").append("\r\n").flush();
        }

        int responseCode = conn.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return responseCode;
    }



    public void deleteMenu() throws Exception {
        String selectedOrderInfo = menuList.getSelectedValue();
        System.out.println("selecetedorder:" + selectedOrderInfo);
        int menuId = getMenuIDFromInfo(selectedOrderInfo);
        String url = "http://localhost:8000/menu/" + menuId;

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("User-Agent", "Chrome/51.0.2704.103");
        conn.setRequestProperty("Content-Type", "application/json");

        int responseCode = conn.getResponseCode();
        //System.out.println("Send DELETE request: " + url);
        //System.out.println("Response code: " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //System.out.println("Received data: \n" + response.toString());
    }

    public static int getMenuIDFromInfo(String menuInfo) {
        int startIndex = menuInfo.indexOf("Menu ID:") + 8;
        int endIndex = menuInfo.indexOf("\n", startIndex);
        String menuIDString = menuInfo.substring(startIndex, endIndex).trim();
        return Integer.parseInt(menuIDString);
    }

    public int addMenu(String nama_menu, String deskripsi, int harga, File gambar) throws Exception {
        String url = "http://localhost:8000/menu";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Chrome/51.0.2704.103");
        conn.setDoOutput(true);

        // Boundary for multipart/form-data request
        String boundary = Long.toHexString(System.currentTimeMillis());
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream output = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {

            // Send text fields
            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"nama_menu\"").append("\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8").append("\r\n\r\n");
            writer.append(nama_menu).append("\r\n").flush();

            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"deskripsi\"").append("\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8").append("\r\n\r\n");
            writer.append(deskripsi).append("\r\n").flush();

            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"harga\"").append("\r\n");
            writer.append("Content-Type: text/plain; charset=UTF-8").append("\r\n\r\n");
            writer.append(Integer.toString(harga)).append("\r\n").flush();

            // Send binary file
            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"gambar\"; filename=\"" + gambar.getName() + "\"").append("\r\n");
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(gambar.getName())).append("\r\n");
            writer.append("Content-Transfer-Encoding: binary").append("\r\n\r\n").flush();

            try (InputStream input = new FileInputStream(gambar)) {
                byte[] buffer = new byte[1024];
                for (int length; (length = input.read(buffer)) > 0; ) {
                    output.write(buffer, 0, length);
                }
                output.flush();
            }

            writer.append("\r\n").flush();
            writer.append("--" + boundary + "--").append("\r\n").flush();
        }

        int responseCode = conn.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return responseCode;
    }

}

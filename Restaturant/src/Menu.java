import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class Menu extends JFrame{
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
    public static DefaultListModel<String> menuListModel;

    private String lastSelectedMenu = null;

    public Menu(){
        setTitle("Menu");
        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        setContentPane(menu_panel);

        menuScroll.setViewportView(menuList);
        menuScroll.setPreferredSize(new Dimension(1200, 1));


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
                        //System.out.println(selectedMenu);
                        if (selectedMenu.equals(lastSelectedMenu)) {
                            menuList.clearSelection();
                            lastSelectedMenu = null;
                            menu_tf.setText("");
                            deskripsi_tf.setText("");
                            harga_tf.setText("");
                        } else {
                            lastSelectedMenu = selectedMenu;

                            String nama_menu = extractData(selectedMenu, "Nama Menu: ");
                            String deskripsi = extractData(selectedMenu, "Deskripsi: ");
                            String harga = extractData(selectedMenu, "Harga: ");

                            menu_tf.setText(nama_menu);
                            deskripsi_tf.setText(deskripsi);
                            harga_tf.setText(harga);
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

                    System.out.println("Nama Menu: " + nama_menu);
                    System.out.println("Deskripsi: " + deskripsi);
                    System.out.println("Harga: " + harga);

                    try {
                        int responseCode = updateMenu(nama_menu, deskripsi, harga);
                        System.out.println(responseCode);
                        if (responseCode == 200) {
                            JOptionPane.showMessageDialog(Menu.this, "Menu updated");
                            menuListModel.clear();
                            getAllMenu();
                            menu_tf.setText("");
                            deskripsi_tf.setText("");
                            harga_tf.setText("");
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
                    JOptionPane.showMessageDialog(Menu.this, "Please select a nemu");
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
                    if(res == 0) {
                        System.out.println("Pressed YES");
                        try {
                            JOptionPane.showMessageDialog(Menu.this, "Menu deleted");
                            deleteMenu();
                            menuListModel.clear();
                            getAllMenu();
                            menu_tf.setText("");
                            deskripsi_tf.setText("");
                            harga_tf.setText("");
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
                String gambar = gambar_tf.getText();

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
                int harga;
                try {
                    harga = Integer.parseInt(hargaText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(Menu.this, "Harga harus berupa angka");
                    return;
                }
                try {
                    int responseCode = addMenu(nama_menu, deskripsi, harga, gambar);
                    if (responseCode == 200) {
                        JOptionPane.showMessageDialog(Menu.this, "Menu added");
                        menuListModel.clear();
                        getAllMenu();
                        menu_tf.setText("");
                        deskripsi_tf.setText("");
                        harga_tf.setText("");
                    } else {
                        JOptionPane.showMessageDialog(Menu.this, "Add menu failed");
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
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

            String menuInfo = "Menu ID: " + menuId + " \nNama Menu: " + nama_menu + " \nDeskripsi: " + deskripsi + " \nHarga: " + harga;
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

    public int updateMenu(String nama_menu, String deskripsi, int harga) throws Exception {
        String selectedMenuIndo = menuList.getSelectedValue();
        int menuId = getMenuIDFromInfo(selectedMenuIndo);
        String url = "http://localhost:8000/menu/" + menuId;

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("User-Agent", "Chrome/51.0.2704.103");
        conn.setRequestProperty("Content-Type", "application/json");

        String editedMenu = "{\"nama_menu\": \"" + nama_menu + "\", " + "\"deskripsi\": \"" + deskripsi + "\", " + "\"harga\": " + harga + "}";

        // Mengirim data ke server
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(editedMenu.getBytes());
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        //System.out.println("Send PUT request: " + url);
        //System.out.println("Response code: " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //System.out.println("Received data: \n" + response.toString());

        return responseCode;
    }

    public void deleteMenu() throws Exception {
        String selectedOrderInfo = menuList.getSelectedValue();
        System.out.println("selecetedorder:"+selectedOrderInfo);
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


    public static int getMenuIDFromInfo(String orderInfo) {
        int startIndex = orderInfo.indexOf("Menu ID:") + 8;
        int endIndex = orderInfo.indexOf("\n", startIndex);
        String menuIDString = orderInfo.substring(startIndex, endIndex).trim();
        return Integer.parseInt(menuIDString);
    }

    public int addMenu(String nama_menu, String deskripsi, int harga, String gambar) throws Exception {
        String url = "http://localhost:8000/menu";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Chrome/51.0.2704.103");
        conn.setRequestProperty("Content-Type", "application/json");

        String newMenu = "{\"nama_menu\": \"" + nama_menu + "\", " + "\"deskripsi\": \"" + deskripsi + "\", " + "\"harga\": " + harga + ", " + "\"gambar\": \"" + gambar + "\"}";


        // Mengirim data ke server
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(newMenu.getBytes());
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //System.out.println("Received data: \n" + response.toString());
        return responseCode;
    }

}

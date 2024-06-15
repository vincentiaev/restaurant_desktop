import org.json.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Order extends JFrame {
    private JPanel order_panel;
    private JButton main_bt;
    private JButton order_bt;
    private JButton menu_bt;
    public JList<String> confirmList; // Updated: Added generic type String
    private JPanel list_panel;
    private JPanel button_panel;
    private JLabel konf_label;
    private JLabel dimasak_label;
    private JComboBox<String> statusFilterComboBox; // Updated: Added generic type String
    private JLabel perjalan_label;
    private JLabel selesai_label;
    private JLabel total_order_label;
    private JTextArea orderDetailText;
    private JButton dimasak_btn;
    private JButton perjalan_btn;
    private JButton selesai_btn;
    private JScrollPane orderScroll;
    private JLabel date_label;
    public static DefaultListModel<String> confirmListModel; // Updated: Added generic type String
    private String lastSelectedOrder = null;


    public Order() {
        setTitle("Order");
        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        setContentPane(order_panel);

        orderScroll.setViewportView(confirmList);

        // Initialize the list model and list
        confirmListModel = new DefaultListModel<>();
        confirmList.setModel(confirmListModel);

        orderDetailText.setEditable(false);


        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDateTime now = LocalDateTime.now();
        date_label.setText(dtf.format(now));

        confirmList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = confirmList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String selectedOrder = confirmListModel.getElementAt(index);
                        //System.out.println(selectedOrder);
                        if (selectedOrder.equals(lastSelectedOrder)) {
                            confirmList.clearSelection();
                            lastSelectedOrder = null;
                            orderDetailText.setText("");
                        } else {
                            lastSelectedOrder = selectedOrder;

                            orderDetailText.setText(selectedOrder);
                        }
                    }
                }
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
        menu_bt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Menu menuFrame = new Menu();
                menuFrame.setVisible(true);
                setVisible(false);
            }
        });

        statusFilterComboBox.addItem("All");
        statusFilterComboBox.addItem("Menunggu Konfirmasi");
        statusFilterComboBox.addItem("Sedang Dimasak");
        statusFilterComboBox.addItem("Dalam Perjalanan");
        statusFilterComboBox.addItem("Pesanan selesai");

        statusFilterComboBox.setSelectedItem("All");

        statusFilterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String status_filter = (String) statusFilterComboBox.getSelectedItem();
                //System.out.println(status_filter);
                if (status_filter.equals("All")) {
                    try {
                        getAllOrder();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    try {
                        getOrderByStatus(status_filter);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        dimasak_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lastSelectedOrder == null) {
                    JOptionPane.showMessageDialog(Order.this, "Please select an order");
                } else {
                    try {
                        int responseCode = updateStatus("Sedang dimasak");
                        if (responseCode == 200) {
                            JOptionPane.showMessageDialog(Order.this, "Status updated");
                        } else {
                            JOptionPane.showMessageDialog(Order.this, "Status update failed");
                        }
                        refreshOrderList();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }

            }
        });
        perjalan_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lastSelectedOrder == null) {
                    JOptionPane.showMessageDialog(Order.this, "Please select an order");
                } else {
                    try {
                        int responseCode = updateStatus("Dalam perjalanan");
                        if (responseCode == 200) {
                            JOptionPane.showMessageDialog(Order.this, "Status updated");
                        } else {
                            JOptionPane.showMessageDialog(Order.this, "Status update failed");
                        }
                        refreshOrderList();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }

            }
        });
        selesai_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lastSelectedOrder == null) {
                    JOptionPane.showMessageDialog(Order.this, "Please select an order");
                } else {
                    try {
                        int responseCode = updateStatus("Pesanan selesai");
                        if (responseCode == 200) {
                            JOptionPane.showMessageDialog(Order.this, "Status updated");
                        } else {
                            JOptionPane.showMessageDialog(Order.this, "Status update failed");
                        }
                        refreshOrderList();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }

            }
        });

        try {
            getAllOrder();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(Order.this, "Failed to load orders: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshOrderList() {
        String status_filter = (String) statusFilterComboBox.getSelectedItem();
        try {
            if (status_filter.equals("All")) {
                orderDetailText.setText("");
                getAllOrder();
            } else {
                orderDetailText.setText("");
                getOrderByStatus(status_filter);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void getOrderByStatus(String status_filter) throws Exception {
        confirmListModel.clear();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();

        String statusFilter = status_filter;
        String encodedString = statusFilter.replace(" ", "%20");

        String url = "http://localhost:8000/order/" + dtf.format(now) + "/" + encodedString;
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
        //System.out.println("Received data: \n" + response.toString());

        JSONObject jsonObject = new JSONObject(response.toString());
        JSONArray responseArray = jsonObject.getJSONArray("response");

        Map<Integer, StringBuilder> orderMap = new HashMap<>();

        //System.out.println(responseArray.length());

        for (int i = 0; i < responseArray.length(); i++) {
            JSONObject order = responseArray.getJSONObject(i);
            int orderId = order.getInt("order_id");
            String orderDetail = order.getString("order_detail");
            String orderStatus = order.getString("status");
            String alamatPengiriman = order.getString("alamat_pengiriman");
            int totalHarga = order.getInt("total_harga");

            // Parsing order_detail
            JSONObject orderDetailJson = new JSONObject(orderDetail);
            Iterator<String> keys = orderDetailJson.keys();
            StringBuilder orderDetails = new StringBuilder();

            while (keys.hasNext()) {
                String menuId = keys.next();
                int quantity = orderDetailJson.getInt(menuId);

                String nama_menu = getMenuByID(menuId);

                orderDetails.append(" ").append(nama_menu).append(": ").append(quantity).append(" \n");
            }

            orderDetails.append(" Total: ").append(totalHarga).append("\n").append(" Alamat: ").append(alamatPengiriman).append("\n").append(" Status: ").append(orderStatus).append("\n");
            orderMap.put(orderId, orderDetails);


        }

        for (int orderId : orderMap.keySet()) {
            StringBuilder orderDetails = orderMap.get(orderId);
            String orderInfo = " Order ID: " + orderId + "\n" + orderDetails.toString();
            confirmListModel.addElement(orderInfo);
        }
    }

    public void getAllOrder() throws Exception {
        confirmListModel.clear();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();


        String url = "http://localhost:8000/order/date/" + dtf.format(now);
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
        //System.out.println("Received data: \n" + response.toString());

        JSONObject jsonObject = new JSONObject(response.toString());
        JSONArray responseArray = jsonObject.getJSONArray("response");

        int konfCount = 0;
        int dimasakCount = 0;
        int perjalanCount = 0;
        int selesaiCount = 0;

        Map<Integer, StringBuilder> orderMap = new HashMap<>();

        total_order_label.setText(String.valueOf(responseArray.length()));
        //System.out.println(responseArray.length());

        for (int i = 0; i < responseArray.length(); i++) {
            JSONObject order = responseArray.getJSONObject(i);
            int orderId = order.getInt("order_id");
            String orderDetail = order.getString("order_detail");
            String orderStatus = order.getString("status");
            String alamatPengiriman = order.getString("alamat_pengiriman");
            int totalHarga = order.getInt("total_harga");

            switch (orderStatus) {
                case "Menunggu konfirmasi":
                    konfCount++;
                    break;
                case "Sedang dimasak":
                    dimasakCount++;
                    break;
                case "Dalam perjalanan":
                    perjalanCount++;
                    break;
                case "Pesanan selesai":
                    selesaiCount++;
                    break;
            }

            JSONObject orderDetailJson = new JSONObject(orderDetail);
            Iterator<String> keys = orderDetailJson.keys();
            StringBuilder orderDetails = new StringBuilder();

            while (keys.hasNext()) {
                String menuId = keys.next();
                int quantity = orderDetailJson.getInt(menuId);

                String nama_menu = getMenuByID(menuId);

                orderDetails.append(" ").append(nama_menu).append(": ").append(quantity).append(" \n");
            }

            orderDetails.append(" Total: ").append(totalHarga).append("\n").append(" Alamat: ").append(alamatPengiriman).append("\n").append(" Status: ").append(orderStatus).append("\n");

            orderMap.put(orderId, orderDetails);


        }

        konf_label.setText(String.valueOf(konfCount));
        dimasak_label.setText(String.valueOf(dimasakCount));
        perjalan_label.setText(String.valueOf(perjalanCount));
        selesai_label.setText(String.valueOf(selesaiCount));

        for (int orderId : orderMap.keySet()) {
            StringBuilder orderDetails = orderMap.get(orderId);
            String orderInfo = " Order ID: " + orderId + " \n" + orderDetails.toString();
            confirmListModel.addElement(orderInfo);
        }
    }

    public static String getMenuByID(String menuId) throws Exception {
        String url = "http://localhost:8000/menu/" + menuId;
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

        JSONObject jsonResponse = new JSONObject(response.toString());
        String menuName = jsonResponse.getJSONObject("response").getString("nama_menu");

        return menuName;
    }

    public int updateStatus(String status) throws Exception {
        String selectedOrderInfo = confirmList.getSelectedValue(); // Mendapatkan item yang dipilih dari JList
        int orderId = getOrderIDFromInfo(selectedOrderInfo); // Mendapatkan ID pesanan dari info yang dipilih
        String url = "http://localhost:8000/order/" + orderId; // Menggunakan URL yang sesuai untuk mengubah status pesanan

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("User-Agent", "Chrome/51.0.2704.103");
        conn.setRequestProperty("Content-Type", "application/json");

        String newStatus = "{\"status\": \"" + status + "\"}";

        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(newStatus.getBytes());
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

    public static int getOrderIDFromInfo(String orderInfo) {
        int startIndex = orderInfo.indexOf("Order ID:") + 9;
        int endIndex = orderInfo.indexOf("\n", startIndex);
        String orderIdString = orderInfo.substring(startIndex, endIndex);
        return Integer.parseInt(orderIdString.trim());
    }

    public static void main(String[] args) {
        new Order();

    }


}
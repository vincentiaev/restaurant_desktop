
import jdk.nashorn.internal.scripts.JO;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Main extends JFrame{

    private JButton main_bt;
    private JButton order_bt;
    private JButton menu_bt;
    private JPanel main_panel;
    private JComboBox tanggal;
    private JComboBox bulan;
    private JComboBox tahun;
    private JButton searchButton;
    private JList<String> historyOrderList;
    private JTextArea histOrderDetail;
    private JScrollPane scroll;
    public static DefaultListModel<String> histOrderListModel;

    public Main(){
        setTitle("Order History");
        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        setContentPane(main_panel);

        histOrderListModel = new DefaultListModel<>();
        historyOrderList.setModel(histOrderListModel);

        scroll.setViewportView(historyOrderList);

        histOrderDetail.setEditable(false);

        historyOrderList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = historyOrderList.locationToIndex(e.getPoint());

                    if (index >= 0) {
                        String selectedOrder = histOrderListModel.getElementAt(index);
                        //System.out.println(selectedOrder);
                        histOrderDetail.setText(selectedOrder);
                    }
                }
            }
        });

        order_bt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Order orderFrame = new Order();
                orderFrame.setVisible(true);
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



        for (int i = 1; i <= 31; i++) {
            tanggal.addItem(String.valueOf(i));
        }
        String[] months = {"Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        for (String month : months) {
            bulan.addItem(month);
        }
        tahun.addItem("2024");

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tanggal_dipilih = tanggal.getSelectedItem().toString();
                String bulan_dipilih = bulan.getSelectedItem().toString();
                String tahun_dipilih = tahun.getSelectedItem().toString();

                try {
                    int responseLength = getHistoryOrder(tanggal_dipilih, bulan_dipilih, tahun_dipilih);
                    if (responseLength == 0) {
                        JOptionPane.showMessageDialog(Main.this, "No order found");
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public int getHistoryOrder(String tanggal, String bulan, String tahun) throws Exception {
        histOrderListModel.clear();
        switch (bulan) {
            case "Januari":
                bulan = "1";
                break;
            case "Februari" :
                bulan = "2";
                break;
            case "Maret":
                bulan = "3";
                break;
            case "April":
                bulan = "4";
                break;
            case "Mei":
                bulan = "5";
                break;
            case "Juni":
                bulan = "6";
                break;
            case "Juli":
                bulan = "7";
                break;
            case "Agustus":
                bulan = "8";
                break;
            case "September":
                bulan = "9";
                break;
            case "Oktober":
                bulan = "10";
                break;
            case "November":
                bulan = "11";
                break;
            case "Desember":
                bulan = "12";
                break;
        }
        String url = "http://localhost:8000/order/date/" + tahun + "-" + bulan + "-" + tanggal; ;
        System.out.println(url);
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

                String nama_menu = Order.getMenuByID(menuId);

                orderDetails.append(" ").append(nama_menu).append(": ").append(quantity).append(" \n");
            }

            orderDetails.append(" Total: ").append(totalHarga).append("\n").append(" Alamat: ").append(alamatPengiriman).append("\n").append(" Status: ").append(orderStatus).append("\n");

            orderMap.put(orderId, orderDetails);


        }

        for (int orderId : orderMap.keySet()) {
            StringBuilder orderDetails = orderMap.get(orderId);
            String orderInfo = " Order ID: " + orderId + "\n" + orderDetails.toString();
            histOrderListModel.addElement(orderInfo);
        }

        return responseArray.length();


    }


}
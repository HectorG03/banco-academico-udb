package vista;

import excepciones.BancoException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.Movimiento;
import modelo.TipoMovimiento;
import servicio.TransaccionServicio;

public class FrmTransacciones extends javax.swing.JFrame {
    
    private final TransaccionServicio transaccionServicio;
    private DefaultTableModel modeloTabla;
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public FrmTransacciones() {
        initComponents();
        
        setTitle("Gestión de Transacciones Bancarias");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        this.transaccionServicio = new TransaccionServicio();
        cargarComboOperaciones();
        configurarTabla();
        actualizarEstadoCamposSegunTipo();
        cargarHistorialCompleto();

        cmbTipoOperacion.addActionListener(e -> actualizarEstadoCamposSegunTipo());
        btnEjecutar.addActionListener(e -> ejecutarTransaccion());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnBuscarMovimiento.addActionListener(e -> consultarPorId());
        btnFiltrarCuenta.addActionListener(e -> filtrarPorCuenta());
        btnMostrarTodos.addActionListener(e -> cargarHistorialCompleto());
        btnCerrar.addActionListener(e -> dispose());
    }
    private void cargarComboOperaciones() {
        cmbTipoOperacion.setModel(new DefaultComboBoxModel<>(TipoMovimiento.values()));
    }
    
    private void configurarTabla() {
        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Tipo", "Monto", "Fecha y Hora", "Cuenta Origen", "Cuenta Destino", "Descripción"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblMovimientos.setModel(modeloTabla);
        tblMovimientos.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer renderMoneda = new DefaultTableCellRenderer() {
            private final NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.US);

            @Override
            protected void setValue(Object value) {
                setText(value instanceof Number ? formato.format(value) : "");
            }
        };
        renderMoneda.setHorizontalAlignment(SwingConstants.RIGHT);
        tblMovimientos.getColumnModel().getColumn(2).setCellRenderer(renderMoneda);
    }
    
    // Habilita o deshabilita Cuenta Origen / Destino según el tipo de movimiento
    private void actualizarEstadoCamposSegunTipo() {
        TipoMovimiento tipo = (TipoMovimiento) cmbTipoOperacion.getSelectedItem();
        if (tipo == null) {
            return;
        }
        switch (tipo) {
            case DEPOSITO:
                txtCuentaOrigen.setText("");
                txtCuentaOrigen.setEnabled(false);
                txtCuentaDestino.setEnabled(true);
                break;
            case RETIRO:
                txtCuentaOrigen.setEnabled(true);
                txtCuentaDestino.setText("");
                txtCuentaDestino.setEnabled(false);
                break;
            case TRANSFERENCIA:
                txtCuentaOrigen.setEnabled(true);
                txtCuentaDestino.setEnabled(true);
                break;
        }
    }
    
    private void ejecutarTransaccion() {
        TipoMovimiento tipo = (TipoMovimiento) cmbTipoOperacion.getSelectedItem();
        String cuentaOrigen = txtCuentaOrigen.getText().trim();
        String cuentaDestino = txtCuentaDestino.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        BigDecimal monto;
        try {
            monto = new BigDecimal(txtMonto.getText().trim());
        } catch (NumberFormatException ex) {
            mostrarError("Ingrese un monto numérico válido (ejemplo: 150.00).");
            return;
        }

        try {
            Movimiento movRealizado = null;
            if (tipo == TipoMovimiento.DEPOSITO) {
                movRealizado = transaccionServicio.realizarDeposito(cuentaDestino, monto, descripcion);
            } else if (tipo == TipoMovimiento.RETIRO) {
                movRealizado = transaccionServicio.realizarRetiro(cuentaOrigen, monto, descripcion);
            } else if (tipo == TipoMovimiento.TRANSFERENCIA) {
                movRealizado = transaccionServicio.realizarTransferencia(cuentaOrigen, cuentaDestino, monto, descripcion);
            }

            if (movRealizado != null) {
                mostrarInfo(tipo + " realizada con éxito.\nComprobante N°: " + movRealizado.getIdMovimiento()
                        + "\nMonto: $" + movRealizado.getMonto());
                limpiarFormulario();
                cargarHistorialCompleto();
            }

        } catch (BancoException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarError("Error técnico al procesar la transacción: " + ex.getMessage());
        }
    }
    
    private void consultarPorId() {
        String idTexto = txtBuscarIdMovimiento.getText().trim();
        if (idTexto.isEmpty()) {
            mostrarError("Ingrese el ID del movimiento que desea consultar.");
            return;
        }
        try {
            int id = Integer.parseInt(idTexto);
            Movimiento m = transaccionServicio.consultarMovimiento(id);
            modeloTabla.setRowCount(0);
            agregarFilaTabla(m);

            String detalle = String.format(
                    "Detalle del Movimiento #%d\nTipo: %s\nMonto: $%s\nFecha: %s\nOrigen: %s\nDestino: %s\nDescripción: %s",
                    m.getIdMovimiento(),
                    m.getTipoMovimiento(),
                    m.getMonto(),
                    m.getFechaHora() != null ? m.getFechaHora().format(formatoFecha) : "-",
                    m.getNumeroCuentaOrigen() != null ? m.getNumeroCuentaOrigen() : "N/A",
                    m.getNumeroCuentaDestino() != null ? m.getNumeroCuentaDestino() : "N/A",
                    m.getDescripcion() != null ? m.getDescripcion() : "-"
            );
            mostrarInfo(detalle);
        } catch (NumberFormatException ex) {
            mostrarError("El ID debe ser un número entero válido.");
        } catch (BancoException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarError("Error al consultar el movimiento: " + ex.getMessage());
        }
    }
    
    private void filtrarPorCuenta() {
        String cuenta = txtFiltrarCuenta.getText().trim();
        try {
            List<Movimiento> lista = transaccionServicio.listarHistorialPorCuenta(cuenta);
            modeloTabla.setRowCount(0);
            for (Movimiento m : lista) {
                agregarFilaTabla(m);
            }
            if (lista.isEmpty()) {
                mostrarInfo("La cuenta " + cuenta + " aún no tiene movimientos registrados.");
            }
        } catch (BancoException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarError("Error al filtrar historial: " + ex.getMessage());
        }
    }
    
    private void cargarHistorialCompleto() {
        try {
            modeloTabla.setRowCount(0);
            List<Movimiento> lista = transaccionServicio.listarHistorial();
            for (Movimiento m : lista) {
                agregarFilaTabla(m);
            }
        } catch (Exception ex) {
            mostrarError("Error al cargar historial de movimientos: " + ex.getMessage());
        }
    }

    private void agregarFilaTabla(Movimiento m) {
        modeloTabla.addRow(new Object[]{
            m.getIdMovimiento(),
            m.getTipoMovimiento(),
            m.getMonto(),
            m.getFechaHora() != null ? m.getFechaHora().format(formatoFecha) : "",
            m.getNumeroCuentaOrigen() != null ? m.getNumeroCuentaOrigen() : "—",
            m.getNumeroCuentaDestino() != null ? m.getNumeroCuentaDestino() : "—",
            m.getDescripcion() != null ? m.getDescripcion() : ""
        });
    }
    
    private void limpiarFormulario() {
        txtCuentaOrigen.setText("");
        txtCuentaDestino.setText("");
        txtMonto.setText("");
        txtDescripcion.setText("");
        txtBuscarIdMovimiento.setText("");
        txtFiltrarCuenta.setText("");
        actualizarEstadoCamposSegunTipo();
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Validación / Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Operación Bancaria", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        cmbTipoOperacion = new javax.swing.JComboBox<>();
        txtCuentaOrigen = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtMonto = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtCuentaDestino = new javax.swing.JTextField();
        txtDescripcion = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        btnEjecutar = new javax.swing.JToggleButton();
        btnLimpiar = new javax.swing.JToggleButton();
        jLabel6 = new javax.swing.JLabel();
        txtBuscarIdMovimiento = new javax.swing.JTextField();
        btnBuscarMovimiento = new javax.swing.JToggleButton();
        jLabel7 = new javax.swing.JLabel();
        txtFiltrarCuenta = new javax.swing.JTextField();
        btnFiltrarCuenta = new javax.swing.JToggleButton();
        btnMostrarTodos = new javax.swing.JToggleButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMovimientos = new javax.swing.JTable();
        btnCerrar = new javax.swing.JToggleButton();

        jTextField1.setText("jTextField1");

        jLabel8.setText("jLabel8");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Tipo de Operación: ");

        cmbTipoOperacion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel2.setText("Cuenta Origen: ");

        jLabel3.setText("Monto: ");

        jLabel4.setText("Cuenta Destino: ");

        jLabel5.setText("Descripción: ");

        btnEjecutar.setText("Realizar Operación");

        btnLimpiar.setText("Limpiar");

        jLabel6.setText("Consultar ID Movimiento: ");

        btnBuscarMovimiento.setText("Consultar ID");

        jLabel7.setText("Filtrar por Cuenta: ");

        btnFiltrarCuenta.setText("Filtrar Cuenta");

        btnMostrarTodos.setText("Ver Todos");

        tblMovimientos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblMovimientos);

        btnCerrar.setText("Cerrar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(208, 208, 208)
                        .addComponent(btnEjecutar))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBuscarIdMovimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnBuscarMovimiento)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFiltrarCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnFiltrarCuenta)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnMostrarTodos))
                    .addComponent(btnLimpiar))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtCuentaOrigen)
                            .addComponent(cmbTipoOperacion, 0, 112, Short.MAX_VALUE)
                            .addComponent(txtMonto))
                        .addGap(78, 78, 78)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtCuentaDestino, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                                .addGap(213, 213, 213))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addContainerGap())))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCerrar)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbTipoOperacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCuentaOrigen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(txtCuentaDestino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtMonto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEjecutar)
                    .addComponent(btnLimpiar))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtBuscarIdMovimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarMovimiento)
                    .addComponent(jLabel7)
                    .addComponent(txtFiltrarCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFiltrarCuenta)
                    .addComponent(btnMostrarTodos))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCerrar)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new FrmTransacciones().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnBuscarMovimiento;
    private javax.swing.JToggleButton btnCerrar;
    private javax.swing.JToggleButton btnEjecutar;
    private javax.swing.JToggleButton btnFiltrarCuenta;
    private javax.swing.JToggleButton btnLimpiar;
    private javax.swing.JToggleButton btnMostrarTodos;
    private javax.swing.JComboBox<String> cmbTipoOperacion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTable tblMovimientos;
    private javax.swing.JTextField txtBuscarIdMovimiento;
    private javax.swing.JTextField txtCuentaDestino;
    private javax.swing.JTextField txtCuentaOrigen;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtFiltrarCuenta;
    private javax.swing.JTextField txtMonto;
    // End of variables declaration//GEN-END:variables
}

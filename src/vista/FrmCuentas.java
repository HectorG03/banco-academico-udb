package vista;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import modelo.Cliente;
import modelo.Cuenta;
import modelo.EstadoCuenta;
import modelo.TipoCuenta;
import servicio.CuentaServicio;

public class FrmCuentas extends javax.swing.JPanel {

    private Cliente clienteActual;
    private CuentaServicio cuentaServicio; 
    private DefaultTableModel modeloTablaCuentas;
    
    public FrmCuentas(Cliente cliente) {
        initComponents();
        setPreferredSize(new java.awt.Dimension(680, 560));
        this.clienteActual = cliente;
        this.cuentaServicio = new CuentaServicio();
        
        // mostrar la info del cliente en los labels
        lblCliente.setText("Cuenta de: " + cliente.getNombre() + " (DUI: " + cliente.getDui() + ")");
        lblEstadoCliente.setText("Estado del cliente: " + cliente.getEstado());
        lblFecha.setText(LocalDate.now().toString());
      
        // cargar el Tipo de Cuenta
        cargarComboTipos();
        cargarComboEstadosCuenta();
        
        // cargar la tabla de cuentas del cliente
        configurarTablaCuentas();
        cargarTablaCuentas();
        
        // número de cuenta automático
        mostrarNuevoNumero();
        
        // conexión de botones
        btnCrearCuenta.addActionListener(e -> crearCuenta());
        btnCambiarEstadoCuenta.addActionListener(e -> cambiarEstadoCuentaSeleccionada());
        btnCerrar.addActionListener(e -> cerrarVentana());
        
        tblCuentas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tblCuentas.getSelectedRow();
                if (fila >= 0) {
                    Object estadoActual = modeloTablaCuentas.getValueAt(fila, 3);
                    cbEstadoCuenta.setSelectedItem(estadoActual);
                }
            }
        });
    }
    
    private void cargarComboTipos() {
        cbTipo.removeAllItems();
        DefaultComboBoxModel<TipoCuenta> modelo = new DefaultComboBoxModel<>(TipoCuenta.values());
        cbTipo.setModel((javax.swing.ComboBoxModel) modelo);
    }
    
    private void cargarComboEstadosCuenta() {
        cbEstadoCuenta.removeAllItems();
        DefaultComboBoxModel<EstadoCuenta> modelo = new DefaultComboBoxModel<>(EstadoCuenta.values());
        cbEstadoCuenta.setModel((javax.swing.ComboBoxModel) modelo);
    }
    
    private void configurarTablaCuentas() {
        modeloTablaCuentas = new DefaultTableModel(
                new Object[]{"Número de Cuenta", "Tipo", "Saldo", "Estado", "Fecha Apertura"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tblCuentas.setModel(modeloTablaCuentas);
        tblCuentas.getTableHeader().setReorderingAllowed(false);
        tblCuentas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Anchos proporcionales para que no se recorte ningún dato
        tblCuentas.getColumnModel().getColumn(0).setPreferredWidth(145); // Número de Cuenta
        tblCuentas.getColumnModel().getColumn(1).setPreferredWidth(110); // Tipo (PLAZO_FIJO)
        tblCuentas.getColumnModel().getColumn(2).setPreferredWidth(100); // Saldo
        tblCuentas.getColumnModel().getColumn(3).setPreferredWidth(90);  // Estado
        tblCuentas.getColumnModel().getColumn(4).setPreferredWidth(110); // Fecha Apertura

        // columna Saldo (índice 2) con formato de moneda: $1,000,000.00
        DefaultTableCellRenderer renderMoneda = new DefaultTableCellRenderer() {
            private final NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.US);

            @Override
            protected void setValue(Object value) {
                setText(value instanceof Number ? formato.format(value) : "");
            }
        };
        renderMoneda.setHorizontalAlignment(SwingConstants.RIGHT);
        tblCuentas.getColumnModel().getColumn(2).setCellRenderer(renderMoneda);
    } // fin configurarTablaCuentas
    
    // ajusta cada columna al texto más largo
    private void ajustarAnchoColumnas() {
        for (int col = 0; col < tblCuentas.getColumnCount(); col++) {
            TableColumn columna = tblCuentas.getColumnModel().getColumn(col);

            Component encabezado = tblCuentas.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(tblCuentas, columna.getHeaderValue(), false, false, -1, col);
            int ancho = encabezado.getPreferredSize().width;

            for (int fila = 0; fila < tblCuentas.getRowCount(); fila++) {
                Component celda = tblCuentas.prepareRenderer(tblCuentas.getCellRenderer(fila, col), fila, col);
                ancho = Math.max(ancho, celda.getPreferredSize().width);
            }
            columna.setPreferredWidth(ancho + 16);
        }
    }

    private void cargarTablaCuentas() {
        try {
            modeloTablaCuentas.setRowCount(0);
            
            List<Cuenta> lista = cuentaServicio.listarCuentasPorCliente(clienteActual.getCodigoCliente());
            for (Cuenta c : lista) {
                modeloTablaCuentas.addRow(new Object[]{
                    c.getNumeroCuenta(),
                    c.getTipoCuenta(),
                    c.getSaldo(),
                    c.getEstado(),
                    c.getFechaApertura()
                });
            }
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(this, "Error al cargar las cuentas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            mostrarError("Error al cargar las cuentas: " + e.getMessage());
        }
    } // fin cargarTablaCuentas
    
       
    // pide al servicio un número nuevo y lo muestra en la etiqueta
    private void mostrarNuevoNumero() {
        try {
            lblNumeroCuenta.setText(cuentaServicio.generarNumeroCuenta());
        } catch (Exception e) {
            lblNumeroCuenta.setText("");
            mostrarError("No se pudo generar el número de cuenta: " + e.getMessage());
        }
    }
    
    private void crearCuenta() {
        try {
            BigDecimal saldo;
            try {
                saldo = new BigDecimal(txtSaldoInicial.getText().trim()).setScale(2, RoundingMode.HALF_UP);
               
            } catch (NumberFormatException ex) {
                mostrarError("Saldo inválido. Ingrese un número, por ejemplo 100.50");
                return;
            }

            Cuenta cuenta = new Cuenta(
                    clienteActual.getCodigoCliente(),
                    lblNumeroCuenta.getText().trim(),
                    (TipoCuenta) cbTipo.getSelectedItem(),
                    saldo,
                    EstadoCuenta.ACTIVA,
                    LocalDate.now()
            );

            String error = cuentaServicio.registrar(cuenta);
            if (error != null) {
                mostrarError(error);
                return;
            }

            mostrarInfo("Cuenta " + cuenta.getNumeroCuenta() + " creada exitosamente.");
            cargarTablaCuentas();
            txtSaldoInicial.setText("");
            cbTipo.setSelectedIndex(0);
            mostrarNuevoNumero();
        } catch (Exception e) {
            mostrarError("Ocurrió un error inesperado al crear la cuenta: " + e.getMessage());
        }
    }
    
    private void cambiarEstadoCuentaSeleccionada() {
        int fila = tblCuentas.getSelectedRow();
        if (fila < 0) {
            mostrarError("Seleccione una cuenta de la tabla para cambiar su estado.");
            return;
        }
        try {
            String numeroCuenta = modeloTablaCuentas.getValueAt(fila, 0).toString();
            EstadoCuenta nuevoEstado = (EstadoCuenta) cbEstadoCuenta.getSelectedItem();

            cuentaServicio.cambiarEstadoCuenta(numeroCuenta, nuevoEstado);
            mostrarInfo("La cuenta " + numeroCuenta + " ahora está " + nuevoEstado + ".");
            cargarTablaCuentas();
        } catch (Exception e) {
            mostrarError("Error al cambiar estado de la cuenta: " + e.getMessage());
        }
    }
    
    private void cerrarVentana() {
        java.awt.Window ventana = SwingUtilities.getWindowAncestor(this);
        if (ventana != null) {
            ventana.dispose();
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblCliente = new javax.swing.JLabel();
        lblEstadoCliente = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cbTipo = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        txtSaldoInicial = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        btnCrearCuenta = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCuentas = new javax.swing.JTable();
        lblNumeroCuenta = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        cbEstadoCuenta = new javax.swing.JComboBox<>();
        btnCambiarEstadoCuenta = new javax.swing.JButton();

        jLabel3.setText("Nueva cuenta");

        jLabel4.setText("Número de cuenta:");

        jLabel5.setText("Tipo de cuenta:");

        cbTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel6.setText("Saldo Inicial:");

        jLabel7.setText("Fecha apertura: ");

        btnCrearCuenta.setText("Crear Cuenta");
        btnCrearCuenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearCuentaActionPerformed(evt);
            }
        });

        btnCerrar.setText("Cerrar");
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });

        tblCuentas.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblCuentas);

        jScrollPane2.setViewportView(jScrollPane1);

        lblNumeroCuenta.setPreferredSize(new java.awt.Dimension(22, 16));

        jLabel1.setText("Estado Cuenta: ");

        cbEstadoCuenta.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnCambiarEstadoCuenta.setText("Cambiar Estado");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel7))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(cbTipo, 0, 185, Short.MAX_VALUE)
                                        .addComponent(txtSaldoInicial)
                                        .addComponent(lblFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(lblNumeroCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(lblCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblEstadoCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCrearCuenta)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbEstadoCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnCambiarEstadoCuenta)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCerrar)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 683, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(lblCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblEstadoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(lblNumeroCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(cbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtSaldoInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCrearCuenta)
                    .addComponent(btnCerrar)
                    .addComponent(jLabel1)
                    .addComponent(cbEstadoCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCambiarEstadoCuenta))
                .addGap(31, 31, 31)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void btnCrearCuentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCrearCuentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCrearCuentaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCambiarEstadoCuenta;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnCrearCuenta;
    private javax.swing.JComboBox<String> cbEstadoCuenta;
    private javax.swing.JComboBox<String> cbTipo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblEstadoCliente;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblNumeroCuenta;
    private javax.swing.JTable tblCuentas;
    private javax.swing.JTextField txtSaldoInicial;
    // End of variables declaration//GEN-END:variables
}

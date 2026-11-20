
package vista;

import excepciones.BancoException;
import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import modelo.Cuenta;
import modelo.Movimiento;
import servicio.ReporteServicio;

public class FrmReportes extends javax.swing.JFrame {
    
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(FrmReportes.class);
    
    private final ReporteServicio reporteServicio;
    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    
    public FrmReportes() {
        initComponents();
        setTitle("Reportes del Sistema Bancario - Fase 2");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        this.reporteServicio = new ReporteServicio();
        cargarOpcionesReporte();
        alCambiarTipoReporte();
        cargarTotalesGenerales();

        cmbTipoReporte.addActionListener(e -> alCambiarTipoReporte());
        txtCuentaReporte.addActionListener(e -> generarReporteSeleccionado());
        btnGenerarReporte.addActionListener(e -> generarReporteSeleccionado());
        btnActualizarTotales.addActionListener(e -> cargarTotalesGenerales());
        btnCerrar.addActionListener(e -> dispose());
    }
    
    private void cargarOpcionesReporte() {
        cmbTipoReporte.setModel(new DefaultComboBoxModel<>(new String[]{
            "1. Cuentas Activas",
            "2. Cuentas Bloqueadas",
            "3. Clientes Registrados",
            "4. Historial de Transacciones",
            "5. Transacciones por Cuenta"
        }));
    }
    
    private void alCambiarTipoReporte() {
        int indice = cmbTipoReporte.getSelectedIndex();
        boolean esPorCuenta = (indice == 4);
        txtCuentaReporte.setEnabled(esPorCuenta);

        if (esPorCuenta) {
            txtCuentaReporte.setText("");
            txtCuentaReporte.requestFocus();
        } else {
            txtCuentaReporte.setText("");
            generarReporteSeleccionado();
        }
    }

    private void cargarTotalesGenerales() {
        try {
            BigDecimal[] totales = reporteServicio.obtenerTotalesGenerales();
            lblTotalDepositado.setText("Total Depositado: " + formatoMoneda.format(totales[0]));
            lblTotalRetirado.setText("Total Retirado: " + formatoMoneda.format(totales[1]));
            lblTotalTransferido.setText("Total Transferido: " + formatoMoneda.format(totales[2]));
        } catch (Exception ex) {
            mostrarError("Error al calcular totales generales: " + ex.getMessage());
        }
    }

    private void generarReporteSeleccionado() {
        int indice = cmbTipoReporte.getSelectedIndex();
        try {
            switch (indice) {
                case 0: // Cuentas Activas
                    mostrarTablaCuentas(reporteServicio.obtenerCuentasActivas());
                    break;
                case 1: // Cuentas Bloqueadas
                    mostrarTablaCuentas(reporteServicio.obtenerCuentasBloqueadas());
                    break;
                case 2: // Clientes Registrados
                    mostrarTablaClientes(reporteServicio.obtenerClientesRegistrados());
                    break;
                case 3: // Historial de Transacciones
                    mostrarTablaMovimientos(reporteServicio.obtenerHistorialTransacciones());
                    break;
                case 4: // Transacciones por Cuenta
                    String numCuenta = txtCuentaReporte.getText().trim();
                    mostrarTablaMovimientos(reporteServicio.obtenerTransaccionesPorCuenta(numCuenta));
                    break;
            }
            cargarTotalesGenerales();
        } catch (BancoException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarError("Error al generar reporte: " + ex.getMessage());
        }
    }
    
    private void mostrarTablaCuentas(List<Cuenta> cuentas) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID Cuenta", "Número de Cuenta", "Tipo", "Saldo", "Estado", "Fecha Apertura", "Código Cliente"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Cuenta c : cuentas) {
            modelo.addRow(new Object[]{
                c.getIdCuenta(),
                c.getNumeroCuenta(),
                c.getTipoCuenta(),
                formatoMoneda.format(c.getSaldo()),
                c.getEstado(),
                c.getFechaApertura(),
                c.getCodigoCliente()
            });
        }
        tblReportes.setModel(modelo);
        lblResumenCantidad.setText("Registros encontrados: " + cuentas.size());
    }

    private void mostrarTablaClientes(List<Cliente> clientes) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Código", "DUI", "Nombre", "Teléfono", "Correo", "Dirección", "Estado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Cliente cl : clientes) {
            modelo.addRow(new Object[]{
                cl.getCodigoCliente(),
                cl.getDui(),
                cl.getNombre(),
                cl.getTelefono(),
                cl.getCorreo(),
                cl.getDireccion(),
                cl.getEstado()
            });
        }
        tblReportes.setModel(modelo);
        lblResumenCantidad.setText("Registros encontrados: " + clientes.size());
    }
    
    private void mostrarTablaMovimientos(List<Movimiento> movimientos) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Tipo", "Monto", "Fecha y Hora", "Cuenta Origen", "Cuenta Destino", "Descripción"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Movimiento m : movimientos) {
            modelo.addRow(new Object[]{
                m.getIdMovimiento(),
                m.getTipoMovimiento(),
                formatoMoneda.format(m.getMonto()),
                m.getFechaHora() != null ? m.getFechaHora().format(formatoFecha) : "",
                m.getNumeroCuentaOrigen() != null ? m.getNumeroCuentaOrigen() : "—",
                m.getNumeroCuentaDestino() != null ? m.getNumeroCuentaDestino() : "—",
                m.getDescripcion() != null ? m.getDescripcion() : ""
            });
        }
        tblReportes.setModel(modelo);
        lblResumenCantidad.setText("Registros encontrados: " + movimientos.size());
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Reporte / Validación", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTotalDepositado = new javax.swing.JLabel();
        lblTotalTransferido = new javax.swing.JLabel();
        lblTotalRetirado = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        cmbTipoReporte = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        txtCuentaReporte = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblReportes = new javax.swing.JTable();
        lblResumenCantidad = new javax.swing.JLabel();
        btnGenerarReporte = new javax.swing.JButton();
        btnActualizarTotales = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblTotalDepositado.setText("Total Depositado: $0.00");

        lblTotalTransferido.setText("Total Transferido: $0.00");

        lblTotalRetirado.setText("Total Retirado: $0.00");

        jLabel1.setText("Seleccionar Reporte: ");

        cmbTipoReporte.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbTipoReporte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTipoReporteActionPerformed(evt);
            }
        });

        jLabel2.setText("N° Cuenta: ");

        tblReportes.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tblReportes);

        jScrollPane1.setViewportView(jScrollPane2);

        lblResumenCantidad.setText("Registros encontrados: 0");

        btnGenerarReporte.setText("Generar Reporte");
        btnGenerarReporte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarReporteActionPerformed(evt);
            }
        });

        btnActualizarTotales.setText("Actualizar Totales");
        btnActualizarTotales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarTotalesActionPerformed(evt);
            }
        });

        btnCerrar.setText("Cerrar");
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(cmbTipoReporte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(txtCuentaReporte, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnGenerarReporte)
                                .addGap(18, 18, 18)
                                .addComponent(btnActualizarTotales))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(lblTotalDepositado, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblTotalRetirado, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblTotalTransferido, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 129, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblResumenCantidad)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCerrar)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalDepositado, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotalTransferido, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotalRetirado, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbTipoReporte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(txtCuentaReporte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGenerarReporte)
                    .addComponent(btnActualizarTotales))
                .addGap(36, 36, 36)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblResumenCantidad)
                    .addComponent(btnCerrar))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGenerarReporteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarReporteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnGenerarReporteActionPerformed

    private void btnActualizarTotalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarTotalesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnActualizarTotalesActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void cmbTipoReporteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTipoReporteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbTipoReporteActionPerformed

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
            logger.error("Error al aplicar LookAndFeel en FrmReportes", ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new FrmReportes().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizarTotales;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnGenerarReporte;
    private javax.swing.JComboBox<String> cmbTipoReporte;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblResumenCantidad;
    private javax.swing.JLabel lblTotalDepositado;
    private javax.swing.JLabel lblTotalRetirado;
    private javax.swing.JLabel lblTotalTransferido;
    private javax.swing.JTable tblReportes;
    private javax.swing.JTextField txtCuentaReporte;
    // End of variables declaration//GEN-END:variables
}

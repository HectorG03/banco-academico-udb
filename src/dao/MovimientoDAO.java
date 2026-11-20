package dao;

import modelo.Movimiento;
import modelo.TipoMovimiento;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDAO {
    
    // metodo para insertar movimiento
    public void insertar(Connection con, Movimiento m) throws SQLException {
        String sql = "INSERT INTO movimientos (tipo_movimiento, monto, fecha_hora, id_cuenta_origen, id_cuenta_destino) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getTipoMovimiento().name());
            ps.setBigDecimal(2, m.getMonto());

            // Si la fecha en el objeto es null, le asignamos el momento actual por seguridad
            LocalDateTime fecha = m.getFechaHora() != null ? m.getFechaHora() : LocalDateTime.now();
            ps.setTimestamp(3, Timestamp.valueOf(fecha));

            // Manejo de cuentas origen y destino
            if (m.getIdCuentaOrigen() != null) {
                ps.setInt(4, m.getIdCuentaOrigen());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            if (m.getIdCuentaDestino() != null) {
                ps.setInt(5, m.getIdCuentaDestino());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.executeUpdate();
        }
    }
    
    
    // Lista todos los movimientos registrados 
    public List<Movimiento> listarTodos() throws SQLException {
        List<Movimiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM movimientos ORDER BY fecha_hora DESC";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Movimiento m = new Movimiento();
                m.setIdMovimiento(rs.getInt("id_movimiento"));
                m.setTipoMovimiento(TipoMovimiento.valueOf(rs.getString("tipo_movimiento")));
                m.setMonto(rs.getBigDecimal("monto"));

                Timestamp timestamp = rs.getTimestamp("fecha_hora");
                if (timestamp != null) {
                    m.setFechaHora(timestamp.toLocalDateTime());
                }

                int origen = rs.getInt("id_cuenta_origen");
                m.setIdCuentaOrigen(rs.wasNull() ? null : origen);

                int destino = rs.getInt("id_cuenta_destino");
                m.setIdCuentaDestino(rs.wasNull() ? null : destino);

                lista.add(m);
            }
        }
        return lista;
    }
    
}

package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.Movimiento;
import modelo.TipoMovimiento;

public class MovimientoDAO {
    
    // metodo para insertar movimiento dentro de una transacción y generar id
    public int insertar(Connection con, Movimiento m) throws SQLException {
        String sql = "INSERT INTO movimientos (tipo_movimiento, monto, fecha_hora, descripcion, id_cuenta_origen, id_cuenta_destino) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getTipoMovimiento().name());
            ps.setBigDecimal(2, m.getMonto());

            LocalDateTime fecha = m.getFechaHora() != null ? m.getFechaHora() : LocalDateTime.now();
            ps.setTimestamp(3, Timestamp.valueOf(fecha));

            if (m.getDescripcion() != null && !m.getDescripcion().trim().isEmpty()) {
                ps.setString(4, m.getDescripcion().trim());
            } else {
                ps.setNull(4, Types.VARCHAR);
            }

            if (m.getIdCuentaOrigen() != null) {
                ps.setInt(5, m.getIdCuentaOrigen());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            if (m.getIdCuentaDestino() != null) {
                ps.setInt(6, m.getIdCuentaDestino());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.executeUpdate();

            try (ResultSet rsKeys = ps.getGeneratedKeys()) {
                if (rsKeys.next()) {
                    int idGenerado = rsKeys.getInt(1);
                    m.setIdMovimiento(idGenerado);
                    return idGenerado;
                }
            }
        }
        return 0;
    }
    
    // consultar un movimiento especifico por su id
    public Movimiento buscarPorId(int idMovimiento) throws SQLException {
        String sql = "SELECT * FROM vw_historial_transacciones WHERE id_movimiento = ?";
        Movimiento mov = null;

        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMovimiento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mov = mapearMovimiento(rs);
                }
            }
        }
        return mov;
    }
    
    // Listar todos los movimientos registrados
    public List<Movimiento> listarTodos() throws SQLException {
        List<Movimiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM vw_historial_transacciones";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearMovimiento(rs));
            }
        }
        return lista;
    }

    // Listar movimientos donde participe un número de cuenta (como origen o destino)
    public List<Movimiento> listarPorCuenta(String numeroCuenta) throws SQLException {
        List<Movimiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM vw_historial_transacciones "
                + "WHERE cuenta_origen = ? OR cuenta_destino = ?";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, numeroCuenta);
            ps.setString(2, numeroCuenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMovimiento(rs));
                }
            }
        }
        return lista;
    }

    // Obtener [0]=Total Depositado, [1]=Total Retirado, [2]=Total Transferido
    public BigDecimal[] obtenerResumenTotales() throws SQLException {
        BigDecimal[] totales = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        String sql = "SELECT total_depositado, total_retirado, total_transferido FROM vw_resumen_totales";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                totales[0] = rs.getBigDecimal("total_depositado");
                totales[1] = rs.getBigDecimal("total_retirado");
                totales[2] = rs.getBigDecimal("total_transferido");
            }
        }
        return totales;
    }

    private Movimiento mapearMovimiento(ResultSet rs) throws SQLException {
        Movimiento m = new Movimiento();
        m.setIdMovimiento(rs.getInt("id_movimiento"));
        m.setTipoMovimiento(TipoMovimiento.valueOf(rs.getString("tipo_movimiento")));
        m.setMonto(rs.getBigDecimal("monto"));

        Timestamp timestamp = rs.getTimestamp("fecha_hora");
        if (timestamp != null) {
            m.setFechaHora(timestamp.toLocalDateTime());
        }

        m.setDescripcion(rs.getString("descripcion"));

        int origen = rs.getInt("id_cuenta_origen");
        m.setIdCuentaOrigen(rs.wasNull() ? null : origen);
        m.setNumeroCuentaOrigen(rs.getString("cuenta_origen"));

        int destino = rs.getInt("id_cuenta_destino");
        m.setIdCuentaDestino(rs.wasNull() ? null : destino);
        m.setNumeroCuentaDestino(rs.getString("cuenta_destino"));

        return m;
    }
}

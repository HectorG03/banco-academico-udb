package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Cuenta;
import modelo.EstadoCuenta;
import modelo.TipoCuenta;

public class CuentaDAO {
    
    // insertar nueva cuenta
    public void insertar (Cuenta cuenta) throws SQLException {
        String sql = "INSERT INTO cuentas (numero_cuenta, tipo_cuenta, saldo, estado, fecha_apertura, codigo_cliente) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, cuenta.getNumeroCuenta());
            ps.setString(2, cuenta.getTipoCuenta().name().replace("_", " "));
            ps.setBigDecimal(3, cuenta.getSaldo());
            ps.setString(4, formatearEstadoSQL(cuenta.getEstado()));
            ps.setObject(5, cuenta.getFechaApertura()); 
            ps.setInt(6, cuenta.getCodigoCliente());
            
            ps.executeUpdate();
        }
    }
    
    // listar todas las cuentas que pertenecen a un cliente especifico
    public List<Cuenta> listarPorCliente(int codigoCliente) throws SQLException {
        List<Cuenta> lista = new ArrayList<>();
        
        String sql = "SELECT id_cuenta, numero_cuenta, tipo_cuenta, saldo, estado, fecha_apertura, codigo_cliente "
                   + "FROM cuentas WHERE codigo_cliente = ? ORDER BY id_cuenta ASC";
        
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, codigoCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCuenta(rs));
                }
            }
        }
        return lista;
    }
    
    // buscar cuenta por numero de cuenta
    public Cuenta buscarPorNumero(String numeroCuenta) throws SQLException {
        String sql = "SELECT id_cuenta, numero_cuenta, tipo_cuenta, saldo, estado, fecha_apertura, codigo_cliente "
                   + "FROM cuentas WHERE numero_cuenta = ?";
        Cuenta cuenta = null;
        
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, numeroCuenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cuenta = mapearCuenta(rs);
                }
            }
        }
        return cuenta;
    }
    
    // buscar cuenta por id
    public Cuenta buscarPorId(int idCuenta) throws SQLException {
        String sql = "SELECT id_cuenta, numero_cuenta, tipo_cuenta, saldo, estado, fecha_apertura, codigo_cliente "
                + "FROM cuentas WHERE id_cuenta = ?";
        Cuenta cuenta = null;

        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCuenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cuenta = mapearCuenta(rs);
                }
            }
        }
        return cuenta;
    }
    
    // actualizar saldo usando una conexión transaccional externa (commit/rollback en Servicio)
    public boolean actualizarSaldo(Connection con, int idCuenta, BigDecimal nuevoSaldo) throws SQLException {
        String sql = "UPDATE cuentas SET saldo = ? WHERE id_cuenta = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, nuevoSaldo);
            ps.setInt(2, idCuenta);
            return ps.executeUpdate() > 0;
        }
    }

    // cambia estado de la cuenta (Activa, Inactiva, Bloqueada)
    public boolean cambiarEstado(int idCuenta, EstadoCuenta nuevoEstado) throws SQLException {
        String sql = "UPDATE cuentas SET estado = ? WHERE id_cuenta = ?";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, formatearEstadoSQL(nuevoEstado));
            ps.setInt(2, idCuenta);
            return ps.executeUpdate() > 0;
        }
    }

    // lista cuentas filtradas por estado (para Reportes: Activas / Bloqueadas)
    public List<Cuenta> listarPorEstado(EstadoCuenta estado) throws SQLException {
        List<Cuenta> lista = new ArrayList<>();
        String sql = "SELECT id_cuenta, numero_cuenta, tipo_cuenta, saldo, estado, fecha_apertura, codigo_cliente "
                + "FROM cuentas WHERE estado = ? ORDER BY id_cuenta ASC";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, formatearEstadoSQL(estado));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCuenta(rs));
                }
            }
        }
        return lista;
    }

    private String formatearEstadoSQL(EstadoCuenta estado) {
        String nombre = estado.name().toLowerCase();
        return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1);
    }
    
    // transformar fila de la bs en un objeto
    private Cuenta mapearCuenta(ResultSet rs) throws SQLException {
        Cuenta cuenta = new Cuenta();
        cuenta.setIdCuenta(rs.getInt("id_cuenta"));
        cuenta.setNumeroCuenta(rs.getString("numero_cuenta"));
        cuenta.setTipoCuenta(TipoCuenta.valueOf(rs.getString("tipo_cuenta").toUpperCase().replace(" ", "_")));
        cuenta.setSaldo(rs.getBigDecimal("saldo"));
        cuenta.setEstado(EstadoCuenta.valueOf(rs.getString("estado").toUpperCase()));
        cuenta.setFechaApertura(rs.getDate("fecha_apertura").toLocalDate());
        cuenta.setCodigoCliente(rs.getInt("codigo_cliente"));
        return cuenta;
    }
}

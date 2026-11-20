package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Cuenta {
    private int idCuenta, codigoCliente;
    private String numeroCuenta;
    private TipoCuenta tipoCuenta;
    private BigDecimal saldo;
    private EstadoCuenta estado;
    private LocalDate fechaApertura;
    
    // constructor vacio
    public Cuenta(){
    }
    
    // constructor para leer bd
    public Cuenta(int idCuenta, int codigoCliente, String numeroCuenta, TipoCuenta tipoCuenta, BigDecimal saldo, EstadoCuenta estado, LocalDate fechaApertura) {
        this.idCuenta = idCuenta;
        this.codigoCliente = codigoCliente;
        this.numeroCuenta = numeroCuenta;
        this.tipoCuenta = tipoCuenta;
        this.saldo = saldo;
        this.estado = estado;
        this.fechaApertura = fechaApertura;
    }    

    public Cuenta(int codigoCliente, String numeroCuenta, TipoCuenta tipoCuenta, BigDecimal saldo, EstadoCuenta estado, LocalDate fechaApertura) {
        this.codigoCliente = codigoCliente;
        this.numeroCuenta = numeroCuenta;
        this.tipoCuenta = tipoCuenta;
        this.saldo = saldo;
        this.estado = estado;
        this.fechaApertura = fechaApertura;
    }

    public int getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(int idCuenta) {
        this.idCuenta = idCuenta;
    }

    public int getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(int codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public TipoCuenta getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(TipoCuenta tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuenta estado) {
        this.estado = estado;
    }

    public LocalDate getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDate fechaApertura) {
        this.fechaApertura = fechaApertura;
    } 
    
}

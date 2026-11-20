package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Movimiento {
    private int idMovimiento;
    private TipoMovimiento tipoMovimiento;
    private BigDecimal monto;
    private LocalDateTime fechaHora;
    private Integer idCuentaOrigen; 
    private Integer idCuentaDestino;

    public Movimiento() {
    }

    public Movimiento(int idMovimiento, TipoMovimiento tipoMovimiento, BigDecimal monto, LocalDateTime fechaHora, Integer idCuentaOrigen, Integer idCuentaDestino) {
        this.idMovimiento = idMovimiento;
        this.tipoMovimiento = tipoMovimiento;
        this.monto = monto;
        this.fechaHora = fechaHora;
        this.idCuentaOrigen = idCuentaOrigen;
        this.idCuentaDestino = idCuentaDestino;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Integer getIdCuentaOrigen() {
        return idCuentaOrigen;
    }

    public void setIdCuentaOrigen(Integer idCuentaOrigen) {
        this.idCuentaOrigen = idCuentaOrigen;
    }

    public Integer getIdCuentaDestino() {
        return idCuentaDestino;
    }

    public void setIdCuentaDestino(Integer idCuentaDestino) {
        this.idCuentaDestino = idCuentaDestino;
    }
    
      
}
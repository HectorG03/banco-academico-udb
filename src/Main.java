import javax.swing.UIManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vista.FrmClientes;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            logger.warn("No se pudo aplicar el tema Nimbus: {}", ex.getMessage());
        }

        logger.info("=== Iniciando Sistema Bancario Desktop - Fase 2 ===");
        java.awt.EventQueue.invokeLater(() -> {
            FrmClientes ventanaPrincipal = new FrmClientes();
            ventanaPrincipal.setLocationRelativeTo(null);
            ventanaPrincipal.setVisible(true);
        });
    }
}

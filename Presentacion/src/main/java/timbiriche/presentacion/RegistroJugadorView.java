package timbiriche.presentacion;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import java.net.URL;

/**
 * Vista para el caso de uso RegistrarJugador.
 */
public class RegistroJugadorView extends JFrame {

    private JTextField txtNickname;
    private JButton btnRegistrar;
    private JButton btnAtras;
    private int avatarSeleccionado = -1;

    private RegistroJugadorListener listener;

    // Rutas de los cuatro avatares dentro del JAR
    String[] AVATAR_PATHS = {
        "/avatars/avatar1.png",
        "/avatars/avatar2.png",
        "/avatars/avatar3.png",
        "/avatars/avatar4.png"
    };

    public RegistroJugadorView() {
        setTitle("Registrar Jugador");
        setSize(400, 300);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1, 10, 10));

        JLabel lblNick = new JLabel("Nickname:");
        txtNickname = new JTextField();
        panel.add(lblNick);
        panel.add(txtNickname);

        JLabel lblAvatar = new JLabel("Selecciona tu avatar:");
        panel.add(lblAvatar);

        // Panel de botones/avatar
        JPanel panelAvatares = new JPanel();
        panelAvatares.setLayout(new GridLayout(1, 4, 5, 5));

        for (int i = 0; i < 4; i++) {
            int id = i;
            JButton avatarBtn = new JButton();

            URL url = getClass().getResource(AVATAR_PATHS[i]);
            if (url != null) {
                avatarBtn.setIcon(new ImageIcon(url));
                avatarBtn.setText("");
            } else {
                avatarBtn.setText("Avatar " + (i + 1));
                System.err.println("No se encontró recurso: " + AVATAR_PATHS[i]);
            }

            avatarBtn.addActionListener(e -> avatarSeleccionado = id);
            panelAvatares.add(avatarBtn);
        }

        panel.add(panelAvatares);

        // Panel inferior con botones
        JPanel panelBotones = new JPanel();
        btnRegistrar = new JButton("Registrar");
        btnAtras = new JButton("Atrás");

        btnRegistrar.addActionListener((ActionEvent e) -> onRegistrar());
        btnAtras.addActionListener((ActionEvent e) -> onAtras());

        panelBotones.add(btnAtras);
        panelBotones.add(btnRegistrar);

        add(panel, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    public void setRegistroJugadorListener(RegistroJugadorListener listener) {
        this.listener = listener;
    }

    private void onRegistrar() {
        String nickname = txtNickname.getText().trim();
        if (nickname.isEmpty() || avatarSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Debes ingresar un nombre y seleccionar un avatar.");
            return;
        }

        if (listener != null) {
            listener.onRegistrarJugador(nickname, avatarSeleccionado);
        }
        setVisible(false);
    }

    private void onAtras() {
        System.exit(0);
    }

    public void mostrar() {
        setVisible(true);
    }

    public String getNickname() {
        return txtNickname.getText().trim();
    }

    public int getAvatarSeleccionado() {
        return avatarSeleccionado;
    }

    public static interface RegistroJugadorListener {
        void onRegistrarJugador(String nickname, int avatarId);
    }
}

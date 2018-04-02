package es.um.redes.nanoGames.broker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import jdk.nashorn.internal.objects.annotations.ScriptClass;

/**
 * Cliente SNMP sin dependencias con otras clases y con funciones de consulta
 * espec�ficas. En la actual versi�n s�lo soporta una funci�n de consulta sobre
 * el UPTIME del host.
 */
public class BrokerClient {
	private static final int PACKET_MAX_SIZE = 484;
	private static final int DEFAULT_PORT = 161;
	private static final String OID_UPTIME = "1.3.6.1.2.1.1.3.0";

	private DatagramSocket socket; // socket UDP
	private InetSocketAddress agentAddress; // direccion del agente SNMP
	// private long token;

	/**
	 * Constructor usando par�metros por defecto
	 * 
	 * @throws IOException
	 */

	public BrokerClient(String agentName) {
		// Registrar dirección del servidor
		// Crear socket de cliente
		try {
			try {
				// Creacion de IP Socket Address ( IP + PUERTO), con el nombre del agenteSMP y
				// su puerto por defecto.
				this.agentAddress = new InetSocketAddress(InetAddress.getByName(agentName), DEFAULT_PORT);
			} catch (UnknownHostException e) {
				// Fallo al conectarse un host.
				System.err.println("IP address of a host could not be determined");
			}
			// Creacion de socket (para enviar y recivir datagram packets) que escucha en
			// cualquier puerto libre disponible.
			socket = new DatagramSocket();
		} catch (SocketException e) {
			System.err.println("No se ha podido crear el socket.");
		}

	}

	private byte[] buildRequest() throws IOException {
		// mensaje GetRequest
		ByteArrayOutputStream request = new ByteArrayOutputStream();
		request.write(new byte[] { 0x30, 0x26 }); // Message (SEQUENCE)
		request.write(new byte[] { 0x02, 0x01, 0x00 }); // Version
		request.write(new byte[] { 0x04, 0x06 }); // Community
		request.write("public".getBytes());
		request.write(new byte[] { (byte) 0xa0, 0x19 }); // GetRequest
		request.write(new byte[] { (byte) 0x02, 0x01, 0x00 }); // RequestId
		request.write(new byte[] { (byte) 0x02, 0x01, 0x00 }); // ErrorStatus
		request.write(new byte[] { (byte) 0x02, 0x01, 0x00 }); // ErrorIndex
		request.write(new byte[] { (byte) 0x30, 0x0e }); // Bindings (SEQUENCE)
		request.write(new byte[] { (byte) 0x30, 0x0c }); // Bindings Child (SEQUENCE)
		request.write(new byte[] { (byte) 0x06 }); // OID
		byte[] oidArray = encodeOID(OID_UPTIME);
		request.write((byte) oidArray.length);
		request.write(oidArray);
		request.write(new byte[] { (byte) 0x05, 0x00 }); // Value (NULL)

		return request.toByteArray();

	}

	private long getTimeTicks(byte[] data) {
		ByteArrayInputStream response = new ByteArrayInputStream(data);

		// recuperamos timeTicks a partir de la respuesta
		int ch;
		while ((ch = response.read()) != -1) {
			if (ch == 0x43) { // TimeTicks
				int len = response.read();
				byte[] value = new byte[len];
				response.read(value, 0, len);
				return new BigInteger(value).longValue();
			}
		}
		return 0;
	}

	/**
	 * Env�a un solicitud GET al agente para el objeto UPTIME
	 * 
	 * @return long
	 * @throws IOException
	 */
	public long getToken() throws IOException {

		// Creacion del buffer de envio con tamaño maximo.
		byte[] paqueteEnvio = new byte[PACKET_MAX_SIZE];
		// Construccion del paquete a enviar(solicitud).
		paqueteEnvio = buildRequest();
		// Creacion de un DatagramPacket para ser enviado.
		DatagramPacket dpEnvio = new DatagramPacket(paqueteEnvio, paqueteEnvio.length, this.agentAddress);
		// Enviar paquete por el socket.
		this.socket.send(dpEnvio);
		// Timeout de 1s.
		this.socket.setSoTimeout(1000);
		// Creacion del buffer de recepcion con tamaño maximo.
		byte[] paqueteRecepcion = new byte[PACKET_MAX_SIZE]; 
		// Creacion del DatagramPacket de repecion.
		DatagramPacket dpRecepcion = new DatagramPacket(paqueteRecepcion, paqueteRecepcion.length);
		// Recibe el paquete por el socket.
		this.socket.receive(dpRecepcion);
		// Extraemos y devolvemos el token(TimeTicks)
		return getTimeTicks(dpRecepcion.getData());

	}

	/**
	 * Codifica un OID según la especifación SNMP Nota: sólo soporta OIDs con
	 * números de uno o dos dígitos
	 * 
	 * @param oid
	 * @return
	 */
	private byte[] encodeOID(String oid) {
		// parsea OID
		String digits[] = oid.split("\\.");
		byte[] value = new byte[digits.length];
		for (int i = 0; i < digits.length; i++)
			value[i] = (byte) Byte.parseByte(digits[i]);

		// codifica OID
		byte[] ret = new byte[value.length - 1];
		byte x = value[0];
		byte y = value.length <= 1 ? 0 : value[1];
		for (int i = 1; i < value.length; i++) {
			ret[i - 1] = (byte) ((i != 1) ? value[i] : x * 40 + y);
		}
		return ret;
	}

	public void close() {
		socket.close();
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public InetSocketAddress getAgentAddress() {
		return agentAddress;
	}

	// Main de prueba de la clase 
	public static void main(String[] args) {
		BrokerClient bcs = new BrokerClient("localhost");
		try {
			long prueba = bcs.getToken();
			System.out.println("Token: " + prueba);
		} catch (IOException e) {
			System.out.println("Token no encontrado");
		}

	}
}
